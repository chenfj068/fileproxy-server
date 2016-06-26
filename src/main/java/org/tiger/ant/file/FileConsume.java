package org.tiger.ant.file;

import java.util.Date;

/**
 * 
 * @time 2016年6月19日 上午8:50:32
 * @author tiger
 * @version $Rev$
 */
public class FileConsume {

  public static final int NOT_CONSUMED=0;
  public static final int IN_CONSUME=1;
  public static final int CONSUME_SUCCESS=2;
  public static final int CONSUME_FAILED=3;
  private String groupId;//consumer goupdid
  private long id;//fileid
  private long fileid;
  private String fileName;
  private String type;
  private String path;
  private Date uptime;
  private int status=NOT_CONSUMED;//0 acquired 
  private String consumerId;
  private Date consumeTime;
  public String getGroupId() {
    return groupId;
  }
  public void setGroupId(String groupId) {
    this.groupId = groupId;
  }
  public long getId() {
    return id;
  }
  public void setId(long id) {
    this.id = id;
  }
  public String getFileName() {
    return fileName;
  }
  public void setFileName(String fileName) {
    this.fileName = fileName;
  }
  public String getType() {
    return type;
  }
  public void setType(String type) {
    this.type = type;
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
  public String getConsumerId() {
    return consumerId;
  }
  public void setConsumerId(String consumerId) {
    this.consumerId = consumerId;
  }
  public Date getConsumeTime() {
    return consumeTime;
  }
  public void setConsumeTime(Date consumeTime) {
    this.consumeTime = consumeTime;
  }
  public Date getUptime() {
    return uptime;
  }
  public void setUptime(Date uptime) {
    this.uptime = uptime;
  }
  public long getFileid() {
    return fileid;
  }
  public void setFileid(long fileid) {
    this.fileid = fileid;
  }
  
  
  
}
