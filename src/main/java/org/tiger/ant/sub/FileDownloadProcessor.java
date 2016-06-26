package org.tiger.ant.sub;

import java.io.File;

import org.tiger.ant.file.FileMeta;

public interface FileDownloadProcessor {

  public void onFileReceived(FileMeta meta,File file);
  
}
