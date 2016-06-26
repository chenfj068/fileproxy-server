package org.tiger.ant.msg;
/**
 * byte flag=4
 * @time 2016年6月16日 下午4:46:19
 * @author tiger
 * @version $Rev$
 */
public class FileFetchRequest {
  
  private String consumerId;
  private String groupId;
  private String type;
  private int offset;
  
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
  public String getType() { 
    return type;
  }
  public void setType(String type) {
    this.type = type;
  }
  public int getOffset() {
    return offset;
  }
  public void setOffset(int offset) {
    this.offset = offset;
  }
  
  
}
