package com.linkedin.datahub.upgrade.restorebackup.backupreader;

import com.linkedin.metadata.entity.ebean.EbeanAspectV2;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.generic.GenericRecord;
import org.apache.parquet.hadoop.ParquetReader;

@Slf4j
public class ParquetReaderWrapper {

  private final ParquetReader<GenericRecord> _parquetReader;
  private final String _fileName;
  private long totalTimeSpentInRead = 0L;
  private long lastTimeLogged = 0L;
  private int recordsSkipped = 0;
  private int recordsFailed = 0;
  private int recordsProcessed = 0;
  private long totalTimeSpentInConvert = 0L;

  public ParquetReaderWrapper(ParquetReader<GenericRecord> parquetReader, String fileName) {
    _parquetReader = parquetReader;
    _fileName = fileName;
  }

  public EbeanAspectV2 next() {

    try {
      long readStart = System.nanoTime();
      GenericRecord record = _parquetReader.read();
      long readEnd = System.nanoTime();
      totalTimeSpentInRead += readEnd - readStart;

      while ((record != null) && ((Long) record.get("version") != 0L)) {
        recordsSkipped += 1;
        readStart = System.nanoTime();
        record = _parquetReader.read();
        readEnd = System.nanoTime();
        totalTimeSpentInRead += readEnd - readStart;
      }
      if ((readEnd - lastTimeLogged) > 1000 * 1000 * 1000 * 5) {
        // print every 5 seconds
        printStat("Running: ");
        lastTimeLogged = readEnd;
      }
      if (record == null) {
        printStat("Closing: ");
        _parquetReader.close();
        return null;
      }
      long convertStart = System.nanoTime();
      final EbeanAspectV2 ebeanAspectV2 = convertRecord(record);
      long convertEnd = System.nanoTime();
      this.totalTimeSpentInConvert += convertEnd - convertStart;
      this.recordsProcessed++;
      return ebeanAspectV2;
    } catch (Exception e) {
      log.error("Error while reading backed up aspect", e);
      this.recordsFailed++;
      return null;
    }
  }

  private EbeanAspectV2 convertRecord(GenericRecord record) {
    return new EbeanAspectV2(record.get("urn").toString(), record.get("aspect").toString(),
        (Long) record.get("version"), record.get("metadata").toString(),
        Timestamp.from(Instant.ofEpochMilli((Long) record.get("createdon") / 1000)), record.get("createdby").toString(),
        Optional.ofNullable(record.get("createdfor")).map(Object::toString).orElse(null),
        Optional.ofNullable(record.get("systemmetadata")).map(Object::toString).orElse(null));
  }

  private void printStat(String prefix) {
    log.info("{} Reader {}. Stats: records processed: {}, Total millis spent in reading: {}, records skipped: {},"
            + " records failed: {}, Total millis in convert: {}", prefix, _fileName,
        recordsProcessed, totalTimeSpentInRead / 1000 / 1000, recordsSkipped, recordsFailed,
        totalTimeSpentInConvert / 1000 / 1000);
  }

  public String getFileName() {
    return this._fileName;
  }

  public void close() throws IOException {
      _parquetReader.close();
  }
}
