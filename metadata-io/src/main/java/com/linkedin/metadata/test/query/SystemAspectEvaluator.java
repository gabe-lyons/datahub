package com.linkedin.metadata.test.query;

import com.linkedin.common.urn.Urn;
import com.linkedin.entity.EntityResponse;
import com.linkedin.entity.EnvelopedAspect;
import com.linkedin.metadata.entity.EntityService;
import com.linkedin.metadata.models.AspectSpec;
import com.linkedin.metadata.models.EntitySpec;
import com.linkedin.metadata.test.definition.ValidationResult;
import com.linkedin.metadata.utils.SystemMetadataUtils;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Evaluator that supports resolving the 'firstSynchronized', 'lastSynchronized', 'lastUpdated'
 * system queries for a given URN.
 */
@Slf4j
@RequiredArgsConstructor
public class SystemAspectEvaluator extends BaseQueryEvaluator {

  private final EntityService entityService;

  private static final String FIRST_SYNCHRONIZED_FIELD_NAME = "firstSynchronized";

  private static final String LAST_SYNCHRONIZED_FIELD_NAME = "lastSynchronized";

  private static final String LAST_UPDATED_FIELD_NAME = "lastUpdated";

  @Override
  public boolean isEligible(@Nonnull final String entityType, @Nonnull final TestQuery query) {
    if (query.getQueryParts().isEmpty()) {
      return false;
    }

    final String queryName = query.getQuery();
    return FIRST_SYNCHRONIZED_FIELD_NAME.equalsIgnoreCase(queryName) || LAST_SYNCHRONIZED_FIELD_NAME.equalsIgnoreCase(
        queryName) || LAST_UPDATED_FIELD_NAME.equalsIgnoreCase(queryName);
  }

  @Override
  @Nonnull
  public ValidationResult validateQuery(@Nonnull final String entityType, @Nonnull final TestQuery query)
      throws IllegalArgumentException {
    final boolean res = query.getQueryParts().size() == 1 && (FIRST_SYNCHRONIZED_FIELD_NAME.equalsIgnoreCase(query.getQuery())
        || LAST_SYNCHRONIZED_FIELD_NAME.equalsIgnoreCase(query.getQuery()) || LAST_UPDATED_FIELD_NAME.equalsIgnoreCase(query.getQuery()));
    return new ValidationResult(res, Collections.emptyList());
  }

  @Override
  @Nonnull
  public Map<Urn, Map<TestQuery, TestQueryResponse>> evaluate(@Nonnull final String entityType,
      @Nonnull final Set<Urn> urns, @Nonnull final Set<TestQuery> queries) {
    final Map<Urn, Map<TestQuery, TestQueryResponse>> result = new HashMap<>();
    for (TestQuery query : queries) {
      try {
        final EntitySpec entitySpec = entityService.getEntityRegistry().getEntitySpec(entityType);
        final Set<String> aspectSpecNames;

        // Limit the number of aspects to pull from entity service if only looking for first synchronized
        if (query.getQuery().equalsIgnoreCase(FIRST_SYNCHRONIZED_FIELD_NAME)) {
          aspectSpecNames = Set.of(entitySpec.getKeyAspectName());
        } else {
          aspectSpecNames = entitySpec.getAspectSpecs().stream().map(AspectSpec::getName)
              .collect(Collectors.toSet());
        }

        entityService.getEntitiesV2(entityType, urns, aspectSpecNames)
            .forEach((urn, response) -> {
              result.putIfAbsent(urn, new HashMap<>());
              result.get(urn).put(query, buildSystemQueryResponse(query, urn, response));
            });
      } catch (URISyntaxException e) {
        log.error("Error while fetching aspects for urns {}", urns, e);
        throw new RuntimeException(
            String.format("Error while fetching aspects for urns %s", urns));
      }
    }
    return result;
  }

  private TestQueryResponse buildSystemQueryResponse(@Nonnull final TestQuery query, @Nonnull final Urn urn, @Nonnull final EntityResponse entityResponse) {
    final String queryName = query.getQuery();
    switch (queryName) {
      case FIRST_SYNCHRONIZED_FIELD_NAME:
        final String keyAspectName = entityService.getEntityRegistry()
            .getEntitySpec(urn.getEntityType())
            .getKeyAspectName();
        final EnvelopedAspect keyAspect = entityResponse.getAspects().get(keyAspectName);
        if (keyAspect == null) {
          log.error("Unable to retrieve key aspect for urn: {}", urn);
          throw new RuntimeException(String.format("Unable to retrieve key aspect for urn: %s", urn));
        }
        final String keyAspectCreatedTime = keyAspect.getCreated().getTime().toString();
        return new TestQueryResponse(Collections.singletonList(keyAspectCreatedTime));
      case LAST_SYNCHRONIZED_FIELD_NAME:
        final String lastSynchronizedTime = SystemMetadataUtils.getLastIngested(entityResponse.getAspects()).toString();
        return new TestQueryResponse(Collections.singletonList(lastSynchronizedTime));
      case LAST_UPDATED_FIELD_NAME:
        final String lastUpdatedTime = entityResponse.getAspects()
            .values()
            .stream()
            .map(aspect -> aspect.getCreated().getTime())
            .max(Long::compareTo)
            .orElseThrow(() -> {
              log.error("Unable to compute max aspect createdAt for urn: {}", urn);
              return new RuntimeException(String.format("Unable to compute max aspect createdAt for urn %s", urn));
            })
            .toString();
        return new TestQueryResponse(Collections.singletonList(lastUpdatedTime));
      default:
        log.error("Unknown query {}", queryName);
        throw new RuntimeException(String.format("Unknown query %s", queryName));
    }
  }
}

