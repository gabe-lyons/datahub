package com.linkedin.metadata.service;

import com.google.common.collect.ImmutableList;
import com.linkedin.common.IncidentsSummary;
import com.linkedin.common.UrnArray;
import com.linkedin.common.urn.Urn;
import com.linkedin.common.urn.UrnUtils;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;


public class IncidentsSummaryUtilsTest {

  private static final Urn TEST_INCIDENT_URN = UrnUtils.getUrn("urn:li:incident:test");
  private static final Urn TEST_INCIDENT_URN_2 = UrnUtils.getUrn("urn:li:incident:test-2");

  @Test
  public void testRemoveIncidentFromResolvedSummary() {
    // Case 1: Has the incident in resolved.
    IncidentsSummary summary = mockIncidentsSummary(
        ImmutableList.of(TEST_INCIDENT_URN),
        Collections.emptyList()
    );
    IncidentsSummaryUtils.removeIncidentFromResolvedSummary(TEST_INCIDENT_URN, summary);
    Assert.assertEquals(summary, mockIncidentsSummary(Collections.emptyList(), Collections.emptyList()));

    // Case 2: Has the incident in active.
    summary = mockIncidentsSummary(
        Collections.emptyList(),
        ImmutableList.of(TEST_INCIDENT_URN)
    );
    IncidentsSummaryUtils.removeIncidentFromResolvedSummary(TEST_INCIDENT_URN, summary);
    Assert.assertEquals(summary, mockIncidentsSummary(Collections.emptyList(), ImmutableList.of(TEST_INCIDENT_URN)));

    // Case 3: Does not have the incident at all.
    summary = mockIncidentsSummary(
        Collections.emptyList(),
        Collections.emptyList()
    );
    IncidentsSummaryUtils.removeIncidentFromResolvedSummary(TEST_INCIDENT_URN, summary);
    Assert.assertEquals(summary, mockIncidentsSummary(Collections.emptyList(), Collections.emptyList()));

    // Case 4: Has 2 items in list.
    summary = mockIncidentsSummary(
        ImmutableList.of(TEST_INCIDENT_URN, TEST_INCIDENT_URN_2),
        Collections.emptyList()
    );
    IncidentsSummaryUtils.removeIncidentFromResolvedSummary(TEST_INCIDENT_URN, summary);
    Assert.assertEquals(summary, mockIncidentsSummary(ImmutableList.of(TEST_INCIDENT_URN_2), Collections.emptyList()));
  }

  @Test
  public void testRemoveIncidentFromActiveSummary() {
    // Case 1: Has the incident in active.
    IncidentsSummary summary = mockIncidentsSummary(
        Collections.emptyList(),
        ImmutableList.of(TEST_INCIDENT_URN)
    );
    IncidentsSummaryUtils.removeIncidentFromActiveSummary(TEST_INCIDENT_URN, summary);
    Assert.assertEquals(summary, mockIncidentsSummary(Collections.emptyList(), Collections.emptyList()));

    // Case 2: Has the incident in resolved.
    summary = mockIncidentsSummary(
        ImmutableList.of(TEST_INCIDENT_URN),
        Collections.emptyList()
    );
    IncidentsSummaryUtils.removeIncidentFromActiveSummary(TEST_INCIDENT_URN, summary);
    Assert.assertEquals(summary, mockIncidentsSummary(ImmutableList.of(TEST_INCIDENT_URN), Collections.emptyList()));

    // Case 3: Does not have the incident at all.
    summary = mockIncidentsSummary(
        Collections.emptyList(),
        Collections.emptyList()
    );
    IncidentsSummaryUtils.removeIncidentFromActiveSummary(TEST_INCIDENT_URN, summary);
    Assert.assertEquals(summary, mockIncidentsSummary(Collections.emptyList(), Collections.emptyList()));

    // Case 4: Has 2 items in list.
    summary = mockIncidentsSummary(
        Collections.emptyList(),
        ImmutableList.of(TEST_INCIDENT_URN, TEST_INCIDENT_URN_2)
    );
    IncidentsSummaryUtils.removeIncidentFromActiveSummary(TEST_INCIDENT_URN, summary);
    Assert.assertEquals(summary, mockIncidentsSummary(Collections.emptyList(), ImmutableList.of(TEST_INCIDENT_URN_2)));
  }

  @Test
  public void testAddIncidentToActiveSummary() {
    // Case 1: Has an incident in active.
    IncidentsSummary summary = mockIncidentsSummary(
        Collections.emptyList(),
        ImmutableList.of(TEST_INCIDENT_URN)
    );
    IncidentsSummaryUtils.addIncidentToActiveSummary(TEST_INCIDENT_URN_2, summary);
    IncidentsSummary expected = mockIncidentsSummary(Collections.emptyList(), ImmutableList.of(TEST_INCIDENT_URN, TEST_INCIDENT_URN_2));
    Assert.assertEquals(new HashSet<>(summary.getActiveIncidents()), new HashSet<>(expected.getActiveIncidents())); // Set comparison
    Assert.assertEquals(new HashSet<>(summary.getResolvedIncidents()), new HashSet<>(expected.getResolvedIncidents())); // Set comparison

    // Case 2: Has an incident in resolved.
    summary = mockIncidentsSummary(
        ImmutableList.of(TEST_INCIDENT_URN),
        Collections.emptyList()
    );
    IncidentsSummaryUtils.addIncidentToActiveSummary(TEST_INCIDENT_URN_2, summary);
    Assert.assertEquals(summary, mockIncidentsSummary(ImmutableList.of(TEST_INCIDENT_URN), ImmutableList.of(TEST_INCIDENT_URN_2)));

    // Case 3: Does not have any incidents yet
    summary = mockIncidentsSummary(
        Collections.emptyList(),
        Collections.emptyList()
    );
    IncidentsSummaryUtils.addIncidentToActiveSummary(TEST_INCIDENT_URN, summary);
    Assert.assertEquals(summary, mockIncidentsSummary(Collections.emptyList(), ImmutableList.of(TEST_INCIDENT_URN)));

    // Case 4: Duplicate additions - already has the same incident
    summary = mockIncidentsSummary(
        Collections.emptyList(),
        ImmutableList.of(TEST_INCIDENT_URN)
    );
    IncidentsSummaryUtils.addIncidentToActiveSummary(TEST_INCIDENT_URN, summary);
    Assert.assertEquals(summary, mockIncidentsSummary(Collections.emptyList(), ImmutableList.of(TEST_INCIDENT_URN)));
  }

  @Test
  public void testAddIncidentToResolvedSummary() {
    // Case 1: Has an incident in resolved.
    IncidentsSummary summary = mockIncidentsSummary(
        ImmutableList.of(TEST_INCIDENT_URN),
        Collections.emptyList()
    );
    IncidentsSummaryUtils.addIncidentToResolvedSummary(TEST_INCIDENT_URN_2, summary);
    IncidentsSummary expected = mockIncidentsSummary(ImmutableList.of(TEST_INCIDENT_URN, TEST_INCIDENT_URN_2), Collections.emptyList());
    Assert.assertEquals(new HashSet<>(summary.getActiveIncidents()), new HashSet<>(expected.getActiveIncidents())); // Set comparison
    Assert.assertEquals(new HashSet<>(summary.getResolvedIncidents()), new HashSet<>(expected.getResolvedIncidents())); // Set comparison

    // Case 2: Has an incident in active.
    summary = mockIncidentsSummary(
        Collections.emptyList(),
        ImmutableList.of(TEST_INCIDENT_URN)
    );
    IncidentsSummaryUtils.addIncidentToResolvedSummary(TEST_INCIDENT_URN_2, summary);
    Assert.assertEquals(summary, mockIncidentsSummary(ImmutableList.of(TEST_INCIDENT_URN_2), ImmutableList.of(TEST_INCIDENT_URN)));

    // Case 3: Does not have any incidents yet
    summary = mockIncidentsSummary(
        Collections.emptyList(),
        Collections.emptyList()
    );
    IncidentsSummaryUtils.addIncidentToResolvedSummary(TEST_INCIDENT_URN, summary);
    Assert.assertEquals(summary, mockIncidentsSummary(ImmutableList.of(TEST_INCIDENT_URN), Collections.emptyList()));

    // Case 4: Duplicate additions - already has the same incident
    summary = mockIncidentsSummary(
        ImmutableList.of(TEST_INCIDENT_URN),
        Collections.emptyList()
    );
    IncidentsSummaryUtils.addIncidentToResolvedSummary(TEST_INCIDENT_URN, summary);
    Assert.assertEquals(summary, mockIncidentsSummary(ImmutableList.of(TEST_INCIDENT_URN), Collections.emptyList()));
  }

  private IncidentsSummary mockIncidentsSummary(
      final List<Urn> resolvedIncidents,
      final List<Urn> activeIncidents) {
    return new IncidentsSummary()
        .setResolvedIncidents(new UrnArray(resolvedIncidents))
        .setActiveIncidents(new UrnArray(activeIncidents));
  }
}
