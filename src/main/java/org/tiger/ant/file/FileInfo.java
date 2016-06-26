package org.tiger.ant.file;

import java.util.Date;
/**
 * 
 * @time 2016年6月19日 上午8:25:42
 * @author tiger
 * @version $Rev$
 */
public class FileInfo {

  private long id;
  private Date uptime;
  private String type;
  private String name;
  private String path;
  private long length;
  public long getId() {
    return id;
  }
  public void setId(long id) {
    this.id = id;
  }
  public Date getUptime() {
    return uptime;
  }
  public void setUptime(Date uptime) {
    this.uptime = uptime;
  }
  public String getType() {
    return type;
  }
  public void setType(String type) {
    this.type = type;
  }
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }
  public long getLength() {
    return length;
  }
  public void setLength(long length) {
    this.length = length;
  }
  public String getPath() {
    return path;
  }
  public void setPath(String path) {
    this.path = path;
  }
  
  
}
