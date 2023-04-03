package com.linkedin.metadata.service;

import com.google.common.collect.ImmutableSet;
import com.linkedin.common.IncidentsSummary;
import com.linkedin.common.urn.Urn;
import com.linkedin.entity.EntityResponse;
import com.linkedin.entity.client.EntityClient;
import com.datahub.authentication.Authentication;
import com.linkedin.incident.IncidentInfo;
import com.linkedin.metadata.Constants;
import com.linkedin.metadata.entity.AspectUtils;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;



@Slf4j
public class IncidentService extends BaseService {

  public IncidentService(@Nonnull final EntityClient entityClient, @Nonnull final Authentication systemAuthentication) {
    super(entityClient, systemAuthentication);
  }

  /**
   * Returns an instance of {@link IncidentInfo} for the specified Incident urn,
   * or null if one cannot be found.
   *
   * @param incidentUrn the urn of the Incident
   *
   * @return an instance of {@link com.linkedin.incident.IncidentInfo} for the Incident, null if it does not exist.
   */
  @Nullable
  public IncidentInfo getIncidentInfo(@Nonnull final Urn incidentUrn) {
    Objects.requireNonNull(incidentUrn, "incidentUrn must not be null");
    final EntityResponse response = getIncidentEntityResponse(incidentUrn, this.systemAuthentication);
    if (response != null && response.getAspects().containsKey(Constants.INCIDENT_INFO_ASPECT_NAME)) {
      return new IncidentInfo(response.getAspects().get(Constants.INCIDENT_INFO_ASPECT_NAME).getValue().data());
    }
    // No aspect found
    return null;
  }

  /**
   * Returns an instance of {@link com.linkedin.common.IncidentsSummary} for the specified Entity urn,
   * or null if one cannot be found.
   *
   * @param entityUrn the urn of the entity to retrieve the summary for
   *
   * @return an instance of {@link com.linkedin.common.IncidentsSummary} for the Entity, null if it does not exist.
   */
  @Nullable
  public IncidentsSummary getIncidentsSummary(@Nonnull final Urn entityUrn) {
    Objects.requireNonNull(entityUrn, "entityUrn must not be null");
    final EntityResponse response = getIncidentsSummaryResponse(entityUrn, this.systemAuthentication);
    if (response != null && response.getAspects().containsKey(Constants.INCIDENTS_SUMMARY_ASPECT_NAME)) {
      return new IncidentsSummary(response.getAspects().get(Constants.INCIDENTS_SUMMARY_ASPECT_NAME).getValue().data());
    }
    // No aspect found
    return null;
  }

  /**
   * Produces a Metadata Change Proposal to update the IncidentsSummary aspect for a given entity.
   */
  public void updateIncidentsSummary(@Nonnull final Urn entityUrn, @Nonnull final IncidentsSummary newSummary) throws Exception {
    Objects.requireNonNull(entityUrn, "entityUrn must not be null");
    Objects.requireNonNull(newSummary, "newSummary must not be null");
    this.entityClient.ingestProposal(
        AspectUtils.buildMetadataChangeProposal(entityUrn, Constants.INCIDENTS_SUMMARY_ASPECT_NAME, newSummary),
        this.systemAuthentication,
        false);
  }

  /**
   * Returns an instance of {@link EntityResponse} for the specified View urn,
   * or null if one cannot be found.
   *
   * @param incidentUrn the urn of the View
   * @param authentication the authentication to use
   *
   * @return an instance of {@link EntityResponse} for the View, null if it does not exist.
   */
  @Nullable
  private EntityResponse getIncidentEntityResponse(@Nonnull final Urn incidentUrn, @Nonnull final Authentication authentication) {
    Objects.requireNonNull(incidentUrn, "incidentUrn must not be null");
    Objects.requireNonNull(authentication, "authentication must not be null");
    try {
      return this.entityClient.getV2(
          Constants.INCIDENT_ENTITY_NAME,
          incidentUrn,
          ImmutableSet.of(Constants.INCIDENT_INFO_ASPECT_NAME),
          authentication
      );
    } catch (Exception e) {
      throw new RuntimeException(String.format("Failed to retrieve Incident with urn %s", incidentUrn), e);
    }
  }


  /**
   * Returns an instance of {@link EntityResponse} for the specified Entity urn containing the incidents summary aspect
   * or null if one cannot be found.
   *
   * @param entityUrn the urn of the Entity for which to fetch incident summary
   * @param authentication the authentication to use
   *
   * @return an instance of {@link EntityResponse} for the View, null if it does not exist.
   */
  @Nullable
  private EntityResponse getIncidentsSummaryResponse(@Nonnull final Urn entityUrn, @Nonnull final Authentication authentication) {
    Objects.requireNonNull(entityUrn, "entityUrn must not be null");
    Objects.requireNonNull(authentication, "authentication must not be null");
    try {
      return this.entityClient.getV2(
          entityUrn.getEntityType(),
          entityUrn,
          ImmutableSet.of(Constants.INCIDENTS_SUMMARY_ASPECT_NAME),
          authentication
      );
    } catch (Exception e) {
      throw new RuntimeException(String.format("Failed to retrieve Incident Summary for entity with urn %s", entityUrn), e);
    }
  }
}
