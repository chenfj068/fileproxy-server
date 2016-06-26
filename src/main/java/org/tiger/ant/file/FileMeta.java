package org.tiger.ant.file;

public class FileMeta {

  private String fileName;
  private long length;
  private String ftype;
  private String client;
  private String crc32CheckSum;
  private boolean checkSum=false;
  
  public String getFileName() {
    return fileName;
  }
  public void setFileName(String fileName) {
    this.fileName = fileName;
  }
  public long getLength() {
    return length;
  }
  public void setLength(long length) {
    this.length = length;
  }
  public String getFtype() {
    return ftype;
  }
  public void setFtype(String ftype) {
    this.ftype = ftype;
  }
  public String getClient() {
    return client;
  }
  public void setClient(String client) {
    this.client = client;
  }
  public String getCrc32CheckSum() {
    return crc32CheckSum;
  }
  public void setCrc32CheckSum(String crc32CheckSum) {
    this.crc32CheckSum = crc32CheckSum;
  }
  public boolean isCheckSum() {
    return checkSum;
  }
  public void setCheckSum(boolean checkSum) {
    this.checkSum = checkSum;
  }
  
  
  
}
