package org.tiger.ant.file;

import java.util.Date;

/**
 * 
 * @time 2016年6月19日 上午8:35:26
 * @author tiger
 * @version $Rev$
 */
public class FileUpstream {

  public static final int NOT_UPSTREAM=0;
  public static final int IS_UPSTREAMING=1;
  public static final int UP_STREAM_SUCCESS=2;
  public static final int UP_STREAM_FAILED=3;
 
  private long id;
  private long fileid;
  private String type;
  private String fileName;
  private String path;
  private Date uptime;
  private Date upstreamTime;
  private int status=0;//0 未上传，1上传中，2上传完成(删除该记录)
  public long getId() {
    return id;
  }
  public void setId(long id) {
    this.id = id;
  }
  public String getType() {
    return type;
  }
  public void setType(String type) {
    this.type = type;
  }
  public String getFileName() {
    return fileName;
  }
  public void setFileName(String fileName) {
    this.fileName = fileName;
  }
  public String getPath() {
    return path;
  }
  public void setPath(String path) {
    this.path = path;
  }
  
  public int getStatus() {
    return status;
  }
  public void setStatus(int status) {
    this.status = status;
  }
  public Date getUpstreamTime() {
    return upstreamTime;
  }
  public void setUpstreamTime(Date upstreamTime) {
    this.upstreamTime = upstreamTime;
  }
  public long getFileid() {
    return fileid;
  }
  public void setFileid(long fileid) {
    this.fileid = fileid;
  }
  public Date getUptime() {
    return uptime;
  }
  public void setUptime(Date uptime) {
    this.uptime = uptime;
  }
    
  
}
