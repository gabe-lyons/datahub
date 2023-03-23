package com.linkedin.metadata.search;

import com.linkedin.common.urn.Urn;
import com.linkedin.metadata.graph.LineageDirection;
import lombok.Data;

import java.time.Instant;
import java.time.temporal.ChronoUnit;


@Data
public class EntityLineageResultCacheKey {
  private final Urn sourceUrn;
  private final LineageDirection direction;
  private final Long startTimeMillis;
  private final Long endTimeMillis;
  private final Integer maxHops;

  public static EntityLineageResultCacheKey from(Urn sourceUrn, LineageDirection direction, Long startTimeMillis,
      Long endTimeMillis, Integer maxHops) {
    return new EntityLineageResultCacheKey(sourceUrn, direction,
        startTimeMillis == null ? null
            : Instant.ofEpochMilli(startTimeMillis).truncatedTo(ChronoUnit.DAYS).toEpochMilli(),
        endTimeMillis == null ? null : Instant.ofEpochMilli(endTimeMillis + 86400000).truncatedTo(ChronoUnit.DAYS).toEpochMilli(),
        maxHops);
  }
}
