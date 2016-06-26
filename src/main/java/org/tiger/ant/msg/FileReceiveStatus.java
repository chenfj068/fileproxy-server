package org.tiger.ant.msg;

import org.tiger.ant.file.FileMeta;

public class FileReceiveStatus {

  private FileMeta fileMeta;
  private boolean success;
  public FileMeta getFileMeta() {
    return fileMeta;
  }
  public void setFileMeta(FileMeta fileMeta) {
    this.fileMeta = fileMeta;
  }
  public boolean isSuccess() {
    return success;
  }
  public void setSuccess(boolean success) {
    this.success = success;
  }
  
  
  
}
