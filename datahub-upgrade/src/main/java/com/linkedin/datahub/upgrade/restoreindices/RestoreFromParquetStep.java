package com.linkedin.datahub.upgrade.restoreindices;

import com.google.common.collect.ImmutableBiMap;
import com.linkedin.common.AuditStamp;
import com.linkedin.common.urn.Urn;
import com.linkedin.common.urn.UrnUtils;
import com.linkedin.data.template.RecordTemplate;
import com.linkedin.datahub.upgrade.UpgradeContext;
import com.linkedin.datahub.upgrade.UpgradeStep;
import com.linkedin.datahub.upgrade.UpgradeStepResult;
import com.linkedin.datahub.upgrade.impl.DefaultUpgradeStepResult;
import com.linkedin.datahub.upgrade.restorebackup.backupreader.BackupReader;
import com.linkedin.datahub.upgrade.restorebackup.backupreader.BackupReaderArgs;
import com.linkedin.datahub.upgrade.restorebackup.backupreader.EbeanAspectBackupIterator;
import com.linkedin.datahub.upgrade.restorebackup.backupreader.LocalParquetReader;
import com.linkedin.datahub.upgrade.restorebackup.backupreader.S3BackupReader;
import com.linkedin.events.metadata.ChangeType;
import com.linkedin.metadata.entity.EntityService;
import com.linkedin.metadata.entity.EntityUtils;
import com.linkedin.metadata.entity.ebean.EbeanAspectV2;
import com.linkedin.metadata.models.AspectSpec;
import com.linkedin.metadata.models.EntitySpec;
import com.linkedin.metadata.models.registry.EntityRegistry;
import com.linkedin.mxe.SystemMetadata;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.linkedin.metadata.Constants.*;


public class RestoreFromParquetStep implements UpgradeStep {

  public static final String DRY_RUN = "dry_run";
  private static final int DEFAULT_BATCH_SIZE = 1000;

  private final EntityService _entityService;
  private final EntityRegistry _entityRegistry;
  private final Map<String, Class<? extends BackupReader>> _backupReaders;

  public RestoreFromParquetStep(final EntityService entityService, final EntityRegistry entityRegistry) {
    _entityService = entityService;
    _entityRegistry = entityRegistry;
    _backupReaders = ImmutableBiMap.of(LocalParquetReader.READER_NAME, LocalParquetReader.class,
        S3BackupReader.READER_NAME, S3BackupReader.class);
  }

  @Override
  public String id() {
    return "RestoreStorageStep";
  }

  @Override
  public int retryCount() {
    return 0;
  }

  @Override
  public boolean skip(UpgradeContext context) {
    if (context.parsedArgs().containsKey(RestoreIndices.RESTORE_FROM_PARQUET)) {
      return false;
    }

    return true;
  }

  @Override
  public Function<UpgradeContext, UpgradeStepResult> executable() {
    return (context) -> {

      context.report().addLine("Reading backup from parquet file...");
      int numRows = 0;
      int batchSize = getBatchSize(context.parsedArgs());
      Optional<String> backupReaderName = context.parsedArgs().get("BACKUP_READER");
      if (!backupReaderName.isPresent() || !_backupReaders.containsKey(backupReaderName.get())) {
        context.report().addLine("BACKUP_READER is not set or is not valid");
        return new DefaultUpgradeStepResult(id(), UpgradeStepResult.Result.FAILED);
      }

      Class<? extends BackupReader> clazz = _backupReaders.get(backupReaderName.get());
      List<String> argNames = BackupReaderArgs.getArgNames(clazz);
      List<Optional<String>> args = argNames.stream().map(argName -> context.parsedArgs().get(argName)).collect(
          Collectors.toList());
      BackupReader backupReader;
      try {
        backupReader = clazz.getConstructor(List.class).newInstance(args);
      } catch (InstantiationException | InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
        context.report().addLine("Invalid BackupReader, not able to construct instance of " + clazz.getSimpleName());
        throw new IllegalArgumentException("Invalid BackupReader: " + clazz.getSimpleName() + ", need to implement proper constructor.");
      }
      EbeanAspectBackupIterator iterator = backupReader.getBackupIterator(context);
      EbeanAspectV2 aspect;
      long startTime = System.currentTimeMillis();
      while ((aspect = iterator.next()) != null) {
        if (aspect.getVersion() != 0) {
          continue;
        }
        numRows++;

        if (context.parsedArgs().containsKey(DRY_RUN)) {
          if (numRows % batchSize == 0) {
            context.report()
                .addLine(String.format("Dry run enabled, continuing. Took %s ms to read %s aspects from parquet.",
                    System.currentTimeMillis() - startTime, batchSize));
            startTime = System.currentTimeMillis();
          }
        }

        // 1. Extract an Entity type from the entity Urn
        Urn urn;
        try {
          urn = Urn.createFromString(aspect.getKey().getUrn());
        } catch (Exception e) {
          context.report()
              .addLine(String.format("Failed to bind Urn with value %s into Urn object: %s. Ignoring row.",
                  aspect.getKey().getUrn(), e));
          continue;
        }

        // 2. Verify that the entity associated with the aspect is found in the registry.
        final String entityName = urn.getEntityType();
        final EntitySpec entitySpec;
        try {
          entitySpec = _entityRegistry.getEntitySpec(entityName);
        } catch (Exception e) {
          context.report()
              .addLine(String.format("Failed to find entity with name %s in Entity Registry: %s. Ignoring row.",
                  entityName, e));
          continue;
        }
        final String aspectName = aspect.getKey().getAspect();

        // 3. Verify that the aspect is a valid aspect associated with the entity
        AspectSpec aspectSpec = entitySpec.getAspectSpec(aspectName);
        if (aspectSpec == null) {
          context.report()
              .addLine(String.format("Failed to find aspect with name %s associated with entity named %s", aspectName,
                  entityName));
          continue;
        }

        // 4. Create record from json aspect
        final RecordTemplate aspectRecord;
        try {
          aspectRecord = EntityUtils.toAspectRecord(entityName, aspectName, aspect.getMetadata(), _entityRegistry);
        } catch (Exception e) {
          context.report()
              .addLine(String.format("Failed to deserialize row %s for entity %s, aspect %s: %s. Ignoring row.",
                  aspect.getMetadata(), entityName, aspectName, e));
          continue;
        }

        SystemMetadata latestSystemMetadata = EntityUtils.parseSystemMetadata(aspect.getSystemMetadata());

        // 5. Produce MAE events for the aspect record
        _entityService.produceMetadataChangeLog(urn, entityName, aspectName, aspectSpec, null, aspectRecord, null,
            latestSystemMetadata,
            new AuditStamp().setActor(UrnUtils.getUrn(SYSTEM_ACTOR)).setTime(System.currentTimeMillis()),
            ChangeType.RESTATE);
        if (numRows % batchSize == 0) {
          context.report().addLine(String.format("Took %s ms to produce %s MCLs.",
              System.currentTimeMillis() - startTime, batchSize));
        }
      }

      context.report().addLine(String.format("Added %d rows to the aspect v2 table", numRows));
      return new DefaultUpgradeStepResult(id(), UpgradeStepResult.Result.SUCCEEDED);
    };
  }

  private int getBatchSize(final Map<String, Optional<String>> parsedArgs) {
    int resolvedBatchSize = DEFAULT_BATCH_SIZE;
    if (parsedArgs.containsKey(RestoreIndices.BATCH_SIZE_ARG_NAME) && parsedArgs.get(RestoreIndices.BATCH_SIZE_ARG_NAME)
        .isPresent()) {
      resolvedBatchSize = Integer.parseInt(parsedArgs.get(RestoreIndices.BATCH_SIZE_ARG_NAME).get());
    }
    return resolvedBatchSize;
  }
}
