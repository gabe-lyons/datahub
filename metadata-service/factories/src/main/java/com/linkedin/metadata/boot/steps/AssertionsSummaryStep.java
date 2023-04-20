package com.linkedin.metadata.boot.steps;

import com.linkedin.assertion.AssertionInfo;
import com.linkedin.assertion.AssertionResult;
import com.linkedin.assertion.AssertionResultType;
import com.linkedin.assertion.AssertionRunEvent;
import com.linkedin.common.AssertionsSummary;
import com.linkedin.common.urn.Urn;
import com.linkedin.metadata.Constants;
import com.linkedin.metadata.aspect.EnvelopedAspect;
import com.linkedin.metadata.boot.BootstrapStep;
import com.linkedin.metadata.boot.UpgradeStep;
import com.linkedin.metadata.entity.EntityService;
import com.linkedin.metadata.search.EntitySearchService;
import com.linkedin.metadata.search.ScrollResult;
import com.linkedin.metadata.search.SearchEntity;
import com.linkedin.metadata.service.AssertionService;
import com.linkedin.metadata.service.AssertionsSummaryUtils;
import com.linkedin.metadata.timeseries.TimeseriesAspectService;
import com.linkedin.metadata.utils.GenericRecordUtils;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Upgrade step that creates and/or updates AssertionsSummary aspects for datasets that the assertions are on.
 * This allows us to search and query for datasets by passing/failing assertions.
 */
@Slf4j
public class AssertionsSummaryStep extends UpgradeStep {
  private static final String VERSION = "1";
  private static final String UPGRADE_ID = "assertions-summary";
  private static final Integer BATCH_SIZE = 1000;

  private final EntitySearchService _entitySearchService;
  private final AssertionService _assertionService;
  private final TimeseriesAspectService _timeseriesAspectService;

  public AssertionsSummaryStep(
      EntityService entityService,
      EntitySearchService entitySearchService,
      AssertionService assertionService,
      TimeseriesAspectService timeseriesAspectService
  ) {
    super(entityService, VERSION, UPGRADE_ID);
    _entitySearchService = entitySearchService;
    _assertionService = assertionService;
    _timeseriesAspectService = timeseriesAspectService;
  }

  @Nonnull
  @Override
  public BootstrapStep.ExecutionMode getExecutionMode() {
    return BootstrapStep.ExecutionMode.ASYNC;
  }

  @Override
  public void upgrade() throws Exception {

    int batch = 1;
    ScrollResult scrollResult = _entitySearchService.scroll(Constants.ASSERTION_ENTITY_NAME, null, null, BATCH_SIZE, null, "1m");

    while (scrollResult.getEntities().size() > 0) {
      List<Urn> assertionsInBatch =  scrollResult.getEntities().stream().map(SearchEntity::getEntity).collect(Collectors.toList());

      try {
        batchAddAssertionsSummary(assertionsInBatch);
      } catch (Exception e) {
        log.error("Error while processing batch {} of assertions", batch, e);
      }
      batch++;
      scrollResult =
          _entitySearchService.scroll(Constants.ASSERTION_ENTITY_NAME, null, null, BATCH_SIZE, scrollResult.getScrollId(), "1m");
    }
  }

  private void batchAddAssertionsSummary(@Nonnull final List<Urn> assertionUrns) {
    for (Urn assertionUrn : assertionUrns) {
      updateAssertionsSummary(assertionUrn);
    }
  }

  private void updateAssertionsSummary(@Nonnull final Urn assertionUrn) {
    // 1. Fetch assertion info to get the dataset urn
    AssertionInfo assertionInfo = _assertionService.getAssertionInfo(assertionUrn);

    if (assertionInfo == null) {
      log.warn(
          String.format(
              "Failed to find assertionInfo aspect for assertion with urn %s. Skipping updating assertion summary for related assertions!",
              assertionUrn));
      return;
    }

    if (!assertionInfo.hasDatasetAssertion()) {
      log.warn(String.format("AssertionInfo does not have datasetAssertion. Skipping upgrade for assertion %s", assertionUrn));
      return;
    }

    Urn datasetUrn = assertionInfo.getDatasetAssertion().getDataset();

    // 2. get most recent assertion run event
    List<EnvelopedAspect> mostRecentRunEvents = _timeseriesAspectService.getAspectValues(
        assertionUrn,
        Constants.ASSERTION_ENTITY_NAME,
        Constants.ASSERTION_RUN_EVENT_ASPECT_NAME,
        null,
        null,
        1,
        null,
        null);

    // we're only fetching 1 and only fetching the latest value, so findFirst
    Optional<EnvelopedAspect> runEvent = mostRecentRunEvents.stream().findFirst();

    // 3. convert runEvent to aspect value and add the assertion to the summary aspect
    if (runEvent.isPresent()) {
      AssertionRunEvent assertionRunEvent = GenericRecordUtils.deserializeAspect(runEvent.get().getAspect().getValue(),
          runEvent.get().getAspect().getContentType(), AssertionRunEvent.class);
      if (assertionRunEvent.hasResult()) {
        addAssertionToSummary(assertionUrn, datasetUrn, assertionRunEvent.getResult());
      }
    }
  }

  /**
   * Adds an assertion to the AssertionSummary aspect for a related entity.
   * This is used to search for entity by active and resolved assertions.
   */
  private void addAssertionToSummary(@Nonnull final Urn assertionUrn, @Nonnull final Urn entityUrn, @Nonnull final AssertionResult result) {
    // 1. Fetch the latest assertion summary for the entity
    AssertionsSummary summary = getAssertionsSummary(entityUrn);

    // 2. Add the assertion to passing or failing assertions
    if (AssertionResultType.SUCCESS.equals(result.getType())) {
      // First, ensure this isn't in failing anymore.
      AssertionsSummaryUtils.removeAssertionFromFailingSummary(assertionUrn, summary);
      // Then, add to passing.
      AssertionsSummaryUtils.addAssertionToPassingSummary(assertionUrn, summary);

    } else if (AssertionResultType.FAILURE.equals(result.getType())) {
      // First, ensure this isn't in passing anymore.
      AssertionsSummaryUtils.removeAssertionFromPassingSummary(assertionUrn, summary);
      // Then, add to failing.
      AssertionsSummaryUtils.addAssertionToFailingSummary(assertionUrn, summary);
    }

    // 3. Emit the change back!
    updateAssertionSummary(entityUrn, summary);
  }

  @Nonnull
  private AssertionsSummary getAssertionsSummary(@Nonnull final Urn entityUrn) {
    AssertionsSummary maybeAssertionsSummary = _assertionService.getAssertionsSummary(entityUrn);
    return maybeAssertionsSummary == null ? new AssertionsSummary() : maybeAssertionsSummary;
  }

  /**
   * Updates the assertions summary for a given entity
   */
  private void updateAssertionSummary(@Nonnull final Urn entityUrn, @Nonnull final AssertionsSummary newSummary) {
    try {
      _assertionService.updateAssertionsSummary(entityUrn, newSummary);
    } catch (Exception e) {
      log.error(
          String.format("Failed to updated assertions summary for entity with urn %s! Skipping updating the summary", entityUrn), e);
    }
  }
}
