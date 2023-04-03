package com.linkedin.metadata.kafka.hook.incident;

import com.google.common.collect.ImmutableList;
import com.linkedin.common.IncidentsSummary;
import com.linkedin.common.Status;
import com.linkedin.common.UrnArray;
import com.linkedin.common.urn.Urn;
import com.linkedin.common.urn.UrnUtils;
import com.linkedin.data.template.RecordTemplate;
import com.linkedin.events.metadata.ChangeType;
import com.linkedin.incident.IncidentInfo;
import com.linkedin.incident.IncidentState;
import com.linkedin.incident.IncidentStatus;
import com.linkedin.incident.IncidentType;
import com.linkedin.metadata.models.registry.ConfigEntityRegistry;
import com.linkedin.metadata.models.registry.EntityRegistry;
import com.linkedin.metadata.service.IncidentService;
import com.linkedin.metadata.utils.GenericRecordUtils;
import com.linkedin.mxe.MetadataChangeLog;
import java.util.List;
import org.mockito.Mockito;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static com.linkedin.metadata.Constants.*;


public class IncidentsSummaryHookTest {
  private static final EntityRegistry ENTITY_REGISTRY = new ConfigEntityRegistry(
      IncidentsSummaryHookTest.class.getClassLoader().getResourceAsStream("test-entity-registry.yml"));
  private static final Urn TEST_INCIDENT_URN = UrnUtils.getUrn("urn:li:incident:test");
  private static final Urn TEST_DATASET_URN = UrnUtils.getUrn("urn:li:dataset:(urn:li:dataPlatform:hive,name,PROD)");
  private static final Urn TEST_DATASET_2_URN = UrnUtils.getUrn("urn:li:dataset:(urn:li:dataPlatform:hive,name2,PROD)");

  @Test
  public void testInvokeNotEnabled() throws Exception {
    IncidentInfo incidentInfo = mockIncidentInfo(ImmutableList.of(TEST_DATASET_URN, TEST_DATASET_2_URN), IncidentState.ACTIVE);
    IncidentService service = mockIncidentService(new IncidentsSummary(), incidentInfo);
    IncidentsSummaryHook hook = new IncidentsSummaryHook(ENTITY_REGISTRY, service);
    hook.setEnabled(false);
    final MetadataChangeLog event = buildMetadataChangeLog(
        TEST_INCIDENT_URN,
        INCIDENT_INFO_ASPECT_NAME,
        ChangeType.UPSERT,
        incidentInfo);
    hook.invoke(event);
    Mockito.verify(service, Mockito.times(0)).getIncidentInfo(Mockito.any());
  }

  @Test
  public void testInvokeNotEligibleChange() throws Exception {
    IncidentInfo info = mockIncidentInfo(ImmutableList.of(TEST_DATASET_URN, TEST_DATASET_2_URN), IncidentState.ACTIVE);
    IncidentService service = mockIncidentService(new IncidentsSummary(), info);
    IncidentsSummaryHook hook = new IncidentsSummaryHook(ENTITY_REGISTRY, service);

    // Case 1: Incorrect aspect
    MetadataChangeLog event = buildMetadataChangeLog(
        TEST_INCIDENT_URN,
        INCIDENT_KEY_ASPECT_NAME,
        ChangeType.UPSERT,
        new IncidentInfo());
    hook.invoke(event);
    Mockito.verify(service, Mockito.times(0)).getIncidentInfo(Mockito.any());

    // Case 2: Run Event But Delete
    event = buildMetadataChangeLog(
        TEST_INCIDENT_URN,
        INCIDENT_INFO_ASPECT_NAME,
        ChangeType.DELETE,
        info);
    hook.invoke(event);
    Mockito.verify(service, Mockito.times(0)).getIncidentInfo(Mockito.any());
  }

  @DataProvider(name = "incidentsSummaryProvider")
  static Object[][] incidentsSummaryProvider() {
    return new Object[][] {
        new Object[] {
            null
        },
        new Object[] {
            new IncidentsSummary()
                .setActiveIncidents(new UrnArray())
                .setResolvedIncidents(new UrnArray())
        }
    };
  }

  @Test(dataProvider = "incidentsSummaryProvider")
  public void testInvokeIncidentRunEventActive(IncidentsSummary summary) throws Exception {
    IncidentInfo info = mockIncidentInfo(ImmutableList.of(TEST_DATASET_URN, TEST_DATASET_2_URN), IncidentState.ACTIVE);
    IncidentService service = mockIncidentService(summary, info);
    IncidentsSummaryHook hook = new IncidentsSummaryHook(ENTITY_REGISTRY, service);
    final MetadataChangeLog event = buildMetadataChangeLog(
        TEST_INCIDENT_URN,
        INCIDENT_INFO_ASPECT_NAME,
        ChangeType.UPSERT,
        info);
    hook.invoke(event);
    Mockito.verify(service, Mockito.times(1)).getIncidentInfo(Mockito.eq(TEST_INCIDENT_URN));
    Mockito.verify(service, Mockito.times(1)).getIncidentsSummary(Mockito.eq(TEST_DATASET_URN));
    Mockito.verify(service, Mockito.times(1)).getIncidentsSummary(Mockito.eq(TEST_DATASET_2_URN));

    IncidentsSummary expectedSummary = new IncidentsSummary();
    expectedSummary.setActiveIncidents(new UrnArray(ImmutableList.of(TEST_INCIDENT_URN)));
    expectedSummary.setResolvedIncidents(new UrnArray());

    // Ensure we ingested a new aspect.
    Mockito.verify(service, Mockito.times(1)).updateIncidentsSummary(
        Mockito.eq(TEST_DATASET_URN),
        Mockito.eq(expectedSummary));
    Mockito.verify(service, Mockito.times(1)).updateIncidentsSummary(
        Mockito.eq(TEST_DATASET_2_URN),
        Mockito.eq(expectedSummary));
  }


  @Test(dataProvider = "incidentsSummaryProvider")
  public void testInvokeIncidentRunEventResolved(IncidentsSummary summary) throws Exception {
    IncidentInfo info = mockIncidentInfo(
        ImmutableList.of(TEST_DATASET_URN, TEST_DATASET_2_URN),
        IncidentState.RESOLVED);
    IncidentService service = mockIncidentService(summary, info);
    IncidentsSummaryHook hook = new IncidentsSummaryHook(ENTITY_REGISTRY, service);
    final MetadataChangeLog event = buildMetadataChangeLog(
        TEST_INCIDENT_URN,
        INCIDENT_INFO_ASPECT_NAME,
        ChangeType.UPSERT,
        info);
    hook.invoke(event);
    Mockito.verify(service, Mockito.times(1)).getIncidentInfo(Mockito.eq(TEST_INCIDENT_URN));
    Mockito.verify(service, Mockito.times(1)).getIncidentsSummary(Mockito.eq(TEST_DATASET_URN));
    Mockito.verify(service, Mockito.times(1)).getIncidentsSummary(Mockito.eq(TEST_DATASET_2_URN));

    IncidentsSummary expectedSummary = new IncidentsSummary();
    expectedSummary.setResolvedIncidents(new UrnArray(ImmutableList.of(TEST_INCIDENT_URN)));
    expectedSummary.setActiveIncidents(new UrnArray());

    // Ensure we ingested a new aspect.
    Mockito.verify(service, Mockito.times(1)).updateIncidentsSummary(
        Mockito.eq(TEST_DATASET_URN),
        Mockito.eq(expectedSummary));
    Mockito.verify(service, Mockito.times(1)).updateIncidentsSummary(
        Mockito.eq(TEST_DATASET_2_URN),
        Mockito.eq(expectedSummary));
  }


  @Test(dataProvider = "incidentsSummaryProvider")
  public void testInvokeIncidentSoftDeleted(IncidentsSummary summary) throws Exception {
    IncidentInfo info = mockIncidentInfo(
        ImmutableList.of(TEST_DATASET_URN, TEST_DATASET_2_URN),
        IncidentState.RESOLVED);
    IncidentService service = mockIncidentService(summary, info);
    IncidentsSummaryHook hook = new IncidentsSummaryHook(ENTITY_REGISTRY, service);
    final MetadataChangeLog event = buildMetadataChangeLog(
        TEST_INCIDENT_URN,
        STATUS_ASPECT_NAME,
        ChangeType.UPSERT,
        mockIncidentSoftDeleted());
    hook.invoke(event);

    Mockito.verify(service, Mockito.times(1)).getIncidentInfo(Mockito.eq(TEST_INCIDENT_URN));
    Mockito.verify(service, Mockito.times(1)).getIncidentsSummary(Mockito.eq(TEST_DATASET_URN));
    Mockito.verify(service, Mockito.times(1)).getIncidentsSummary(Mockito.eq(TEST_DATASET_2_URN));

    IncidentsSummary expectedSummary = new IncidentsSummary();
    expectedSummary.setActiveIncidents(new UrnArray());
    expectedSummary.setResolvedIncidents(new UrnArray());

    // Ensure we ingested a new aspect.
    Mockito.verify(service, Mockito.times(1)).updateIncidentsSummary(
        Mockito.eq(TEST_DATASET_URN),
        Mockito.eq(expectedSummary));
    Mockito.verify(service, Mockito.times(1)).updateIncidentsSummary(
        Mockito.eq(TEST_DATASET_2_URN),
        Mockito.eq(expectedSummary));
  }

  private IncidentInfo mockIncidentInfo(final List<Urn> entityUrns, final IncidentState state) {
    IncidentInfo event = new IncidentInfo();
    event.setEntities(new UrnArray(entityUrns));
    event.setType(IncidentType.OPERATIONAL);
    event.setStatus(new IncidentStatus().setState(state));
    return event;
  }

  private Status mockIncidentSoftDeleted() {
    Status status = new Status();
    status.setRemoved(true);
    return status;
  }

  private IncidentService mockIncidentService(IncidentsSummary summary, IncidentInfo info) {
    IncidentService mockService = Mockito.mock(IncidentService.class);

    Mockito.when(mockService.getIncidentInfo(TEST_INCIDENT_URN))
      .thenReturn(info);

    Mockito.when(mockService.getIncidentsSummary(TEST_DATASET_URN)).thenReturn(summary);
    return mockService;
  }

  private MetadataChangeLog buildMetadataChangeLog(Urn urn, String aspectName, ChangeType changeType, RecordTemplate aspect) throws Exception {
    MetadataChangeLog event = new MetadataChangeLog();
    event.setEntityUrn(urn);
    event.setEntityType(INCIDENT_ENTITY_NAME);
    event.setAspectName(aspectName);
    event.setChangeType(changeType);
    event.setAspect(GenericRecordUtils.serializeAspect(aspect));
    return event;
  }
}