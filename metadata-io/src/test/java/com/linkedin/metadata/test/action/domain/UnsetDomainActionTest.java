package com.linkedin.metadata.test.action.domain;

import com.linkedin.common.urn.Urn;
import com.linkedin.common.urn.UrnUtils;
import com.linkedin.metadata.resource.ResourceReference;
import com.linkedin.metadata.service.DomainService;
import com.linkedin.metadata.test.action.ActionParameters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.mockito.Mockito;
import org.testcontainers.shaded.com.google.common.collect.ImmutableList;
import org.testng.annotations.Test;

public class UnsetDomainActionTest {

  private static final List<Urn> DATASET_URNS = ImmutableList.of(
      UrnUtils.getUrn("urn:li:dataset:(urn:li:dataPlatform:kafka,test,PROD)"),
      UrnUtils.getUrn("urn:li:dataset:(urn:li:dataPlatform:kafka,test1,PROD)")
  );

  private static final List<Urn> DASHBOARD_URNS = ImmutableList.of(
      UrnUtils.getUrn("urn:li:dashboard:(looker,1)"),
      UrnUtils.getUrn("urn:li:dashboard:(looker,2)")
  );

  private static final List<Urn> ALL_URNS = new ArrayList<>();

  static {
    ALL_URNS.addAll(DATASET_URNS);
    ALL_URNS.addAll(DASHBOARD_URNS);
  }

  private static final List<ResourceReference> DATASET_REFERENCES = DATASET_URNS.stream().map(
      urn -> new ResourceReference(urn, null, null)
  ).collect(Collectors.toList());

  private static final List<ResourceReference> DASHBOARD_REFERENCES = DASHBOARD_URNS.stream().map(
      urn -> new ResourceReference(urn, null, null)
  ).collect(Collectors.toList());

  @Test
  private void testApply() throws Exception {
    DomainService service = Mockito.mock(DomainService.class);

    UnsetDomainAction action = new UnsetDomainAction(service);
    ActionParameters params = new ActionParameters(Collections.emptyMap());
    action.apply(ALL_URNS, params);

    Mockito.verify(service, Mockito.atLeastOnce()).batchUnsetDomain(
        Mockito.eq(DASHBOARD_REFERENCES)
    );
    Mockito.verify(service, Mockito.atLeastOnce()).batchUnsetDomain(
        Mockito.eq(DATASET_REFERENCES)
    );

    Mockito.verifyNoMoreInteractions(service);
  }

  @Test
  private void testValidateValidParams() {
    UnsetDomainAction action = new UnsetDomainAction(Mockito.mock(DomainService.class));
    ActionParameters params = new ActionParameters(Collections.emptyMap());
    action.validate(params);
  }
}