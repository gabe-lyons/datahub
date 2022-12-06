package com.linkedin.datahub.upgrade.restorebackup.backupreader;

<<<<<<< HEAD
=======
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
>>>>>>> oss_master
import com.linkedin.metadata.entity.ebean.EbeanAspectV2;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
<<<<<<< HEAD
=======
import org.apache.avro.generic.GenericFixed;
>>>>>>> oss_master
import org.apache.avro.generic.GenericRecord;
import org.apache.parquet.hadoop.ParquetReader;

@Slf4j
<<<<<<< HEAD
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
=======
public class ParquetReaderWrapper extends ReaderWrapper<GenericRecord> {

  private final static long NANOS_PER_MILLISECOND = 1000000;
  private final static long MILLIS_IN_DAY = 86400000;
  private final static long JULIAN_EPOCH_OFFSET_DAYS = 2440588;

  private final ParquetReader<GenericRecord> _parquetReader;

  public ParquetReaderWrapper(ParquetReader<GenericRecord> parquetReader, String fileName) {
    super(fileName);
    _parquetReader = parquetReader;
  }

  @Override
  boolean isLatestVersion(GenericRecord record) {
    return (Long) record.get("version") == 0L;
  }

  @Override
  GenericRecord read() throws IOException {
    return _parquetReader.read();
  }

  EbeanAspectV2 convertRecord(GenericRecord record) {

    long ts;
    if (record.get("createdon") instanceof GenericFixed) {
      ts = convertFixed96IntToTs((GenericFixed) record.get("createdon"));
    } else {
      ts = (Long) record.get("createdon");
    }

    return new EbeanAspectV2(record.get("urn").toString(), record.get("aspect").toString(),
        (Long) record.get("version"), record.get("metadata").toString(),
        Timestamp.from(Instant.ofEpochMilli(ts / 1000)), record.get("createdby").toString(),
>>>>>>> oss_master
        Optional.ofNullable(record.get("createdfor")).map(Object::toString).orElse(null),
        Optional.ofNullable(record.get("systemmetadata")).map(Object::toString).orElse(null));
  }

<<<<<<< HEAD
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
=======
  private long convertFixed96IntToTs(GenericFixed createdon) {
    // From https://github.com/apache/parquet-format/pull/49/filesParquetTimestampUtils.java
    // and ParquetTimestampUtils.java from https://github.com/kube-reporting/presto/blob/master/presto-parquet/
    // src/main/java/io/prestosql/parquet/ParquetTimestampUtils.java

    byte[] bytes = createdon.bytes(); // little endian encoding - need to invert byte order
    long timeOfDayNanos = Longs.fromBytes(bytes[7], bytes[6], bytes[5], bytes[4], bytes[3], bytes[2], bytes[1], bytes[0]);
    int julianDay = Ints.fromBytes(bytes[11], bytes[10], bytes[9], bytes[8]);
    return ((julianDay - JULIAN_EPOCH_OFFSET_DAYS) * MILLIS_IN_DAY) + (timeOfDayNanos / NANOS_PER_MILLISECOND);
  }

  @Override
  public void close() throws IOException {
    _parquetReader.close();
>>>>>>> oss_master
  }
}
