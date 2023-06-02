package com.linkedin.datahub.upgrade.test;

import com.linkedin.common.urn.Urn;
import com.linkedin.datahub.upgrade.UpgradeContext;
import com.linkedin.datahub.upgrade.UpgradeStep;
import com.linkedin.datahub.upgrade.UpgradeStepResult;
import com.linkedin.datahub.upgrade.impl.DefaultUpgradeStepResult;
import com.linkedin.metadata.search.EntitySearchService;
import com.linkedin.metadata.search.ScrollResult;
import com.linkedin.metadata.search.SearchEntity;
import com.linkedin.metadata.test.TestEngine;
import com.linkedin.test.TestResults;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class EvaluateTestsStep implements UpgradeStep {

  private static final String ELASTIC_TIMEOUT = System.getenv()
      .getOrDefault(EvaluateTests.ELASTIC_TIMEOUT_ENV_NAME,
          "5m");

  private final EntitySearchService _entitySearchService;
  private final TestEngine _testEngine;
  private final ExecutorService _executorService;

  public EvaluateTestsStep(@Nonnull EntitySearchService entitySearchService, @Nonnull TestEngine testEngine) {
    _entitySearchService = entitySearchService;
    _testEngine = testEngine;

    int numThreads = Integer.parseInt(System.getenv().getOrDefault(EvaluateTests.EXECUTOR_POOL_SIZE,
        String.valueOf(Runtime.getRuntime().availableProcessors() + 1)));
    _executorService = Executors.newFixedThreadPool(numThreads);
  }

  @Override
  public String id() {
    return "EvaluateTests";
  }

  @Override
  public int retryCount() {
    return 0;
  }

  @Override
  public Function<UpgradeContext, UpgradeStepResult> executable() {
    return (context) -> {

      context.report().addLine("Starting to evaluate tests...");

      int batchSize =
          context.parsedArgs().getOrDefault("BATCH_SIZE", Optional.empty()).map(Integer::parseInt).orElse(1000);

      Set<String> entityTypesToEvaluate = new HashSet<>(_testEngine.getEntityTypesToEvaluate());
      context.report().addLine(String.format("Evaluating tests for entities %s", entityTypesToEvaluate));

      List<Future<Map<Urn, TestResults>>> futures = new ArrayList<>();
      for (String entityType : entityTypesToEvaluate) {
        int batch = 1;
        String nextScrollId = null;
        do {
          context.report().addLine(String.format("Fetching batch %d of %s entities", batch, entityType));
          ScrollResult scrollResult = _entitySearchService.scroll(
            Collections.singletonList(entityType), null, null, batchSize, nextScrollId, ELASTIC_TIMEOUT);
          nextScrollId = scrollResult.getScrollId();
          context.report().addLine(String.format("Processing batch %d of %s entities", batch, entityType));
          List<Urn> entitiesInBatch =
              scrollResult.getEntities().stream().map(SearchEntity::getEntity).collect(Collectors.toList());
          final int batchNumber = batch;
          futures.add(_executorService.submit(() -> processBatch(entitiesInBatch, batchNumber, entityType, context)));
          batch++;
        } while (nextScrollId != null);

        context.report().addLine(String.format("Finished submitting test evaluation for %s entities to worker pool.", entityType));
      }

      for (Future<Map<Urn, TestResults>> results : futures) {
        // Wait for processing, we don't actually use the result currently for anything for now, so treat as void
        try {
          results.get();
        } catch (InterruptedException | ExecutionException e) {
          context.report().addLine("Reading interrupted, not able to finish processing.");
          throw new RuntimeException(e);
        }
      }
      context.report().addLine("Finished evaluating tests for all entities");

      return new DefaultUpgradeStepResult(id(), UpgradeStepResult.Result.SUCCEEDED);
    };
  }

  private Map<Urn, TestResults> processBatch(List<Urn> entitiesInBatch, int batchNumber, String entityType, UpgradeContext context) {
    {
      Map<Urn, TestResults> result;
      try {
        result = _testEngine.batchEvaluateTestsForEntities(entitiesInBatch, TestEngine.EvaluationMode.DEFAULT);
        context.report()
            .addLine(String.format("Pushed %d test results for batch %d of %s entities", result.size(), batchNumber,
                entityType));
        return result;
      } catch (Exception e) {
        context.report().addLine(String.format("Error while processing batch %d of %s entities", batchNumber, entityType));
        log.error("Error while processing batch {} of {} entities", batchNumber, entityType, e);
      }
      return null;
    }
  }
}
