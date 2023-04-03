package com.linkedin.metadata.service;

import com.datahub.authentication.Authentication;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.linkedin.assertion.AssertionInfo;
import com.linkedin.assertion.AssertionType;
import com.linkedin.assertion.DatasetAssertionInfo;
import com.linkedin.common.AssertionsSummary;
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


public class AssertionServiceTest {

  private static final Urn TEST_ASSERTION_URN = UrnUtils.getUrn("urn:li:assertion:test");
  private static final Urn TEST_NON_EXISTENT_ASSERTION_URN = UrnUtils.getUrn("urn:li:assertion:test-non-existant");
  private static final Urn TEST_DATASET_URN = UrnUtils.getUrn("urn:li:dataset:(urn:li:dataPlatform:hive,name,PROD)");
  private static final Urn TEST_NON_EXISTENT_DATASET_URN = UrnUtils.getUrn("urn:li:dataset:(urn:li:dataPlatform:hive,non-existant,PROD)");

  @Test
  private void testGetAssertionInfo() throws Exception {
    final EntityClient mockClient = createMockEntityClient();
    final AssertionService service = new AssertionService(
        mockClient,
        Mockito.mock(Authentication.class));

    // Case 1: Info exists
    AssertionInfo info = service.getAssertionInfo(TEST_ASSERTION_URN);
    Assert.assertEquals(info, mockAssertionInfo());
    Mockito.verify(mockClient, Mockito.times(1)).getV2(
        Mockito.eq(Constants.ASSERTION_ENTITY_NAME),
        Mockito.eq(TEST_ASSERTION_URN),
        Mockito.eq(ImmutableSet.of(Constants.ASSERTION_INFO_ASPECT_NAME)),
        Mockito.any(Authentication.class)
    );

    // Case 2: Info does not exist
    info = service.getAssertionInfo(TEST_NON_EXISTENT_ASSERTION_URN);
    Assert.assertNull(info);
    Mockito.verify(mockClient, Mockito.times(1)).getV2(
        Mockito.eq(Constants.ASSERTION_ENTITY_NAME),
        Mockito.eq(TEST_NON_EXISTENT_ASSERTION_URN),
        Mockito.eq(ImmutableSet.of(Constants.ASSERTION_INFO_ASPECT_NAME)),
        Mockito.any(Authentication.class)
    );
  }

  @Test
  private void testGetAssertionsSummary() throws Exception {
    final EntityClient mockClient = createMockEntityClient();
    final AssertionService service = new AssertionService(
        mockClient,
        Mockito.mock(Authentication.class));

    // Case 1: Summary exists
    AssertionsSummary summary = service.getAssertionsSummary(TEST_DATASET_URN);
    Assert.assertEquals(summary, mockAssertionSummary());
    Mockito.verify(mockClient, Mockito.times(1)).getV2(
        Mockito.eq(DATASET_ENTITY_NAME),
        Mockito.eq(TEST_DATASET_URN),
        Mockito.eq(ImmutableSet.of(ASSERTIONS_SUMMARY_ASPECT_NAME)),
        Mockito.any(Authentication.class)
    );

    // Case 2: Summary does not exist
    summary = service.getAssertionsSummary(TEST_NON_EXISTENT_DATASET_URN);
    Assert.assertNull(summary);
    Mockito.verify(mockClient, Mockito.times(1)).getV2(
        Mockito.eq(Constants.DATASET_ENTITY_NAME),
        Mockito.eq(TEST_DATASET_URN),
        Mockito.eq(ImmutableSet.of(Constants.ASSERTIONS_SUMMARY_ASPECT_NAME)),
        Mockito.any(Authentication.class)
    );
  }

  @Test
  private void testUpdateAssertionsSummary() throws Exception {
    final EntityClient mockClient = createMockEntityClient();
    final AssertionService service = new AssertionService(
        mockClient,
        Mockito.mock(Authentication.class));
    service.updateAssertionsSummary(TEST_DATASET_URN, mockAssertionSummary());
    Mockito.verify(mockClient, Mockito.times(1)).ingestProposal(
        Mockito.eq(mockAssertionSummaryMcp()),
        Mockito.any(Authentication.class),
        Mockito.eq(false)
    );
  }

  private static EntityClient createMockEntityClient() throws Exception {
    EntityClient mockClient = Mockito.mock(EntityClient.class);

    // Init for assertion info
    Mockito.when(mockClient.getV2(
        Mockito.eq(Constants.ASSERTION_ENTITY_NAME),
        Mockito.eq(TEST_ASSERTION_URN),
        Mockito.eq(ImmutableSet.of(Constants.ASSERTION_INFO_ASPECT_NAME)),
        Mockito.any(Authentication.class))
    ).thenReturn(
            new EntityResponse()
                .setUrn(TEST_ASSERTION_URN)
                .setEntityName(ASSERTION_ENTITY_NAME)
                .setAspects(new EnvelopedAspectMap(ImmutableMap.of(
                    ASSERTION_INFO_ASPECT_NAME,
                    new EnvelopedAspect().setValue(new Aspect(mockAssertionInfo().data()))
                ))));
    Mockito.when(mockClient.getV2(
        Mockito.eq(Constants.ASSERTION_ENTITY_NAME),
        Mockito.eq(TEST_NON_EXISTENT_ASSERTION_URN),
        Mockito.eq(ImmutableSet.of(Constants.ASSERTION_INFO_ASPECT_NAME)),
        Mockito.any(Authentication.class))
    ).thenReturn(
        new EntityResponse()
            .setUrn(TEST_NON_EXISTENT_ASSERTION_URN)
            .setEntityName(ASSERTION_ENTITY_NAME)
            .setAspects(new EnvelopedAspectMap(Collections.emptyMap())));

    // Init for assertions summary
    Mockito.when(mockClient.getV2(
        Mockito.eq(DATASET_ENTITY_NAME),
        Mockito.eq(TEST_DATASET_URN),
        Mockito.eq(ImmutableSet.of(ASSERTIONS_SUMMARY_ASPECT_NAME)),
        Mockito.any(Authentication.class))
    ).thenReturn(
        new EntityResponse()
            .setUrn(TEST_DATASET_URN)
            .setEntityName(DATASET_ENTITY_NAME)
            .setAspects(new EnvelopedAspectMap(ImmutableMap.of(
                ASSERTIONS_SUMMARY_ASPECT_NAME,
                new EnvelopedAspect().setValue(new Aspect(mockAssertionSummary().data()))
            ))));
    Mockito.when(mockClient.getV2(
        Mockito.eq(DATASET_ENTITY_NAME),
        Mockito.eq(TEST_NON_EXISTENT_DATASET_URN),
        Mockito.eq(ImmutableSet.of(ASSERTIONS_SUMMARY_ASPECT_NAME)),
        Mockito.any(Authentication.class))
    ).thenReturn(
        new EntityResponse()
            .setUrn(TEST_NON_EXISTENT_DATASET_URN)
            .setEntityName(DATASET_ENTITY_NAME)
            .setAspects(new EnvelopedAspectMap(Collections.emptyMap())));

    // Init for update summary
    Mockito.when(mockClient.ingestProposal(
        Mockito.eq(mockAssertionSummaryMcp()),
        Mockito.any(Authentication.class),
        Mockito.eq(false))).thenReturn(TEST_DATASET_URN.toString());

    return mockClient;
  }

  private static AssertionInfo mockAssertionInfo() throws Exception {
    final AssertionInfo info = new AssertionInfo();
    info.setType(AssertionType.DATASET);
    info.setDatasetAssertion(new DatasetAssertionInfo()
      .setDataset(TEST_DATASET_URN)
    );
    return info;
  }

  private static AssertionsSummary mockAssertionSummary() throws Exception {
    final AssertionsSummary summary = new AssertionsSummary();
    summary.setFailingAssertions(new UrnArray(ImmutableList.of(TEST_ASSERTION_URN)));
    return summary;
  }

  private static MetadataChangeProposal mockAssertionSummaryMcp() throws Exception {

    final MetadataChangeProposal mcp = new MetadataChangeProposal();
    mcp.setEntityUrn(TEST_DATASET_URN);
    mcp.setEntityType(DATASET_ENTITY_NAME);
    mcp.setAspectName(ASSERTIONS_SUMMARY_ASPECT_NAME);
    mcp.setChangeType(ChangeType.UPSERT);
    mcp.setAspect(GenericRecordUtils.serializeAspect(mockAssertionSummary()));

    return mcp;
  }
}
