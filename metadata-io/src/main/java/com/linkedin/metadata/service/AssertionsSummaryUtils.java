package com.linkedin.metadata.service;

import com.linkedin.common.AssertionsSummary;
import com.linkedin.common.UrnArray;
import com.linkedin.common.urn.Urn;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nonnull;

public class AssertionsSummaryUtils {

  public static void removeAssertionFromFailingSummary(@Nonnull final Urn assertionUrn, @Nonnull final AssertionsSummary summary) {
    final Set<Urn> failingAssertions = new HashSet<>(summary.getFailingAssertions());
    failingAssertions.remove(assertionUrn);
    summary.setFailingAssertions(new UrnArray(new ArrayList<>(failingAssertions)));
  }

  public static void removeAssertionFromPassingSummary(@Nonnull final Urn assertionUrn,  @Nonnull final AssertionsSummary summary) {
    final Set<Urn> passingAssertions = new HashSet<>(summary.getPassingAssertions());
    passingAssertions.remove(assertionUrn);
    summary.setPassingAssertions(new UrnArray(new ArrayList<>(passingAssertions)));
  }

  public static void addAssertionToFailingSummary(@Nonnull final Urn assertionUrn,  @Nonnull final AssertionsSummary summary) {
    final Set<Urn> failingAssertions = new HashSet<>(summary.getFailingAssertions());
    failingAssertions.add(assertionUrn);
    summary.setFailingAssertions(new UrnArray(new ArrayList<>(failingAssertions)));
  }

  public static void addAssertionToPassingSummary(@Nonnull final Urn assertionUrn,  @Nonnull final AssertionsSummary summary) {
    final Set<Urn> passingAssertions = new HashSet<>(summary.getPassingAssertions());
    passingAssertions.add(assertionUrn);
    summary.setPassingAssertions(new UrnArray(new ArrayList<>(passingAssertions)));
  }

  private AssertionsSummaryUtils() { }
}
