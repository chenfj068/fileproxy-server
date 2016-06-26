package org.tiger.ant.file;

import java.util.Date;
/**
 * 
 * @time 2016年6月19日 上午8:47:56
 * @author tiger
 * @version $Rev$
 */
public class Consumer {

 private String consumerId;
 private String groupId;
 private Date regTime;//first time get file
 private Date lastUpTime;
 private String ftype;
 
public String getConsumerId() {
  return consumerId;
}
public void setConsumerId(String consumerId) {
  this.consumerId = consumerId;
}
public String getGroupId() {
  return groupId;
}
public void setGroupId(String groupId) {
  this.groupId = groupId;
}
public Date getLastUpTime() {
  return lastUpTime;
}
public void setLastUpTime(Date lastUpTime) {
  this.lastUpTime = lastUpTime;
}
public Date getRegTime() {
  return regTime;
}
public void setRegTime(Date regTime) {
  this.regTime = regTime;
}
public String getFtype() {
  return ftype;
}
public void setFtype(String ftype) {
  this.ftype = ftype;
}
 
 

}
