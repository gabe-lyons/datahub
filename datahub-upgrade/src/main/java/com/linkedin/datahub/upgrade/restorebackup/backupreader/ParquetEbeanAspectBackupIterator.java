package com.linkedin.datahub.upgrade.restorebackup.backupreader;

import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


/**
 * Iterator to retrieve EbeanAspectV2 objects from the ParquetReader
 * Converts the avro GenericRecord object into EbeanAspectV2
 */
@Slf4j
@RequiredArgsConstructor
public class ParquetEbeanAspectBackupIterator implements EbeanAspectBackupIterator<ParquetReaderWrapper> {
  private final List<ParquetReaderWrapper> _parquetReaders;
  private int currentReaderIndex = 0;

  @Override
  public ParquetReaderWrapper getNextReader() {
      if (currentReaderIndex >= _parquetReaders.size()) {
        return null;
      }
      try {
      return _parquetReaders.get(currentReaderIndex);
      } finally {
      currentReaderIndex++;
    }
  }

  @Override
  public void close() {
    _parquetReaders.forEach(reader -> {
      try {
        reader.close();
      } catch (IOException e) {
        log.error("Error while closing parquet reader", e);
      }
    });
  }
}
