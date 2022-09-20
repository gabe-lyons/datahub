package com.linkedin.metadata.test.action.domain;

import com.linkedin.common.urn.Urn;
import com.linkedin.common.urn.UrnUtils;
import com.linkedin.metadata.service.DomainService;
import com.linkedin.metadata.resource.ResourceReference;
import com.linkedin.metadata.test.action.ActionParameters;
import com.linkedin.metadata.test.action.Action;
import com.linkedin.metadata.test.action.ActionType;
import com.linkedin.metadata.test.exception.InvalidActionParamsException;
import com.linkedin.metadata.test.exception.InvalidOperandException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static com.linkedin.metadata.test.action.ActionUtils.*;


@Slf4j
@RequiredArgsConstructor
public class SetDomainAction implements Action {

  private static final String VALUES_PARAM = "values";
  private final DomainService domainService;

  @Override
  public ActionType getActionType() {
    return ActionType.SET_DOMAIN;
  }

  @Override
  public void validate(ActionParameters params) throws InvalidActionParamsException {
    if (!params.getParams().containsKey(VALUES_PARAM)) {
      throw new InvalidActionParamsException("Action parameters are missing the required 'values' parameter.");
    }
  }

  @Override
  public void apply(List<Urn> urns, ActionParameters params) throws InvalidOperandException {
    // For each entity type, group then apply the action.
    final List<String> domainUrnStrs = params.getParams().get(VALUES_PARAM);
    final Map<String, List<Urn>> entityTypesToUrns = getEntityTypeToUrns(urns);
    for (Map.Entry<String, List<Urn>> entityTypeToUrn : entityTypesToUrns.entrySet()) {
      applyInternal(UrnUtils.getUrn(domainUrnStrs.get(0)), entityTypeToUrn.getValue());
    }
  }

  private void applyInternal(Urn domainUrn, List<Urn> urns) {
    if (!urns.isEmpty()) {
      this.domainService.batchSetDomain(domainUrn, urns.stream()
          .map(urn -> new ResourceReference(urn, null, null))
          .collect(Collectors.toList()));
    }
  }
}