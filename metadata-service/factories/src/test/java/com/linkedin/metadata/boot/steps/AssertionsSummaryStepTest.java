package com.linkedin.metadata.boot.steps;

import com.linkedin.assertion.AssertionInfo;
import com.linkedin.assertion.AssertionResult;
import com.linkedin.assertion.AssertionResultType;
import com.linkedin.assertion.AssertionRunEvent;
import com.linkedin.assertion.DatasetAssertionInfo;
import com.linkedin.common.AssertionsSummary;
import com.linkedin.common.UrnArray;
import com.linkedin.common.urn.UrnUtils;
import com.linkedin.metadata.Constants;
import com.linkedin.metadata.aspect.EnvelopedAspect;
import com.linkedin.metadata.entity.EntityService;
import com.linkedin.metadata.search.EntitySearchService;
import com.linkedin.metadata.search.ScrollResult;
import com.linkedin.metadata.search.SearchEntity;
import com.linkedin.metadata.search.SearchEntityArray;
import com.linkedin.metadata.service.AssertionService;
import com.linkedin.metadata.timeseries.TimeseriesAspectService;
import com.linkedin.metadata.utils.GenericRecordUtils;
import org.mockito.Mockito;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

public class AssertionsSummaryStepTest {

  private static final String ASSERTION_URN = "urn:li:assertion:126d8dc8939e0cf9bf0fd03264ad1a06";
  private static final String DATASET_URN = "urn:li:dataset:(urn:li:dataPlatform:hive,SampleHiveDataset,PROD)";
  private static final String SCROLL_ID = "test123";

  @Test
  public void testExecuteAssertionsSummaryStepWithFailingAssertion() throws Exception {
    final EntityService entityService = mock(EntityService.class);
    final EntitySearchService entitySearchService = mock(EntitySearchService.class);
    final AssertionService assertionService = mock(AssertionService.class);
    final TimeseriesAspectService timeseriesAspectService = mock(TimeseriesAspectService.class);
    configureEntitySearchServiceMock(entitySearchService);
    configureAssertionServiceMock(assertionService);
    configureTimeSeriesAspectServiceMock(timeseriesAspectService, AssertionResultType.FAILURE);

    final AssertionsSummaryStep step = new AssertionsSummaryStep(
        entityService,
        entitySearchService,
        assertionService,
        timeseriesAspectService);

    step.execute();

    AssertionsSummary expectedSummary = new AssertionsSummary();
    UrnArray failingAssertions = new UrnArray();
    failingAssertions.add(UrnUtils.getUrn(ASSERTION_URN));
    expectedSummary.setFailingAssertions(failingAssertions);
    expectedSummary.setPassingAssertions(new UrnArray());

    Mockito.verify(assertionService, times(1)).updateAssertionsSummary(
        Mockito.eq(UrnUtils.getUrn(DATASET_URN)),
        Mockito.eq(expectedSummary)
    );
  }

  @Test
  public void testExecuteAssertionsSummaryStepWithPassingAssertion() throws Exception {
    final EntityService entityService = mock(EntityService.class);
    final EntitySearchService entitySearchService = mock(EntitySearchService.class);
    final AssertionService assertionService = mock(AssertionService.class);
    final TimeseriesAspectService timeseriesAspectService = mock(TimeseriesAspectService.class);
    configureEntitySearchServiceMock(entitySearchService);
    configureAssertionServiceMock(assertionService);
    configureTimeSeriesAspectServiceMock(timeseriesAspectService, AssertionResultType.SUCCESS);

    final AssertionsSummaryStep step = new AssertionsSummaryStep(
        entityService,
        entitySearchService,
        assertionService,
        timeseriesAspectService);

    step.execute();

    AssertionsSummary expectedSummary = new AssertionsSummary();
    UrnArray failingAssertions = new UrnArray();
    failingAssertions.add(UrnUtils.getUrn(ASSERTION_URN));
    expectedSummary.setPassingAssertions(failingAssertions);
    expectedSummary.setFailingAssertions(new UrnArray());

    Mockito.verify(assertionService, times(1)).updateAssertionsSummary(
        Mockito.eq(UrnUtils.getUrn(DATASET_URN)),
        Mockito.eq(expectedSummary)
    );
  }

  private static void configureEntitySearchServiceMock(final EntitySearchService mockSearchService) {
    SearchEntity searchEntity = new SearchEntity();
    searchEntity.setEntity(UrnUtils.getUrn(ASSERTION_URN));
    SearchEntityArray searchEntityArray = new SearchEntityArray();
    searchEntityArray.add(searchEntity);
    ScrollResult scrollResult = new ScrollResult();
    scrollResult.setEntities(searchEntityArray);
    scrollResult.setScrollId(SCROLL_ID);

    Mockito.when(mockSearchService.scroll(
        Mockito.eq(Constants.ASSERTION_ENTITY_NAME),
        Mockito.eq(null),
        Mockito.eq(null),
        Mockito.eq(1000),
        Mockito.eq(null),
        Mockito.eq("1m")
    )).thenReturn(scrollResult);

    ScrollResult newScrollResult = new ScrollResult();
    newScrollResult.setEntities(new SearchEntityArray());

    Mockito.when(mockSearchService.scroll(
        Mockito.eq(Constants.ASSERTION_ENTITY_NAME),
        Mockito.eq(null),
        Mockito.eq(null),
        Mockito.eq(1000),
        Mockito.eq(SCROLL_ID),
        Mockito.eq("1m")
    )).thenReturn(newScrollResult);
  }

  private static void configureAssertionServiceMock(final AssertionService mockAssertionService) {
    DatasetAssertionInfo datasetAssertionInfo = new DatasetAssertionInfo();
    datasetAssertionInfo.setDataset(UrnUtils.getUrn(DATASET_URN));
    AssertionInfo assertionInfo = new AssertionInfo();
    assertionInfo.setDatasetAssertion(datasetAssertionInfo);


    Mockito.when(mockAssertionService.getAssertionInfo(
        Mockito.eq(UrnUtils.getUrn(ASSERTION_URN))
    )).thenReturn(assertionInfo);

    Mockito.when(mockAssertionService.getAssertionsSummary(
        Mockito.eq(UrnUtils.getUrn(ASSERTION_URN))
    )).thenReturn(new AssertionsSummary());
  }

  private static void configureTimeSeriesAspectServiceMock(final TimeseriesAspectService timeseriesAspectService, final AssertionResultType resultType) {
    List<EnvelopedAspect> envelopedAspects = new ArrayList<>();
    EnvelopedAspect envelopedAspect = new EnvelopedAspect();
    AssertionRunEvent assertionRunEvent = new AssertionRunEvent();
    AssertionResult assertionResult = new AssertionResult();
    assertionResult.setType(resultType);
    assertionRunEvent.setResult(assertionResult);
    envelopedAspect.setAspect(GenericRecordUtils.serializeAspect(assertionRunEvent));
    envelopedAspects.add(envelopedAspect);

    Mockito.when(timeseriesAspectService.getAspectValues(
        Mockito.eq(UrnUtils.getUrn(ASSERTION_URN)),
        Mockito.eq(Constants.ASSERTION_ENTITY_NAME),
        Mockito.eq(Constants.ASSERTION_RUN_EVENT_ASPECT_NAME),
        Mockito.eq(null),
        Mockito.eq(null),
        Mockito.eq(1),
        Mockito.eq(true),
        Mockito.eq(null)
    )).thenReturn(envelopedAspects);
  }
}
