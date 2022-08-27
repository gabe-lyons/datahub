package com.linkedin.datahub.upgrade.restorebackup.backupreader;

import com.amazonaws.AmazonServiceException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.paginators.ListObjectsV2Iterable;


@Slf4j
public class S3Downloader implements FileSupplier {

  public static final String READER_NAME = "S3_PARQUET";
  public static final String BACKUP_S3_BUCKET = "BACKUP_S3_BUCKET";
  public static final String BACKUP_S3_PATH = "BACKUP_S3_PATH";
  public static final String S3_REGION = "S3_REGION";
  private static final String TEMP_DIR = "/tmp/";

  private final S3Client _client;

  public S3Downloader(S3Client client) {
    _client = client;
  }

  @Override
  public List<Optional<String>> supplyFiles() {
    return saveFiles();
  }

  private List<Optional<String>> saveFiles() {
    String bucket = System.getenv(BACKUP_S3_BUCKET);
    String path = System.getenv(BACKUP_S3_PATH);
    if (bucket == null || path == null) {
      throw new IllegalArgumentException(
          "BACKUP_S3_BUCKET and BACKUP_S3_PATH must be set to run RestoreBackup through S3");
    }
    List<String> s3Keys = getFileKey(bucket, path);

    return s3Keys.stream()
        .map(key -> saveFile(bucket, key))
        .collect(Collectors.toList());
  }

  private List<String> getFileKey(String bucket, String path) {
    ListObjectsV2Request request = ListObjectsV2Request.builder().bucket(bucket).prefix(path).build();
    ListObjectsV2Iterable objectListResult = _client.listObjectsV2Paginator(request);
    return objectListResult.contents()
        .stream()
        .map(S3Object::key)
        .collect(Collectors.toList());
  }

  private Optional<String> saveFile(String bucket, String key) {
    log.info("Downloading {} from S3 bucket {}...", key, bucket);
    String[] path = key.split("/");
    String localFilePath;
    if (path.length > 0) {
      localFilePath = TEMP_DIR + path[path.length - 1];
    } else {
      localFilePath = "backup.gz.parquet";
    }

    try (ResponseInputStream<GetObjectResponse> o =
        _client.getObject(GetObjectRequest.builder().bucket(bucket).key(key).build());
        FileOutputStream fos = FileUtils.openOutputStream(new File(localFilePath))) {
      byte[] readBuf = new byte[1024];
      int readLen = 0;
      while ((readLen = o.read(readBuf)) > 0) {
        fos.write(readBuf, 0, readLen);
      }
      return Optional.of(localFilePath);
    } catch (AmazonServiceException e) {
      System.err.println(e.getErrorMessage());
      return Optional.empty();
    } catch (IOException e) {
      System.err.println(e.getMessage());
      return Optional.empty();
    }
  }
}
