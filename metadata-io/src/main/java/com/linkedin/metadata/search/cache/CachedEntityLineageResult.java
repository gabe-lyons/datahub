package com.linkedin.metadata.search.cache;

import com.linkedin.data.DataMap;
import com.linkedin.metadata.graph.EntityLineageResult;
import lombok.Data;


@Data
public class CachedEntityLineageResult {
  private final DataMap entityLineageResult;
  private final long timestamp;
}
