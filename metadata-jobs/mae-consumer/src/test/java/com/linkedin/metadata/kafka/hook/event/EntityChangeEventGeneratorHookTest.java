package com.linkedin.metadata.kafka.hook.event;

import com.datahub.authentication.Authentication;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.linkedin.actionrequest.ActionRequestInfo;
import com.linkedin.actionrequest.ActionRequestParams;
import com.linkedin.actionrequest.ActionRequestStatus;
import com.linkedin.actionrequest.CreateGlossaryNodeProposal;
import com.linkedin.actionrequest.DescriptionProposal;
import com.linkedin.actionrequest.GlossaryTermProposal;
import com.linkedin.actionrequest.TagProposal;
import com.linkedin.common.AuditStamp;
import com.linkedin.common.Deprecation;
import com.linkedin.common.FabricType;
import com.linkedin.common.GlobalTags;
import com.linkedin.common.GlossaryTermAssociation;
import com.linkedin.common.GlossaryTermAssociationArray;
import com.linkedin.common.GlossaryTerms;
import com.linkedin.common.Owner;
import com.linkedin.common.OwnerArray;
import com.linkedin.common.Ownership;
import com.linkedin.common.OwnershipType;
import com.linkedin.common.Status;
import com.linkedin.common.TagAssociation;
import com.linkedin.common.TagAssociationArray;
import com.linkedin.common.UrnArray;
import com.linkedin.common.urn.GlossaryTermUrn;
import com.linkedin.common.urn.TagUrn;
import com.linkedin.common.urn.Urn;
import com.linkedin.data.DataMap;
import com.linkedin.data.template.RecordTemplate;
import com.linkedin.dataset.DatasetProperties;
import com.linkedin.domain.Domains;
import com.linkedin.entity.client.EntityClient;
import com.linkedin.entity.client.RestliEntityClient;
import com.linkedin.events.metadata.ChangeType;
import com.linkedin.metadata.key.DatasetKey;
import com.linkedin.metadata.models.AspectSpec;
import com.linkedin.metadata.models.EntitySpec;
import com.linkedin.metadata.models.registry.EntityRegistry;
import com.linkedin.metadata.timeline.data.ChangeCategory;
import com.linkedin.metadata.timeline.data.ChangeOperation;
import com.linkedin.metadata.timeline.differ.ActionRequestInfoDiffer;
import com.linkedin.metadata.timeline.differ.ActionRequestStatusDiffer;
import com.linkedin.metadata.timeline.differ.AspectDifferRegistry;
import com.linkedin.metadata.timeline.differ.DeprecationDiffer;
import com.linkedin.metadata.timeline.differ.EntityKeyDiffer;
import com.linkedin.metadata.timeline.differ.GlobalTagsDiffer;
import com.linkedin.metadata.timeline.differ.GlossaryTermsDiffer;
import com.linkedin.metadata.timeline.differ.OwnershipDiffer;
import com.linkedin.metadata.timeline.differ.SingleDomainDiffer;
import com.linkedin.metadata.timeline.differ.StatusDiffer;
import com.linkedin.metadata.utils.GenericRecordUtils;
import com.linkedin.mxe.MetadataChangeLog;
import com.linkedin.mxe.PlatformEvent;
import com.linkedin.mxe.PlatformEventHeader;
import com.linkedin.platform.event.v1.EntityChangeEvent;
import com.linkedin.platform.event.v1.Parameters;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static com.linkedin.metadata.AcrylConstants.*;
import static com.linkedin.metadata.Constants.*;
import static org.mockito.ArgumentMatchers.*;


/**
 * Tests the {@link EntityChangeEventGeneratorHook}.
 *
 * TODO: Include Schema Field Tests, description update tests.
 */
public class EntityChangeEventGeneratorHookTest {
  private static final long EVENT_TIME = 123L;

  private static final String TEST_DATASET_URN = "urn:li:dataset:(urn:li:dataPlatform:kafka,SampleDataset,PROD)";
  private static final String TEST_ACTOR_URN = "urn:li:corpuser:test";

  // SaaS only
  private static final String TEST_ACTION_REQUEST_URN = "urn:li:actionRequest:123";
  private static final String TEST_GLOSSARY_TERM_URN = "urn:li:glossaryTerm:123";
  private static final String TEST_TAG_URN = "urn:li:tag:123";
  private static final String TEST_GROUP_URN = "urn:li:corpGroup:123";
  private static final String TEST_PARENT_NODE_URN = "urn:li:glossaryNode:123";
  private static final String TEST_GLOSSARY_NODE_NAME = "Classification";
  private static final String TEST_GLOSSARY_NODE_DESCRIPTION = "A classification of data";
  private static final String TEST_GLOSSARY_TERM_NAME = "PII";
  private static final String TEST_GLOSSARY_TERM_DESCRIPTION = "Personally Identifiable Information";
  private Urn actorUrn;

  private RestliEntityClient _mockClient;
  private EntityChangeEventGeneratorHook _entityChangeEventHook;

  @BeforeMethod
  public void setupTest() throws URISyntaxException {
    actorUrn = Urn.createFromString(TEST_ACTOR_URN);
    AspectDifferRegistry differRegistry = createAspectDifferRegistry();
    Authentication mockAuthentication = Mockito.mock(Authentication.class);
    _mockClient = Mockito.mock(RestliEntityClient.class);
    _entityChangeEventHook = new EntityChangeEventGeneratorHook(
        differRegistry,
        _mockClient,
        mockAuthentication,
        createMockEntityRegistry(),
        true);
  }

  @Test
  public void testInvokeEntityAddTagChange() throws Exception {
    MetadataChangeLog event = new MetadataChangeLog();
    event.setEntityType(DATASET_ENTITY_NAME);
    event.setAspectName(GLOBAL_TAGS_ASPECT_NAME);
    event.setChangeType(ChangeType.UPSERT);
    final GlobalTags newTags = new GlobalTags();
    final TagUrn newTagUrn = new TagUrn("Test");
    final long eventTime = 123L;
    newTags.setTags(new TagAssociationArray(
        ImmutableList.of(new TagAssociation()
            .setTag(newTagUrn)
        )
    ));
    event.setAspect(GenericRecordUtils.serializeAspect(newTags));
    event.setEntityUrn(Urn.createFromString(TEST_DATASET_URN));
    event.setEntityType(DATASET_ENTITY_NAME);
    event.setCreated(new AuditStamp().setActor(Urn.createFromString(TEST_ACTOR_URN)).setTime(eventTime));

    // No previous tags aspect.
    _entityChangeEventHook.invoke(
        event
    );

    // Create Platform Event
    PlatformEvent platformEvent = createChangeEvent(
        DATASET_ENTITY_NAME,
        Urn.createFromString(TEST_DATASET_URN),
        ChangeCategory.TAG,
        ChangeOperation.ADD,
        newTagUrn.toString(),
        ImmutableMap.of(
            "tagUrn", newTagUrn.toString()
        ),
        Urn.createFromString(TEST_ACTOR_URN),
        eventTime
    );

    verifyProducePlatformEvent(_mockClient, platformEvent);
  }

  @Test
  public void testInvokeEntityRemoveTagChange() throws Exception {
    MetadataChangeLog event = new MetadataChangeLog();
    event.setEntityType(DATASET_ENTITY_NAME);
    event.setAspectName(GLOBAL_TAGS_ASPECT_NAME);
    event.setChangeType(ChangeType.UPSERT);
    final GlobalTags existingTags = new GlobalTags();
    final TagUrn newTagUrn = new TagUrn("Test");
    final long eventTime = 123L;
    existingTags.setTags(new TagAssociationArray(
        ImmutableList.of(new TagAssociation()
            .setTag(newTagUrn)
        )
    ));
    event.setPreviousAspectValue(GenericRecordUtils.serializeAspect(existingTags));
    event.setEntityUrn(Urn.createFromString(TEST_DATASET_URN));
    event.setEntityType(DATASET_ENTITY_NAME);
    event.setCreated(new AuditStamp().setActor(Urn.createFromString(TEST_ACTOR_URN)).setTime(eventTime));

    // No previous tags aspect.
    _entityChangeEventHook.invoke(
        event
    );

    // Create Platform Event
    PlatformEvent platformEvent = createChangeEvent(
        DATASET_ENTITY_NAME,
        Urn.createFromString(TEST_DATASET_URN),
        ChangeCategory.TAG,
        ChangeOperation.REMOVE,
        newTagUrn.toString(),
        ImmutableMap.of(
            "tagUrn", newTagUrn.toString()
        ),
        Urn.createFromString(TEST_ACTOR_URN),
        eventTime
    );

    verifyProducePlatformEvent(_mockClient, platformEvent);
  }

  @Test
  public void testInvokeEntityAddTermChange() throws Exception {
    MetadataChangeLog event = new MetadataChangeLog();
    event.setEntityType(DATASET_ENTITY_NAME);
    event.setAspectName(GLOSSARY_TERMS_ASPECT_NAME);
    event.setChangeType(ChangeType.UPSERT);
    final GlossaryTerms newTerms = new GlossaryTerms();
    final GlossaryTermUrn glossaryTermUrn = new GlossaryTermUrn("TestTerm");
    final long eventTime = 123L;
    newTerms.setTerms(new GlossaryTermAssociationArray(
        ImmutableList.of(new GlossaryTermAssociation()
            .setUrn(glossaryTermUrn)
        )
    ));
    final GlossaryTerms previousTerms = new GlossaryTerms();
    previousTerms.setTerms(new GlossaryTermAssociationArray());
    event.setAspect(GenericRecordUtils.serializeAspect(newTerms));
    event.setPreviousAspectValue(GenericRecordUtils.serializeAspect(previousTerms));
    event.setEntityUrn(Urn.createFromString(TEST_DATASET_URN));
    event.setEntityType(DATASET_ENTITY_NAME);
    event.setCreated(new AuditStamp().setActor(Urn.createFromString(TEST_ACTOR_URN)).setTime(eventTime));

    // No previous tags aspect.
    _entityChangeEventHook.invoke(
        event
    );

    // Create Platform Event
    PlatformEvent platformEvent = createChangeEvent(
        DATASET_ENTITY_NAME,
        Urn.createFromString(TEST_DATASET_URN),
        ChangeCategory.GLOSSARY_TERM,
        ChangeOperation.ADD,
        glossaryTermUrn.toString(),
        ImmutableMap.of(
            "termUrn", glossaryTermUrn.toString()
        ),
        Urn.createFromString(TEST_ACTOR_URN),
        eventTime
    );

    verifyProducePlatformEvent(_mockClient, platformEvent);
  }

  @Test
  public void testInvokeEntityRemoveTermChange() throws Exception {
    MetadataChangeLog event = new MetadataChangeLog();
    event.setEntityType(DATASET_ENTITY_NAME);
    event.setAspectName(GLOSSARY_TERMS_ASPECT_NAME);
    event.setChangeType(ChangeType.UPSERT);
    final GlossaryTerms newTerms = new GlossaryTerms();
    newTerms.setTerms(new GlossaryTermAssociationArray());
    final GlossaryTerms previousTerms = new GlossaryTerms();
    final GlossaryTermUrn glossaryTermUrn = new GlossaryTermUrn("TestTerm");
    final long eventTime = 123L;
    previousTerms.setTerms(new GlossaryTermAssociationArray(
        ImmutableList.of(new GlossaryTermAssociation()
            .setUrn(glossaryTermUrn)
        )
    ));
    event.setAspect(GenericRecordUtils.serializeAspect(newTerms));
    event.setPreviousAspectValue(GenericRecordUtils.serializeAspect(previousTerms));
    event.setEntityUrn(Urn.createFromString(TEST_DATASET_URN));
    event.setEntityType(DATASET_ENTITY_NAME);
    event.setCreated(new AuditStamp().setActor(Urn.createFromString(TEST_ACTOR_URN)).setTime(eventTime));

    // No previous tags aspect.
    _entityChangeEventHook.invoke(
        event
    );

    // Create Platform Event
    PlatformEvent platformEvent = createChangeEvent(
        DATASET_ENTITY_NAME,
        Urn.createFromString(TEST_DATASET_URN),
        ChangeCategory.GLOSSARY_TERM,
        ChangeOperation.REMOVE,
        glossaryTermUrn.toString(),
        ImmutableMap.of(
            "termUrn", glossaryTermUrn.toString()
        ),
        Urn.createFromString(TEST_ACTOR_URN),
        eventTime
    );

    verifyProducePlatformEvent(_mockClient, platformEvent);
  }

  @Test
  public void testInvokeEntitySetDomain() throws Exception {
    MetadataChangeLog event = new MetadataChangeLog();
    event.setEntityType(DATASET_ENTITY_NAME);
    event.setAspectName(DOMAINS_ASPECT_NAME);
    event.setChangeType(ChangeType.UPSERT);
    final Domains newDomains = new Domains();
    final Urn domainUrn = Urn.createFromString("urn:li:domain:test");
    final long eventTime = 123L;
    newDomains.setDomains(new UrnArray(
        ImmutableList.of(domainUrn)
    ));
    event.setAspect(GenericRecordUtils.serializeAspect(newDomains));
    event.setEntityUrn(Urn.createFromString(TEST_DATASET_URN));
    event.setEntityType(DATASET_ENTITY_NAME);
    event.setCreated(new AuditStamp().setActor(Urn.createFromString(TEST_ACTOR_URN)).setTime(eventTime));

    // No previous tags aspect.
    _entityChangeEventHook.invoke(
        event
    );

    // Create Platform Event
    PlatformEvent platformEvent = createChangeEvent(
        DATASET_ENTITY_NAME,
        Urn.createFromString(TEST_DATASET_URN),
        ChangeCategory.DOMAIN,
        ChangeOperation.ADD,
        domainUrn.toString(),
        ImmutableMap.of(
            "domainUrn", domainUrn.toString()
        ),
        Urn.createFromString(TEST_ACTOR_URN),
        eventTime
    );

    verifyProducePlatformEvent(_mockClient, platformEvent);
  }

  @Test
  public void testInvokeEntityUnsetDomain() throws Exception {
    MetadataChangeLog event = new MetadataChangeLog();
    event.setEntityType(DATASET_ENTITY_NAME);
    event.setAspectName(DOMAINS_ASPECT_NAME);
    event.setChangeType(ChangeType.UPSERT);
    final Domains previousDomains = new Domains();
    final Urn domainUrn = Urn.createFromString("urn:li:domain:test");
    final long eventTime = 123L;
    previousDomains.setDomains(new UrnArray(
        ImmutableList.of(domainUrn)
    ));
    event.setPreviousAspectValue(GenericRecordUtils.serializeAspect(previousDomains));
    event.setEntityUrn(Urn.createFromString(TEST_DATASET_URN));
    event.setEntityType(DATASET_ENTITY_NAME);
    event.setCreated(new AuditStamp().setActor(Urn.createFromString(TEST_ACTOR_URN)).setTime(eventTime));

    // No previous tags aspect.
    _entityChangeEventHook.invoke(
        event
    );

    // Create Platform Event
    PlatformEvent platformEvent = createChangeEvent(
        DATASET_ENTITY_NAME,
        Urn.createFromString(TEST_DATASET_URN),
        ChangeCategory.DOMAIN,
        ChangeOperation.REMOVE,
        domainUrn.toString(),
        ImmutableMap.of(
            "domainUrn", domainUrn.toString()
        ),
        Urn.createFromString(TEST_ACTOR_URN),
        eventTime
    );

    verifyProducePlatformEvent(_mockClient, platformEvent);
  }

  @Test
  public void testInvokeEntityOwnerChange() throws Exception {
    MetadataChangeLog event = new MetadataChangeLog();
    event.setEntityType(DATASET_ENTITY_NAME);
    event.setAspectName(OWNERSHIP_ASPECT_NAME);
    event.setChangeType(ChangeType.UPSERT);
    final Ownership newOwners = new Ownership();
    final Urn ownerUrn1 = Urn.createFromString("urn:li:corpuser:test1");
    final Urn ownerUrn2 = Urn.createFromString("urn:li:corpuser:test2");
    final long eventTime = 123L;
    newOwners.setOwners(new OwnerArray(
        ImmutableList.of(
            new Owner().setOwner(ownerUrn1).setType(OwnershipType.TECHNICAL_OWNER),
            new Owner().setOwner(ownerUrn2).setType(OwnershipType.BUSINESS_OWNER)
        )
    ));
    final Ownership prevOwners = new Ownership();
    prevOwners.setOwners(new OwnerArray());
    event.setAspect(GenericRecordUtils.serializeAspect(newOwners));
    event.setPreviousAspectValue(GenericRecordUtils.serializeAspect(prevOwners));
    event.setEntityUrn(Urn.createFromString(TEST_DATASET_URN));
    event.setEntityType(DATASET_ENTITY_NAME);
    event.setCreated(new AuditStamp().setActor(Urn.createFromString(TEST_ACTOR_URN)).setTime(eventTime));

    // No previous tags aspect.
    _entityChangeEventHook.invoke(
        event
    );

    // Create Platform Event
    PlatformEvent platformEvent1 = createChangeEvent(
        DATASET_ENTITY_NAME,
        Urn.createFromString(TEST_DATASET_URN),
        ChangeCategory.OWNER,
        ChangeOperation.ADD,
        ownerUrn1.toString(),
        ImmutableMap.of(
            "ownerUrn", ownerUrn1.toString(),
            "ownerType", OwnershipType.TECHNICAL_OWNER.toString()
        ),
        Urn.createFromString(TEST_ACTOR_URN),
        eventTime
    );
    verifyProducePlatformEvent(_mockClient, platformEvent1, false);

    PlatformEvent platformEvent2 = createChangeEvent(
        DATASET_ENTITY_NAME,
        Urn.createFromString(TEST_DATASET_URN),
        ChangeCategory.OWNER,
        ChangeOperation.ADD,
        ownerUrn2.toString(),
        ImmutableMap.of(
            "ownerUrn", ownerUrn2.toString(),
            "ownerType", OwnershipType.BUSINESS_OWNER.toString()
        ),
        Urn.createFromString(TEST_ACTOR_URN),
        eventTime
    );
    verifyProducePlatformEvent(_mockClient, platformEvent2, true);
  }

  @Test
  public void testInvokeEntityTermDeprecation() throws Exception {
    MetadataChangeLog event = new MetadataChangeLog();
    event.setEntityType(DATASET_ENTITY_NAME);
    event.setAspectName(DEPRECATION_ASPECT_NAME);
    event.setChangeType(ChangeType.UPSERT);

    Deprecation newDeprecation = new Deprecation();
    newDeprecation.setDeprecated(true);
    newDeprecation.setNote("Test Note");
    newDeprecation.setActor(Urn.createFromString(TEST_ACTOR_URN));
    final long eventTime = 123L;

    event.setAspect(GenericRecordUtils.serializeAspect(newDeprecation));
    event.setEntityUrn(Urn.createFromString(TEST_DATASET_URN));
    event.setEntityType(DATASET_ENTITY_NAME);
    event.setCreated(new AuditStamp().setActor(Urn.createFromString(TEST_ACTOR_URN)).setTime(eventTime));

    // No previous tags aspect.
    _entityChangeEventHook.invoke(
        event
    );

    // Create Platform Event
    PlatformEvent platformEvent = createChangeEvent(
        DATASET_ENTITY_NAME,
        Urn.createFromString(TEST_DATASET_URN),
        ChangeCategory.DEPRECATION,
        ChangeOperation.MODIFY,
        null,
        ImmutableMap.of(
            "status", "DEPRECATED"
        ),
        Urn.createFromString(TEST_ACTOR_URN),
        eventTime
    );

    verifyProducePlatformEvent(_mockClient, platformEvent);
  }

  @Test
  public void testInvokeEntityCreate() throws Exception {
    MetadataChangeLog event = new MetadataChangeLog();
    event.setEntityType(DATASET_ENTITY_NAME);
    event.setAspectName(DATASET_KEY_ASPECT_NAME);
    event.setChangeType(ChangeType.UPSERT);

    DatasetKey newDatasetKey = new DatasetKey();
    newDatasetKey.setName("TestName");
    newDatasetKey.setOrigin(FabricType.PROD);
    newDatasetKey.setPlatform(Urn.createFromString("urn:li:dataPlatform:hive"));
    final long eventTime = 123L;

    event.setAspect(GenericRecordUtils.serializeAspect(newDatasetKey));
    event.setEntityUrn(Urn.createFromString(TEST_DATASET_URN));
    event.setEntityType(DATASET_ENTITY_NAME);
    event.setCreated(new AuditStamp().setActor(Urn.createFromString(TEST_ACTOR_URN)).setTime(eventTime));

    // No previous tags aspect.
    _entityChangeEventHook.invoke(
        event
    );

    // Create Platform Event
    PlatformEvent platformEvent = createChangeEvent(
        DATASET_ENTITY_NAME,
        Urn.createFromString(TEST_DATASET_URN),
        ChangeCategory.LIFECYCLE,
        ChangeOperation.CREATE,
        null,
        null,
        Urn.createFromString(TEST_ACTOR_URN),
        eventTime
    );

    verifyProducePlatformEvent(_mockClient, platformEvent);
  }

  @Test
  public void testInvokeEntityHardDelete() throws Exception {
    MetadataChangeLog event = new MetadataChangeLog();
    event.setEntityType(DATASET_ENTITY_NAME);
    event.setAspectName(DATASET_KEY_ASPECT_NAME);
    event.setChangeType(ChangeType.DELETE);

    DatasetKey newDatasetKey = new DatasetKey();
    newDatasetKey.setName("TestName");
    newDatasetKey.setOrigin(FabricType.PROD);
    newDatasetKey.setPlatform(Urn.createFromString("urn:li:dataPlatform:hive"));
    final long eventTime = 123L;

    event.setPreviousAspectValue(GenericRecordUtils.serializeAspect(newDatasetKey));
    event.setEntityUrn(Urn.createFromString(TEST_DATASET_URN));
    event.setEntityType(DATASET_ENTITY_NAME);
    event.setCreated(new AuditStamp().setActor(Urn.createFromString(TEST_ACTOR_URN)).setTime(eventTime));

    // No previous tags aspect.
    _entityChangeEventHook.invoke(
        event
    );

    // Create Platform Event
    PlatformEvent platformEvent = createChangeEvent(
        DATASET_ENTITY_NAME,
        Urn.createFromString(TEST_DATASET_URN),
        ChangeCategory.LIFECYCLE,
        ChangeOperation.HARD_DELETE,
        null,
        null,
        Urn.createFromString(TEST_ACTOR_URN),
        eventTime
    );

    verifyProducePlatformEvent(_mockClient, platformEvent);
  }

  @Test
  public void testInvokeEntitySoftDelete() throws Exception {
    MetadataChangeLog event = new MetadataChangeLog();
    event.setEntityType(DATASET_ENTITY_NAME);
    event.setAspectName(STATUS_ASPECT_NAME);
    event.setChangeType(ChangeType.UPSERT);

    Status newStatus = new Status();
    newStatus.setRemoved(true);
    final long eventTime = 123L;

    event.setAspect(GenericRecordUtils.serializeAspect(newStatus));
    event.setEntityUrn(Urn.createFromString(TEST_DATASET_URN));
    event.setEntityType(DATASET_ENTITY_NAME);
    event.setCreated(new AuditStamp().setActor(Urn.createFromString(TEST_ACTOR_URN)).setTime(eventTime));

    // No previous tags aspect.
    _entityChangeEventHook.invoke(
        event
    );

    // Create Platform Event
    PlatformEvent platformEvent = createChangeEvent(
        DATASET_ENTITY_NAME,
        Urn.createFromString(TEST_DATASET_URN),
        ChangeCategory.LIFECYCLE,
        ChangeOperation.SOFT_DELETE,
        null,
        null,
        Urn.createFromString(TEST_ACTOR_URN),
        eventTime
    );

    verifyProducePlatformEvent(_mockClient, platformEvent);
  }

  @Test
  public void testInvokePendingActionRequest() throws Exception {
    Urn actionRequestUrn = Urn.createFromString(TEST_ACTION_REQUEST_URN);

    MetadataChangeLog event = new MetadataChangeLog();
    event.setEntityType(ACTION_REQUEST_ENTITY_NAME);
    event.setEntityUrn(actionRequestUrn);
    event.setAspectName(ACTION_REQUEST_STATUS_ASPECT_NAME);
    event.setChangeType(ChangeType.UPSERT);

    AuditStamp auditStamp = new AuditStamp().setActor(actorUrn).setTime(EVENT_TIME);

    ActionRequestStatus actionRequestStatus =
        new ActionRequestStatus().setStatus(ACTION_REQUEST_STATUS_PENDING).setLastModified(auditStamp);

    event.setAspect(GenericRecordUtils.serializeAspect(actionRequestStatus));
    event.setCreated(auditStamp);

    _entityChangeEventHook.invoke(event);

    Map<String, Object> parameters = new HashMap<>();
    parameters.put(ACTION_REQUEST_STATUS_KEY, ACTION_REQUEST_STATUS_PENDING);

    // Create Platform Event
    PlatformEvent platformEvent =
        createChangeEvent(ACTION_REQUEST_ENTITY_NAME, actionRequestUrn, ChangeCategory.LIFECYCLE,
            ChangeOperation.PENDING, null, parameters, actorUrn, EVENT_TIME);

    verifyProducePlatformEvent(_mockClient, platformEvent, false);
  }

  @Test
  public void testInvokeCompletedActionRequest() throws Exception {
    Urn actionRequestUrn = Urn.createFromString(TEST_ACTION_REQUEST_URN);

    MetadataChangeLog event = new MetadataChangeLog();
    event.setEntityType(ACTION_REQUEST_ENTITY_NAME);
    event.setEntityUrn(actionRequestUrn);
    event.setAspectName(ACTION_REQUEST_STATUS_ASPECT_NAME);
    event.setChangeType(ChangeType.UPSERT);

    AuditStamp auditStamp = new AuditStamp().setActor(actorUrn).setTime(EVENT_TIME);

    ActionRequestStatus actionRequestStatus =
        new ActionRequestStatus().setStatus(ACTION_REQUEST_STATUS_COMPLETE)
            .setResult(ACTION_REQUEST_RESULT_ACCEPTED)
            .setLastModified(auditStamp);

    event.setAspect(GenericRecordUtils.serializeAspect(actionRequestStatus));
    event.setCreated(auditStamp);

    _entityChangeEventHook.invoke(event);

    Map<String, Object> parameters = new HashMap<>();
    parameters.put(ACTION_REQUEST_STATUS_KEY, ACTION_REQUEST_STATUS_COMPLETE);
    parameters.put(ACTION_REQUEST_RESULT_KEY, ACTION_REQUEST_RESULT_ACCEPTED);

    // Create Platform Event
    PlatformEvent platformEvent =
        createChangeEvent(ACTION_REQUEST_ENTITY_NAME, actionRequestUrn, ChangeCategory.LIFECYCLE,
            ChangeOperation.COMPLETED, null, parameters, actorUrn, EVENT_TIME);

    verifyProducePlatformEvent(_mockClient, platformEvent, false);
  }

  @Test
  public void testInvokeGlossaryTermProposal() throws Exception {
    Urn actionRequestUrn = Urn.createFromString(TEST_ACTION_REQUEST_URN);

    MetadataChangeLog event = new MetadataChangeLog();
    event.setEntityType(ACTION_REQUEST_ENTITY_NAME);
    event.setEntityUrn(actionRequestUrn);
    event.setAspectName(ACTION_REQUEST_INFO_ASPECT_NAME);
    event.setChangeType(ChangeType.UPSERT);

    AuditStamp auditStamp = new AuditStamp().setActor(actorUrn).setTime(EVENT_TIME);

    final ActionRequestInfo actionRequestInfo =
        buildActionRequestInfo(ACTION_REQUEST_TYPE_TERM_PROPOSAL, DATASET_ENTITY_NAME);
    final ActionRequestParams actionRequestParams = new ActionRequestParams().setGlossaryTermProposal(
        new GlossaryTermProposal().setGlossaryTerm(Urn.createFromString(TEST_GLOSSARY_TERM_URN)));
    actionRequestInfo.setParams(actionRequestParams);

    event.setAspect(GenericRecordUtils.serializeAspect(actionRequestInfo));
    event.setCreated(auditStamp);

    _entityChangeEventHook.invoke(event);

    Map<String, Object> parameters =
        getActionRequestParameters(ACTION_REQUEST_TYPE_TERM_PROPOSAL, DATASET_ENTITY_NAME);
    parameters.put(GLOSSARY_TERM_URN_KEY, TEST_GLOSSARY_TERM_URN);

    // Create Platform Event
    PlatformEvent platformEvent =
        createChangeEvent(ACTION_REQUEST_ENTITY_NAME, actionRequestUrn, ChangeCategory.LIFECYCLE,
            ChangeOperation.CREATE, null, parameters, actorUrn, EVENT_TIME);

    verifyProducePlatformEvent(_mockClient, platformEvent, false);
  }

  @Test
  public void testInvokeTagProposal() throws Exception {
    Urn actionRequestUrn = Urn.createFromString(TEST_ACTION_REQUEST_URN);

    MetadataChangeLog event = new MetadataChangeLog();
    event.setEntityType(ACTION_REQUEST_ENTITY_NAME);
    event.setEntityUrn(actionRequestUrn);
    event.setAspectName(ACTION_REQUEST_INFO_ASPECT_NAME);
    event.setChangeType(ChangeType.UPSERT);

    AuditStamp auditStamp = new AuditStamp().setActor(actorUrn).setTime(EVENT_TIME);

    final ActionRequestInfo actionRequestInfo =
        buildActionRequestInfo(ACTION_REQUEST_TYPE_TAG_PROPOSAL, DATASET_ENTITY_NAME);
    final ActionRequestParams actionRequestParams = new ActionRequestParams().setTagProposal(
        new TagProposal().setTag(Urn.createFromString(TEST_TAG_URN)));
    actionRequestInfo.setParams(actionRequestParams);

    event.setAspect(GenericRecordUtils.serializeAspect(actionRequestInfo));
    event.setCreated(auditStamp);

    _entityChangeEventHook.invoke(event);

    Map<String, Object> parameters =
        getActionRequestParameters(ACTION_REQUEST_TYPE_TAG_PROPOSAL, DATASET_ENTITY_NAME);
    parameters.put(TAG_URN_KEY, TEST_TAG_URN);

    // Create Platform Event
    PlatformEvent platformEvent =
        createChangeEvent(ACTION_REQUEST_ENTITY_NAME, actionRequestUrn, ChangeCategory.LIFECYCLE,
            ChangeOperation.CREATE, null, parameters, actorUrn, EVENT_TIME);

    verifyProducePlatformEvent(_mockClient, platformEvent, false);
  }

  @Test
  public void testInvokeCreateGlossaryNodeProposal() throws Exception {
    Urn actionRequestUrn = Urn.createFromString(TEST_ACTION_REQUEST_URN);

    MetadataChangeLog event = new MetadataChangeLog();
    event.setEntityType(ACTION_REQUEST_ENTITY_NAME);
    event.setEntityUrn(actionRequestUrn);
    event.setAspectName(ACTION_REQUEST_INFO_ASPECT_NAME);
    event.setChangeType(ChangeType.UPSERT);

    AuditStamp auditStamp = new AuditStamp().setActor(actorUrn).setTime(EVENT_TIME);

    final ActionRequestInfo actionRequestInfo =
        buildActionRequestInfo(ACTION_REQUEST_TYPE_CREATE_GLOSSARY_NODE_PROPOSAL, GLOSSARY_NODE_ENTITY_NAME);
    final ActionRequestParams actionRequestParams = new ActionRequestParams().setCreateGlossaryNodeProposal(
        new CreateGlossaryNodeProposal().setName(TEST_GLOSSARY_NODE_NAME)
            .setParentNode(Urn.createFromString(TEST_PARENT_NODE_URN))
            .setDescription(TEST_GLOSSARY_NODE_DESCRIPTION));
    actionRequestInfo.setParams(actionRequestParams);

    event.setAspect(GenericRecordUtils.serializeAspect(actionRequestInfo));
    event.setCreated(auditStamp);

    _entityChangeEventHook.invoke(event);

    Map<String, Object> parameters =
        getActionRequestParameters(ACTION_REQUEST_TYPE_CREATE_GLOSSARY_NODE_PROPOSAL, GLOSSARY_NODE_ENTITY_NAME);
    parameters.put(GLOSSARY_ENTITY_NAME_KEY, TEST_GLOSSARY_NODE_NAME);
    parameters.put(PARENT_NODE_URN_KEY, TEST_PARENT_NODE_URN);
    parameters.put(DESCRIPTION_KEY, TEST_GLOSSARY_NODE_DESCRIPTION);

    // Create Platform Event
    PlatformEvent platformEvent =
        createChangeEvent(ACTION_REQUEST_ENTITY_NAME, actionRequestUrn, ChangeCategory.LIFECYCLE,
            ChangeOperation.CREATE, null, parameters, actorUrn, EVENT_TIME);

    verifyProducePlatformEvent(_mockClient, platformEvent, false);
  }

  @Test
  public void testInvokeCreateGlossaryTermProposal() throws Exception {
    Urn actionRequestUrn = Urn.createFromString(TEST_ACTION_REQUEST_URN);

    MetadataChangeLog event = new MetadataChangeLog();
    event.setEntityType(ACTION_REQUEST_ENTITY_NAME);
    event.setEntityUrn(actionRequestUrn);
    event.setAspectName(ACTION_REQUEST_INFO_ASPECT_NAME);
    event.setChangeType(ChangeType.UPSERT);

    AuditStamp auditStamp = new AuditStamp().setActor(actorUrn).setTime(EVENT_TIME);

    final ActionRequestInfo actionRequestInfo =
        buildActionRequestInfo(ACTION_REQUEST_TYPE_CREATE_GLOSSARY_TERM_PROPOSAL, GLOSSARY_TERM_ENTITY_NAME);
    final ActionRequestParams actionRequestParams = new ActionRequestParams().setCreateGlossaryNodeProposal(
        new CreateGlossaryNodeProposal().setName(TEST_GLOSSARY_TERM_NAME)
            .setParentNode(Urn.createFromString(TEST_PARENT_NODE_URN))
            .setDescription(TEST_GLOSSARY_TERM_DESCRIPTION));
    actionRequestInfo.setParams(actionRequestParams);

    event.setAspect(GenericRecordUtils.serializeAspect(actionRequestInfo));
    event.setCreated(auditStamp);

    _entityChangeEventHook.invoke(event);

    Map<String, Object> parameters =
        getActionRequestParameters(ACTION_REQUEST_TYPE_CREATE_GLOSSARY_TERM_PROPOSAL, GLOSSARY_TERM_ENTITY_NAME);
    parameters.put(GLOSSARY_ENTITY_NAME_KEY, TEST_GLOSSARY_TERM_NAME);
    parameters.put(PARENT_NODE_URN_KEY, TEST_PARENT_NODE_URN);
    parameters.put(DESCRIPTION_KEY, TEST_GLOSSARY_TERM_DESCRIPTION);

    // Create Platform Event
    PlatformEvent platformEvent =
        createChangeEvent(ACTION_REQUEST_ENTITY_NAME, actionRequestUrn, ChangeCategory.LIFECYCLE,
            ChangeOperation.CREATE, null, parameters, actorUrn, EVENT_TIME);

    verifyProducePlatformEvent(_mockClient, platformEvent, false);
  }

  @Test
  public void testInvokeUpdateDescriptionProposal() throws Exception {
    Urn actionRequestUrn = Urn.createFromString(TEST_ACTION_REQUEST_URN);

    MetadataChangeLog event = new MetadataChangeLog();
    event.setEntityType(ACTION_REQUEST_ENTITY_NAME);
    event.setEntityUrn(actionRequestUrn);
    event.setAspectName(ACTION_REQUEST_INFO_ASPECT_NAME);
    event.setChangeType(ChangeType.UPSERT);

    AuditStamp auditStamp = new AuditStamp().setActor(actorUrn).setTime(EVENT_TIME);

    final ActionRequestInfo actionRequestInfo =
        buildActionRequestInfo(ACTION_REQUEST_TYPE_UPDATE_DESCRIPTION_PROPOSAL, GLOSSARY_TERM_ENTITY_NAME);
    final ActionRequestParams actionRequestParams = new ActionRequestParams().setUpdateDescriptionProposal(
        new DescriptionProposal().setDescription(TEST_GLOSSARY_TERM_DESCRIPTION));
    actionRequestInfo.setParams(actionRequestParams);

    event.setAspect(GenericRecordUtils.serializeAspect(actionRequestInfo));
    event.setCreated(auditStamp);

    _entityChangeEventHook.invoke(event);

    Map<String, Object> parameters =
        getActionRequestParameters(ACTION_REQUEST_TYPE_UPDATE_DESCRIPTION_PROPOSAL, GLOSSARY_TERM_ENTITY_NAME);
    parameters.put(DESCRIPTION_KEY, TEST_GLOSSARY_TERM_DESCRIPTION);

    // Create Platform Event
    PlatformEvent platformEvent =
        createChangeEvent(ACTION_REQUEST_ENTITY_NAME, actionRequestUrn, ChangeCategory.LIFECYCLE,
            ChangeOperation.CREATE, null, parameters, actorUrn, EVENT_TIME);

    verifyProducePlatformEvent(_mockClient, platformEvent, false);
  }

  @Test
  public void testInvokeIneligibleAspect() throws Exception {
    MetadataChangeLog event = new MetadataChangeLog();
    event.setEntityType(DATASET_ENTITY_NAME);
    event.setAspectName(DATAHUB_POLICY_INFO_ASPECT_NAME);
    event.setChangeType(ChangeType.UPSERT);

    DatasetProperties props = new DatasetProperties();
    props.setName("Test name");

    event.setAspect(GenericRecordUtils.serializeAspect(props));
    event.setEntityUrn(Urn.createFromString(TEST_DATASET_URN));
    event.setEntityType(DATASET_ENTITY_NAME);
    event.setCreated(new AuditStamp().setActor(Urn.createFromString(TEST_ACTOR_URN)).setTime(123L));

    // No previous tags aspect.
    _entityChangeEventHook.invoke(
        event
    );
    // Verify 0 interactions
    Mockito.verifyNoMoreInteractions(_mockClient);
  }

  private PlatformEvent createChangeEvent(
      String entityType,
      Urn entityUrn,
      ChangeCategory category,
      ChangeOperation operation,
      String modifier,
      Map<String, Object> parameters,
      Urn actor,
      long timestamp) throws Exception {
    final EntityChangeEvent changeEvent = new EntityChangeEvent();
    changeEvent.setEntityType(entityType);
    changeEvent.setEntityUrn(entityUrn);
    changeEvent.setCategory(category.name());
    changeEvent.setOperation(operation.name());
    if (modifier != null) {
      changeEvent.setModifier(modifier);
    }
    changeEvent.setAuditStamp(new AuditStamp()
      .setActor(actor)
      .setTime(timestamp)
    );
    changeEvent.setVersion(0);
    if (parameters != null) {
      changeEvent.setParameters(new Parameters(new DataMap(parameters)));
    }
    final PlatformEvent platformEvent = new PlatformEvent();
    platformEvent.setName(CHANGE_EVENT_PLATFORM_EVENT_NAME);
    platformEvent.setHeader(new PlatformEventHeader().setTimestampMillis(timestamp));
    platformEvent.setPayload(GenericRecordUtils.serializePayload(
        changeEvent
    ));
    return platformEvent;
  }

  private AspectDifferRegistry createAspectDifferRegistry() {
    final AspectDifferRegistry registry = new AspectDifferRegistry();
    registry.register(GLOBAL_TAGS_ASPECT_NAME, new GlobalTagsDiffer());
    registry.register(GLOSSARY_TERMS_ASPECT_NAME, new GlossaryTermsDiffer());
    registry.register(DOMAINS_ASPECT_NAME, new SingleDomainDiffer());
    registry.register(OWNERSHIP_ASPECT_NAME, new OwnershipDiffer());
    registry.register(STATUS_ASPECT_NAME, new StatusDiffer());
    registry.register(DEPRECATION_ASPECT_NAME, new DeprecationDiffer());

    // TODO Add Dataset Schema Field related differs.

    // Entity Lifecycle Differs
    registry.register(DATASET_KEY_ASPECT_NAME, new EntityKeyDiffer<>());

    // Action Request differs
    registry.register(ACTION_REQUEST_STATUS_ASPECT_NAME, new ActionRequestStatusDiffer());
    registry.register(ACTION_REQUEST_INFO_ASPECT_NAME, new ActionRequestInfoDiffer());

    return registry;
  }

  private EntityRegistry createMockEntityRegistry() {
    EntityRegistry registry = Mockito.mock(EntityRegistry.class);
    // Build Dataset Entity Spec
    EntitySpec datasetSpec = Mockito.mock(EntitySpec.class);

    AspectSpec mockTags = createMockAspectSpec(GlobalTags.class);
    Mockito.when(datasetSpec.getAspectSpec(eq(GLOBAL_TAGS_ASPECT_NAME))).thenReturn(mockTags);

    AspectSpec mockTerms = createMockAspectSpec(GlossaryTerms.class);
    Mockito.when(datasetSpec.getAspectSpec(eq(GLOSSARY_TERMS_ASPECT_NAME))).thenReturn(mockTerms);

    AspectSpec mockOwners = createMockAspectSpec(Ownership.class);
    Mockito.when(datasetSpec.getAspectSpec(eq(OWNERSHIP_ASPECT_NAME))).thenReturn(mockOwners);

    AspectSpec mockStatus = createMockAspectSpec(Status.class);
    Mockito.when(datasetSpec.getAspectSpec(eq(STATUS_ASPECT_NAME))).thenReturn(mockStatus);

    AspectSpec mockDomains = createMockAspectSpec(Domains.class);
    Mockito.when(datasetSpec.getAspectSpec(eq(DOMAINS_ASPECT_NAME))).thenReturn(mockDomains);

    AspectSpec mockDeprecation = createMockAspectSpec(Deprecation.class);
    Mockito.when(datasetSpec.getAspectSpec(eq(DEPRECATION_ASPECT_NAME))).thenReturn(mockDeprecation);

    AspectSpec mockDatasetKey = createMockAspectSpec(DatasetKey.class);
    Mockito.when(datasetSpec.getAspectSpec(eq(DATASET_KEY_ASPECT_NAME))).thenReturn(mockDatasetKey);

    Mockito.when(registry.getEntitySpec(eq(DATASET_ENTITY_NAME)))
        .thenReturn(datasetSpec);

    // Build Action Request Entity Spec
    EntitySpec actionRequestSpec = Mockito.mock(EntitySpec.class);

    AspectSpec mockActionRequestStatus = createMockAspectSpec(ActionRequestStatus.class);
    Mockito.when(actionRequestSpec.getAspectSpec(eq(ACTION_REQUEST_STATUS_ASPECT_NAME)))
        .thenReturn(mockActionRequestStatus);

    AspectSpec mockActionRequestInfo = createMockAspectSpec(ActionRequestInfo.class);
    Mockito.when(actionRequestSpec.getAspectSpec(eq(ACTION_REQUEST_INFO_ASPECT_NAME)))
        .thenReturn(mockActionRequestInfo);

    Mockito.when(registry.getEntitySpec(ACTION_REQUEST_ENTITY_NAME)).thenReturn(actionRequestSpec);

    return registry;
  }

  private void verifyProducePlatformEvent(EntityClient mockClient, PlatformEvent platformEvent) throws Exception {
    verifyProducePlatformEvent(mockClient, platformEvent, true);
  }

  private void verifyProducePlatformEvent(EntityClient mockClient, PlatformEvent platformEvent, boolean noMoreInteractions) throws Exception {
    // Verify event has been emitted.
    Mockito.verify(mockClient, Mockito.times(1))
        .producePlatformEvent(eq(CHANGE_EVENT_PLATFORM_EVENT_NAME), Mockito.anyString(),
            argThat(new PlatformEventMatcher(platformEvent)), Mockito.any(Authentication.class));

    if (noMoreInteractions) {
      Mockito.verifyNoMoreInteractions(_mockClient);
    }
  }

  private <T extends RecordTemplate> AspectSpec createMockAspectSpec(Class<T> clazz) {
    AspectSpec mockSpec = Mockito.mock(AspectSpec.class);
    Mockito.when(mockSpec.getDataTemplateClass()).thenReturn((Class<RecordTemplate>) clazz);
    return mockSpec;
  }

  // SaaS only
  private ActionRequestInfo buildActionRequestInfo(@Nonnull final String proposalType,
      @Nonnull final String resourceType)
      throws URISyntaxException {
    return new ActionRequestInfo().setType(proposalType)
        .setAssignedUsers(new UrnArray(actorUrn))
        .setAssignedGroups(new UrnArray(Urn.createFromString(TEST_GROUP_URN)))
        .setResourceType(resourceType);
  }

  private Map<String, Object> getActionRequestParameters(@Nonnull final String proposalType,
      @Nonnull final String resourceType) {
    Map<String, Object> parameters = new HashMap<>();
    parameters.put(ACTION_REQUEST_TYPE_KEY, proposalType);
    parameters.put(RESOURCE_TYPE_KEY, resourceType);
    return parameters;
  }
}