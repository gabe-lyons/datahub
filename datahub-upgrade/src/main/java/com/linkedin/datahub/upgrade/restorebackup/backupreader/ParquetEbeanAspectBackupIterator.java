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

//  @Override
//  public EbeanAspectV2 next(ParquetReader<GenericRecord> parquetReader) {
//
//    try {
//      long readStart = System.nanoTime();
//      GenericRecord record = parquetReader.read();
//      long readEnd = System.nanoTime();
//      totalTimeSpentInRead += readEnd - readStart;
//
//      while ((record != null) && ((Long) record.get("version") != 0L)) {
//        recordsSkipped += 1;
//        readStart = System.nanoTime();
//        record = parquetReader.read();
//        readEnd = System.nanoTime();
//        totalTimeSpentInRead += readEnd - readStart;
//      }
//      if ((readEnd - lastTimeLogged) > 1000 * 1000 * 1000 * 5) {
//        // print every 5 seconds
//        printStat("Running: ");
//        lastTimeLogged = readEnd;
//      }
//      if (record == null) {
//        printStat("Closing: ");
//        parquetReader.close();
//        return null;
//      }
//      long convertStart = System.nanoTime();
//      final EbeanAspectV2 ebeanAspectV2 = convertRecord(record);
//      long convertEnd = System.nanoTime();
//      this.totalTimeSpentInConvert += convertEnd - convertStart;
//      this.recordsProcessed++;
//      return ebeanAspectV2;
//    } catch (Exception e) {
//      log.error("Error while reading backed up aspect", e);
//      this.recordsFailed++;
//      return null;
//    }
//  }

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
