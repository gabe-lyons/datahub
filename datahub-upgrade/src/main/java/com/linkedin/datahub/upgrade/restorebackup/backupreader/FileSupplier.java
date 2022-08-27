package com.linkedin.datahub.upgrade.restorebackup.backupreader;

import java.util.List;
import java.util.Optional;


public interface FileSupplier {

  List<Optional<String>> supplyFiles();
}
