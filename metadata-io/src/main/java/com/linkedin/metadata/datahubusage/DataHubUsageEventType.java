package com.linkedin.metadata.datahubusage;

import lombok.Getter;


@Getter
public enum DataHubUsageEventType {
  HOME_PAGE_RECOMMENDATION_CLICK_EVENT("HomePageRecommendationClickEvent"),
  HOME_PAGE_EXPLORE_ALL_CLICK_EVENT("HomePageExploreAllClickEvent"),
  SEARCH_ACROSS_LINEAGE_EVENT("SearchAcrossLineageEvent"),
  SEARCH_ACROSS_LINEAGE_RESULTS_VIEW_EVENT("SearchAcrossLineageResultsViewEvent"),
  DOWNLOAD_AS_CSV_EVENT("DownloadAsCsvEvent"),
  SIGN_UP_EVENT("SignUpEvent"),
  RESET_CREDENTIALS_EVENT("ResetCredentialsEvent"),
  CREATE_ACCESS_TOKEN_EVENT("CreateAccessTokenEvent"),
  REVOKE_ACCESS_TOKEN_EVENT("RevokeAccessTokenEvent"),
  CREATE_GROUP_EVENT("CreateGroupEvent"),
  CREATE_INVITE_LINK_EVENT("CreateInviteLinkEvent"),
  CREATE_RESET_CREDENTIALS_LINK_EVENT("CreateResetCredentialsLinkEvent"),
  DELETE_ENTITY_EVENT("DeleteEntityEvent"),
  SELECT_USER_ROLE_EVENT("SelectUserRoleEvent"),
  BATCH_SELECT_USER_ROLE_EVENT("BatchSelectUserRoleEvent"),
  CREATE_POLICY_EVENT("CreatePolicyEvent"),
  UPDATE_POLICY_EVENT("UpdatePolicyEvent"),
  DEACTIVATE_POLICY_EVENT("DeactivatePolicyEvent"),
  ACTIVATE_POLICY_EVENT("ActivatePolicyEvent"),
  SHOW_SIMPLIFIED_HOME_PAGE_EVENT("ShowSimplifiedHomepageEvent"),
  SHOW_STANDARD_HOME_PAGE_EVENT("ShowStandardHomepageEvent"),
  CREATE_GLOSSARY_ENTITY_EVENT("CreateGlossaryEntityEvent"),
  CREATE_DOMAIN_EVENT("CreateDomainEvent"),
  CREATE_INGESTION_SOURCE_EVENT("CreateIngestionSourceEvent"),
  UPDATE_INGESTION_SOURCE_EVENT("UpdateIngestionSourceEvent"),
  DELETE_INGESTION_SOURCE_EVENT("DeleteIngestionSourceEvent"),
  EXECUTE_INGESTION_SOURCE_EVENT("ExecuteIngestionSourceEvent"),
  SSO_EVENT("SsoEvent"),
  CREATE_VIEW_EVENT("CreateViewEvent"),
  UPDATE_VIEW_EVENT("UpdateViewEvent"),
  SET_GLOBAL_DEFAULT_VIEW_EVENT("SetGlobalDefaultViewEvent"),
  SET_USER_DEFAULT_VIEW_EVENT("SetUserDefaultViewEvent"),
  MANUALLY_CREATE_LINEAGE_EVENT("ManuallyCreateLineageEvent"),
  MANUALLY_DELETE_LINEAGE_EVENT("ManuallyDeleteLineageEvent"),
  
  // SaaS only events
  CREATE_TEST_EVENT("CreateTestEvent"),
  UPDATE_TEST_EVENT("UpdateTestEvent"),
  DELETE_TEST_EVENT("DeleteTestEvent");

  private final String type;

  DataHubUsageEventType(String type) {
    this.type = type;
  }

  public static DataHubUsageEventType getType(String name) {
    for (DataHubUsageEventType eventType : DataHubUsageEventType.values()) {
      if (eventType.type.equalsIgnoreCase(name)) {
        return eventType;
      }
    }
    return null;
  }
}
