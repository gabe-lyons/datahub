package com.linkedin.metadata.service;

import com.google.common.collect.ImmutableList;
import com.linkedin.common.AssertionsSummary;
import com.linkedin.common.UrnArray;
import com.linkedin.common.urn.Urn;
import com.linkedin.common.urn.UrnUtils;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;


public class AssertionsSummaryUtilsTest {

  private static final Urn TEST_ASSERTION_URN = UrnUtils.getUrn("urn:li:assertion:test");
  private static final Urn TEST_ASSERTION_URN_2 = UrnUtils.getUrn("urn:li:assertion:test-2");

  @Test
  public void testRemoveAssertionFromFailingSummary() {
    // Case 1: Has the assertion in failing.
    AssertionsSummary summary = mockAssertionsSummary(
        ImmutableList.of(TEST_ASSERTION_URN),
        Collections.emptyList()
    );
    AssertionsSummaryUtils.removeAssertionFromFailingSummary(TEST_ASSERTION_URN, summary);
    Assert.assertEquals(summary, mockAssertionsSummary(Collections.emptyList(), Collections.emptyList()));

    // Case 2: Has the assertion in passing.
    summary = mockAssertionsSummary(
        Collections.emptyList(),
        ImmutableList.of(TEST_ASSERTION_URN)
    );
    AssertionsSummaryUtils.removeAssertionFromFailingSummary(TEST_ASSERTION_URN, summary);
    Assert.assertEquals(summary, mockAssertionsSummary(Collections.emptyList(), ImmutableList.of(TEST_ASSERTION_URN)));

    // Case 3: Does not have the assertion at all.
    summary = mockAssertionsSummary(
        Collections.emptyList(),
        Collections.emptyList()
    );
    AssertionsSummaryUtils.removeAssertionFromFailingSummary(TEST_ASSERTION_URN, summary);
    Assert.assertEquals(summary, mockAssertionsSummary(Collections.emptyList(), Collections.emptyList()));

    // Case 4: Has 2 items in list.
    summary = mockAssertionsSummary(
        ImmutableList.of(TEST_ASSERTION_URN, TEST_ASSERTION_URN_2),
        Collections.emptyList()
    );
    AssertionsSummaryUtils.removeAssertionFromFailingSummary(TEST_ASSERTION_URN, summary);
    Assert.assertEquals(summary, mockAssertionsSummary(ImmutableList.of(TEST_ASSERTION_URN_2), Collections.emptyList()));
  }

  @Test
  public void testRemoveAssertionFromPassingSummary() {
    // Case 1: Has the assertion in passing.
    AssertionsSummary summary = mockAssertionsSummary(
        Collections.emptyList(),
        ImmutableList.of(TEST_ASSERTION_URN)
    );
    AssertionsSummaryUtils.removeAssertionFromPassingSummary(TEST_ASSERTION_URN, summary);
    Assert.assertEquals(summary, mockAssertionsSummary(Collections.emptyList(), Collections.emptyList()));

    // Case 2: Has the assertion in failing.
    summary = mockAssertionsSummary(
        ImmutableList.of(TEST_ASSERTION_URN),
        Collections.emptyList()
    );
    AssertionsSummaryUtils.removeAssertionFromPassingSummary(TEST_ASSERTION_URN, summary);
    Assert.assertEquals(summary, mockAssertionsSummary(ImmutableList.of(TEST_ASSERTION_URN), Collections.emptyList()));

    // Case 3: Does not have the assertion at all.
    summary = mockAssertionsSummary(
        Collections.emptyList(),
        Collections.emptyList()
    );
    AssertionsSummaryUtils.removeAssertionFromPassingSummary(TEST_ASSERTION_URN, summary);
    Assert.assertEquals(summary, mockAssertionsSummary(Collections.emptyList(), Collections.emptyList()));

    // Case 4: Has 2 items in list.
    summary = mockAssertionsSummary(
        Collections.emptyList(),
        ImmutableList.of(TEST_ASSERTION_URN, TEST_ASSERTION_URN_2)
    );
    AssertionsSummaryUtils.removeAssertionFromPassingSummary(TEST_ASSERTION_URN, summary);
    Assert.assertEquals(summary, mockAssertionsSummary(Collections.emptyList(), ImmutableList.of(TEST_ASSERTION_URN_2)));
  }

  @Test
  public void testAddAssertionToPassingSummary() {
    // Case 1: Has an assertion in passing.
    AssertionsSummary summary = mockAssertionsSummary(
        Collections.emptyList(),
        ImmutableList.of(TEST_ASSERTION_URN)
    );
    AssertionsSummaryUtils.addAssertionToPassingSummary(TEST_ASSERTION_URN_2, summary);
    AssertionsSummary expected = mockAssertionsSummary(Collections.emptyList(), ImmutableList.of(TEST_ASSERTION_URN, TEST_ASSERTION_URN_2));
    Assert.assertEquals(new HashSet<>(summary.getPassingAssertions()), new HashSet<>(expected.getPassingAssertions())); // Set comparison
    Assert.assertEquals(new HashSet<>(summary.getFailingAssertions()), new HashSet<>(expected.getFailingAssertions())); // Set comparison

    // Case 2: Has an assertion in failing.
    summary = mockAssertionsSummary(
        ImmutableList.of(TEST_ASSERTION_URN),
        Collections.emptyList()
    );
    AssertionsSummaryUtils.addAssertionToPassingSummary(TEST_ASSERTION_URN_2, summary);
    Assert.assertEquals(summary, mockAssertionsSummary(ImmutableList.of(TEST_ASSERTION_URN), ImmutableList.of(TEST_ASSERTION_URN_2)));

    // Case 3: Does not have any assertions yet
    summary = mockAssertionsSummary(
        Collections.emptyList(),
        Collections.emptyList()
    );
    AssertionsSummaryUtils.addAssertionToPassingSummary(TEST_ASSERTION_URN, summary);
    Assert.assertEquals(summary, mockAssertionsSummary(Collections.emptyList(), ImmutableList.of(TEST_ASSERTION_URN)));

    // Case 4: Duplicate additions - already has the same assertion
    summary = mockAssertionsSummary(
        Collections.emptyList(),
        ImmutableList.of(TEST_ASSERTION_URN)
    );
    AssertionsSummaryUtils.addAssertionToPassingSummary(TEST_ASSERTION_URN, summary);
    Assert.assertEquals(summary, mockAssertionsSummary(Collections.emptyList(), ImmutableList.of(TEST_ASSERTION_URN)));
  }

  @Test
  public void testAddAssertionToFailingSummary() {
    // Case 1: Has an assertion in failing.
    AssertionsSummary summary = mockAssertionsSummary(
        ImmutableList.of(TEST_ASSERTION_URN),
        Collections.emptyList()
    );
    AssertionsSummaryUtils.addAssertionToFailingSummary(TEST_ASSERTION_URN_2, summary);
    AssertionsSummary expected = mockAssertionsSummary(ImmutableList.of(TEST_ASSERTION_URN, TEST_ASSERTION_URN_2), Collections.emptyList());
    Assert.assertEquals(new HashSet<>(summary.getPassingAssertions()), new HashSet<>(expected.getPassingAssertions())); // Set comparison
    Assert.assertEquals(new HashSet<>(summary.getFailingAssertions()), new HashSet<>(expected.getFailingAssertions())); // Set comparison

    // Case 2: Has an assertion in passing.
    summary = mockAssertionsSummary(
        Collections.emptyList(),
        ImmutableList.of(TEST_ASSERTION_URN)
    );
    AssertionsSummaryUtils.addAssertionToFailingSummary(TEST_ASSERTION_URN_2, summary);
    Assert.assertEquals(summary, mockAssertionsSummary(ImmutableList.of(TEST_ASSERTION_URN_2), ImmutableList.of(TEST_ASSERTION_URN)));

    // Case 3: Does not have any assertions yet
    summary = mockAssertionsSummary(
        Collections.emptyList(),
        Collections.emptyList()
    );
    AssertionsSummaryUtils.addAssertionToFailingSummary(TEST_ASSERTION_URN, summary);
    Assert.assertEquals(summary, mockAssertionsSummary(ImmutableList.of(TEST_ASSERTION_URN), Collections.emptyList()));

    // Case 4: Duplicate additions - already has the same assertion
    summary = mockAssertionsSummary(
        ImmutableList.of(TEST_ASSERTION_URN),
        Collections.emptyList()
    );
    AssertionsSummaryUtils.addAssertionToFailingSummary(TEST_ASSERTION_URN, summary);
    Assert.assertEquals(summary, mockAssertionsSummary(ImmutableList.of(TEST_ASSERTION_URN), Collections.emptyList()));
  }

  private AssertionsSummary mockAssertionsSummary(
      final List<Urn> failingAssertions,
      final List<Urn> passingAssertions) {
    return new AssertionsSummary()
        .setFailingAssertions(new UrnArray(failingAssertions))
        .setPassingAssertions(new UrnArray(passingAssertions));
  }
}
