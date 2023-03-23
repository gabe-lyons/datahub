package com.linkedin.metadata.service;

import com.linkedin.common.IncidentsSummary;
import com.linkedin.common.UrnArray;
import com.linkedin.common.urn.Urn;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nonnull;

public class IncidentsSummaryUtils {

  public static void removeIncidentFromResolvedSummary(@Nonnull final Urn incidentUrn, @Nonnull final IncidentsSummary summary) {
    final Set<Urn> resolvedIncidents = new HashSet<>(summary.getResolvedIncidents());
    resolvedIncidents.remove(incidentUrn);
    summary.setResolvedIncidents(new UrnArray(new ArrayList<>(resolvedIncidents)));
  }

  public static void removeIncidentFromActiveSummary(@Nonnull final Urn incidentUrn,  @Nonnull final IncidentsSummary summary) {
    final Set<Urn> activeIncidents = new HashSet<>(summary.getActiveIncidents());
    activeIncidents.remove(incidentUrn);
    summary.setActiveIncidents(new UrnArray(new ArrayList<>(activeIncidents)));
  }

  public static void addIncidentToResolvedSummary(@Nonnull final Urn incidentUrn,  @Nonnull final IncidentsSummary summary) {
    final Set<Urn> resolvedIncidents = new HashSet<>(summary.getResolvedIncidents());
    resolvedIncidents.add(incidentUrn);
    summary.setResolvedIncidents(new UrnArray(new ArrayList<>(resolvedIncidents)));
  }

  public static void addIncidentToActiveSummary(@Nonnull final Urn incidentUrn,  @Nonnull final IncidentsSummary summary) {
    final Set<Urn> activeIncidents = new HashSet<>(summary.getActiveIncidents());
    activeIncidents.add(incidentUrn);
    summary.setActiveIncidents(new UrnArray(new ArrayList<>(activeIncidents)));
  }

  private IncidentsSummaryUtils() { }
}
