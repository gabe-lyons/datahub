package com.linkedin.metadata.service;

import com.datahub.authentication.Authentication;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.linkedin.incident.IncidentInfo;
import com.linkedin.common.IncidentsSummary;
import com.linkedin.common.UrnArray;
import com.linkedin.common.urn.Urn;
import com.linkedin.common.urn.UrnUtils;
import com.linkedin.entity.Aspect;
import com.linkedin.entity.EntityResponse;
import com.linkedin.entity.EnvelopedAspect;
import com.linkedin.entity.EnvelopedAspectMap;
import com.linkedin.entity.client.EntityClient;
import com.linkedin.events.metadata.ChangeType;
import com.linkedin.metadata.Constants;
import com.linkedin.metadata.utils.GenericRecordUtils;
import com.linkedin.mxe.MetadataChangeProposal;
import java.util.Collections;
import org.mockito.Mockito;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;
import org.testng.Assert;
import org.testng.annotations.Test;

import static com.linkedin.metadata.Constants.*;


public class IncidentServiceTest {

  private static final Urn TEST_INCIDENT_URN = UrnUtils.getUrn("urn:li:incident:test");
  private static final Urn TEST_NON_EXISTENT_INCIDENT_URN = UrnUtils.getUrn("urn:li:incident:test-non-existant");
  private static final Urn TEST_DATASET_URN = UrnUtils.getUrn("urn:li:dataset:(urn:li:dataPlatform:hive,name,PROD)");
  private static final Urn TEST_NON_EXISTENT_DATASET_URN = UrnUtils.getUrn("urn:li:dataset:(urn:li:dataPlatform:hive,non-existant,PROD)");

  @Test
  private void testGetIncidentInfo() throws Exception {
    final EntityClient mockClient = createMockEntityClient();
    final IncidentService service = new IncidentService(
        mockClient,
        Mockito.mock(Authentication.class));

    // Case 1: Info exists
    IncidentInfo info = service.getIncidentInfo(TEST_INCIDENT_URN);
    Assert.assertEquals(info, mockIncidentInfo());
    Mockito.verify(mockClient, Mockito.times(1)).getV2(
        Mockito.eq(Constants.INCIDENT_ENTITY_NAME),
        Mockito.eq(TEST_INCIDENT_URN),
        Mockito.eq(ImmutableSet.of(Constants.INCIDENT_INFO_ASPECT_NAME)),
        Mockito.any(Authentication.class)
    );

    // Case 2: Info does not exist
    info = service.getIncidentInfo(TEST_NON_EXISTENT_INCIDENT_URN);
    Assert.assertNull(info);
    Mockito.verify(mockClient, Mockito.times(1)).getV2(
        Mockito.eq(Constants.INCIDENT_ENTITY_NAME),
        Mockito.eq(TEST_NON_EXISTENT_INCIDENT_URN),
        Mockito.eq(ImmutableSet.of(Constants.INCIDENT_INFO_ASPECT_NAME)),
        Mockito.any(Authentication.class)
    );
  }

  @Test
  private void testGetIncidentsSummary() throws Exception {
    final EntityClient mockClient = createMockEntityClient();
    final IncidentService service = new IncidentService(
        mockClient,
        Mockito.mock(Authentication.class));

    // Case 1: Summary exists
    IncidentsSummary summary = service.getIncidentsSummary(TEST_DATASET_URN);
    Assert.assertEquals(summary, mockIncidentSummary());
    Mockito.verify(mockClient, Mockito.times(1)).getV2(
        Mockito.eq(DATASET_ENTITY_NAME),
        Mockito.eq(TEST_DATASET_URN),
        Mockito.eq(ImmutableSet.of(INCIDENTS_SUMMARY_ASPECT_NAME)),
        Mockito.any(Authentication.class)
    );

    // Case 2: Summary does not exist
    summary = service.getIncidentsSummary(TEST_NON_EXISTENT_DATASET_URN);
    Assert.assertNull(summary);
    Mockito.verify(mockClient, Mockito.times(1)).getV2(
        Mockito.eq(Constants.DATASET_ENTITY_NAME),
        Mockito.eq(TEST_DATASET_URN),
        Mockito.eq(ImmutableSet.of(Constants.INCIDENTS_SUMMARY_ASPECT_NAME)),
        Mockito.any(Authentication.class)
    );
  }

  @Test
  private void testUpdateIncidentsSummary() throws Exception {
    final EntityClient mockClient = createMockEntityClient();
    final IncidentService service = new IncidentService(
        mockClient,
        Mockito.mock(Authentication.class));
    service.updateIncidentsSummary(TEST_DATASET_URN, mockIncidentSummary());
    Mockito.verify(mockClient, Mockito.times(1)).ingestProposal(
        Mockito.eq(mockIncidentSummaryMcp()),
        Mockito.any(Authentication.class),
        Mockito.eq(false)
    );
  }

  private static EntityClient createMockEntityClient() throws Exception {
    EntityClient mockClient = Mockito.mock(EntityClient.class);

    // Init for incident info
    Mockito.when(mockClient.getV2(
        Mockito.eq(Constants.INCIDENT_ENTITY_NAME),
        Mockito.eq(TEST_INCIDENT_URN),
        Mockito.eq(ImmutableSet.of(Constants.INCIDENT_INFO_ASPECT_NAME)),
        Mockito.any(Authentication.class))
    ).thenReturn(
        new EntityResponse()
            .setUrn(TEST_INCIDENT_URN)
            .setEntityName(INCIDENT_ENTITY_NAME)
            .setAspects(new EnvelopedAspectMap(ImmutableMap.of(
                INCIDENT_INFO_ASPECT_NAME,
                new EnvelopedAspect().setValue(new Aspect(mockIncidentInfo().data()))
            ))));
    Mockito.when(mockClient.getV2(
        Mockito.eq(Constants.INCIDENT_ENTITY_NAME),
        Mockito.eq(TEST_NON_EXISTENT_INCIDENT_URN),
        Mockito.eq(ImmutableSet.of(Constants.INCIDENT_INFO_ASPECT_NAME)),
        Mockito.any(Authentication.class))
    ).thenReturn(
        new EntityResponse()
            .setUrn(TEST_NON_EXISTENT_INCIDENT_URN)
            .setEntityName(INCIDENT_ENTITY_NAME)
            .setAspects(new EnvelopedAspectMap(Collections.emptyMap())));

    // Init for incidents summary
    Mockito.when(mockClient.getV2(
        Mockito.eq(DATASET_ENTITY_NAME),
        Mockito.eq(TEST_DATASET_URN),
        Mockito.eq(ImmutableSet.of(INCIDENTS_SUMMARY_ASPECT_NAME)),
        Mockito.any(Authentication.class))
    ).thenReturn(
        new EntityResponse()
            .setUrn(TEST_DATASET_URN)
            .setEntityName(DATASET_ENTITY_NAME)
            .setAspects(new EnvelopedAspectMap(ImmutableMap.of(
                INCIDENTS_SUMMARY_ASPECT_NAME,
                new EnvelopedAspect().setValue(new Aspect(mockIncidentSummary().data()))
            ))));
    Mockito.when(mockClient.getV2(
        Mockito.eq(DATASET_ENTITY_NAME),
        Mockito.eq(TEST_NON_EXISTENT_DATASET_URN),
        Mockito.eq(ImmutableSet.of(INCIDENTS_SUMMARY_ASPECT_NAME)),
        Mockito.any(Authentication.class))
    ).thenReturn(
        new EntityResponse()
            .setUrn(TEST_NON_EXISTENT_DATASET_URN)
            .setEntityName(DATASET_ENTITY_NAME)
            .setAspects(new EnvelopedAspectMap(Collections.emptyMap())));

    // Init for update summary
    Mockito.when(mockClient.ingestProposal(
        Mockito.eq(mockIncidentSummaryMcp()),
        Mockito.any(Authentication.class),
        Mockito.eq(false))).thenReturn(TEST_DATASET_URN.toString());

    return mockClient;
  }

  private static IncidentInfo mockIncidentInfo() throws Exception {
    final IncidentInfo info = new IncidentInfo();
    info.setEntities(new UrnArray(ImmutableList.of(TEST_DATASET_URN)));
    return info;
  }

  private static IncidentsSummary mockIncidentSummary() throws Exception {
    final IncidentsSummary summary = new IncidentsSummary();
    summary.setResolvedIncidents(new UrnArray(ImmutableList.of(TEST_INCIDENT_URN)));
    return summary;
  }

  private static MetadataChangeProposal mockIncidentSummaryMcp() throws Exception {

    final MetadataChangeProposal mcp = new MetadataChangeProposal();
    mcp.setEntityUrn(TEST_DATASET_URN);
    mcp.setEntityType(DATASET_ENTITY_NAME);
    mcp.setAspectName(INCIDENTS_SUMMARY_ASPECT_NAME);
    mcp.setChangeType(ChangeType.UPSERT);
    mcp.setAspect(GenericRecordUtils.serializeAspect(mockIncidentSummary()));

    return mcp;
  }
}
