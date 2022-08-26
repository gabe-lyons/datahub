package com.linkedin.datahub.upgrade.restorebackup.backupreader;

import com.linkedin.metadata.entity.ebean.EbeanAspectV2;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.generic.GenericRecord;
import org.apache.parquet.hadoop.ParquetReader;


/**
 * Iterator to retrieve EbeanAspectV2 objects from the ParquetReader
 * Converts the avro GenericRecord object into EbeanAspectV2
 */
@Slf4j
@RequiredArgsConstructor
public class ParquetEbeanAspectBackupIterator implements EbeanAspectBackupIterator<ParquetReader<GenericRecord>> {
  private final List<ParquetReader<GenericRecord>> _parquetReaders;
  private int currentReaderIndex = 0;
  private long totalTimeSpentInRead = 0L;
  private long lastTimeLogged = 0L;
  private int recordsSkipped = 0;
  private int recordsFailed = 0;
  private long totalTimeSpentInConvert = 0L;

  @Override
  public EbeanAspectV2 next(ParquetReader<GenericRecord> parquetReader) {

    try {
      long readStart = System.nanoTime();
      GenericRecord record = parquetReader.read();
      long readEnd = System.nanoTime();
      totalTimeSpentInRead += readEnd - readStart;

      while ((record != null) && ((Long) record.get("version") !=0L)) {
        recordsSkipped +=1;
        readStart = System.nanoTime();
        record = parquetReader.read();
        readEnd = System.nanoTime();
        totalTimeSpentInRead += readEnd - readStart;
      }
      if ((readEnd - lastTimeLogged) > 1000*1000*1000*5) {
        // print every 5 seconds
        printStat("Running: ");
        lastTimeLogged = readEnd;
      }
      if (record == null) {
        printStat("Closing: ");
        parquetReader.close();
        return null;
      }
      long convertStart = System.nanoTime();
      final EbeanAspectV2 ebeanAspectV2 = convertRecord(record);
      long convertEnd = System.nanoTime();
      this.totalTimeSpentInConvert += convertEnd - convertStart;
      return ebeanAspectV2;
    } catch (Exception e) {
      log.error("Error while reading backed up aspect", e);
      this.recordsFailed ++;
      return null;
    }
  }

  private void printStat(String prefix) {
    log.info("{} Reader {} of {}. Stats: Total millis spent in reading: {}, records skipped: {}, records failed: {}", prefix, currentReaderIndex, _parquetReaders.size(), totalTimeSpentInRead / 1000 / 1000, recordsSkipped, recordsFailed);
  }

  @Override
  public ParquetReader<GenericRecord> getNextReader() {
    if (currentReaderIndex >= _parquetReaders.size()) {
      return null;
    }
    ParquetReader<GenericRecord> parquetReader = _parquetReaders.get(currentReaderIndex);
    currentReaderIndex++;
    return parquetReader;
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

  private EbeanAspectV2 convertRecord(GenericRecord record) {
    return new EbeanAspectV2(record.get("urn").toString(), record.get("aspect").toString(),
        (Long) record.get("version"), record.get("metadata").toString(),
        Timestamp.from(Instant.ofEpochMilli((Long) record.get("createdon") / 1000)), record.get("createdby").toString(),
        Optional.ofNullable(record.get("createdfor")).map(Object::toString).orElse(null),
        Optional.ofNullable(record.get("systemmetadata")).map(Object::toString).orElse(null));
  }
}
