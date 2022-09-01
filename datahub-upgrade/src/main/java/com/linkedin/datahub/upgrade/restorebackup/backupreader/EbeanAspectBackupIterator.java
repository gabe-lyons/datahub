package com.linkedin.datahub.upgrade.restorebackup.backupreader;

import java.io.Closeable;


/**
 * Base interface for iterators that retrieves EbeanAspectV2 objects
 * This allows us to restore from backups of various format
 */
public interface EbeanAspectBackupIterator<T> extends Closeable {

  T getNextReader();
}
