package com.linkedin.metadata.service;

import com.google.common.collect.ImmutableSet;
import com.linkedin.assertion.AssertionActions;
import com.linkedin.assertion.AssertionInfo;
import com.linkedin.common.AssertionsSummary;
import com.linkedin.common.urn.Urn;
import com.linkedin.entity.EntityResponse;
import com.linkedin.entity.client.EntityClient;
import com.datahub.authentication.Authentication;
import com.linkedin.metadata.Constants;
import com.linkedin.metadata.entity.AspectUtils;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;



@Slf4j
public class AssertionService extends BaseService {

  public AssertionService(@Nonnull final EntityClient entityClient, @Nonnull final Authentication systemAuthentication) {
    super(entityClient, systemAuthentication);
  }

  /**
   * Returns an instance of {@link AssertionInfo} for the specified Assertion urn,
   * or null if one cannot be found.
   *
   * @param assertionUrn the urn of the Assertion
   *
   * @return an instance of {@link com.linkedin.assertion.AssertionInfo} for the Assertion, null if it does not exist.
   */
  @Nullable
  public AssertionInfo getAssertionInfo(@Nonnull final Urn assertionUrn) {
    Objects.requireNonNull(assertionUrn, "assertionUrn must not be null");
    final EntityResponse response = getAssertionEntityResponse(assertionUrn, this.systemAuthentication);
    if (response != null && response.getAspects().containsKey(Constants.ASSERTION_INFO_ASPECT_NAME)) {
      return new AssertionInfo(response.getAspects().get(Constants.ASSERTION_INFO_ASPECT_NAME).getValue().data());
    }
    // No aspect found
    return null;
  }

  /**
   * Returns an instance of {@link com.linkedin.common.AssertionsSummary} for the specified Entity urn,
   * or null if one cannot be found.
   *
   * @param entityUrn the urn of the entity to retrieve the summary for
   *
   * @return an instance of {@link com.linkedin.common.AssertionsSummary} for the Entity, null if it does not exist.
   */
  @Nullable
  public AssertionsSummary getAssertionsSummary(@Nonnull final Urn entityUrn) {
    Objects.requireNonNull(entityUrn, "entityUrn must not be null");
    final EntityResponse response = getAssertionsSummaryResponse(entityUrn, this.systemAuthentication);
    if (response != null && response.getAspects().containsKey(Constants.ASSERTIONS_SUMMARY_ASPECT_NAME)) {
      return new AssertionsSummary(response.getAspects().get(Constants.ASSERTIONS_SUMMARY_ASPECT_NAME).getValue().data());
    }
    // No aspect found
    return null;
  }

  /**
   * Returns an instance of AssertionActions for the specified Entity urn,
   * or null if one cannot be found.
   *
   * @param entityUrn the urn of the entity to retrieve the actions for
   *
   * @return an instance of AssertionActions for the Entity, null if it does not exist.
   */
  @Nullable
  public AssertionActions getAssertionActions(@Nonnull final Urn entityUrn) {
    Objects.requireNonNull(entityUrn, "entityUrn must not be null");
    final EntityResponse response = getAssertionEntityResponse(entityUrn, this.systemAuthentication);
    if (response != null && response.getAspects().containsKey(Constants.ASSERTION_ACTIONS_ASPECT_NAME)) {
      return new AssertionActions(response.getAspects().get(Constants.ASSERTION_ACTIONS_ASPECT_NAME).getValue().data());
    }
    // No aspect found
    return null;
  }

  /**
   * Produces a Metadata Change Proposal to update the AssertionsSummary aspect for a given entity.
   */
  public void updateAssertionsSummary(@Nonnull final Urn entityUrn, @Nonnull final AssertionsSummary newSummary) throws Exception {
    Objects.requireNonNull(entityUrn, "entityUrn must not be null");
    Objects.requireNonNull(newSummary, "newSummary must not be null");
    this.entityClient.ingestProposal(
        AspectUtils.buildMetadataChangeProposal(entityUrn, Constants.ASSERTIONS_SUMMARY_ASPECT_NAME, newSummary),
        this.systemAuthentication,
        false);
  }

  /**
   * Returns an instance of {@link EntityResponse} for the specified View urn,
   * or null if one cannot be found.
   *
   * @param assertionUrn the urn of the View
   * @param authentication the authentication to use
   *
   * @return an instance of {@link EntityResponse} for the View, null if it does not exist.
   */
  @Nullable
  private EntityResponse getAssertionEntityResponse(@Nonnull final Urn assertionUrn, @Nonnull final Authentication authentication) {
    Objects.requireNonNull(assertionUrn, "assertionUrn must not be null");
    Objects.requireNonNull(authentication, "authentication must not be null");
    try {
      return this.entityClient.getV2(
          Constants.ASSERTION_ENTITY_NAME,
          assertionUrn,
          ImmutableSet.of(Constants.ASSERTION_INFO_ASPECT_NAME, Constants.ASSERTION_ACTIONS_ASPECT_NAME),
          authentication
      );
    } catch (Exception e) {
      throw new RuntimeException(String.format("Failed to retrieve Assertion with urn %s", assertionUrn), e);
    }
  }


  /**
   * Returns an instance of {@link EntityResponse} for the specified Entity urn containing the assertions summary aspect
   * or null if one cannot be found.
   *
   * @param entityUrn the urn of the Entity for which to fetch assertion summary
   * @param authentication the authentication to use
   *
   * @return an instance of {@link EntityResponse} for the View, null if it does not exist.
   */
  @Nullable
  private EntityResponse getAssertionsSummaryResponse(@Nonnull final Urn entityUrn, @Nonnull final Authentication authentication) {
    Objects.requireNonNull(entityUrn, "entityUrn must not be null");
    Objects.requireNonNull(authentication, "authentication must not be null");
    try {
      return this.entityClient.getV2(
          entityUrn.getEntityType(),
          entityUrn,
          ImmutableSet.of(Constants.ASSERTIONS_SUMMARY_ASPECT_NAME),
          authentication
      );
    } catch (Exception e) {
      throw new RuntimeException(String.format("Failed to retrieve Assertion Summary for entity with urn %s", entityUrn), e);
    }
  }
}