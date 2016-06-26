package org.tiger.ant.sub;

public class ConsumerConfig {

  private String brokerIp;
  private int brokerPort;
  private String consumerId;
  private String groupId;
  private String workDir;
  private int maxLocalCache=0;
  private String []types;
  public String getBrokerIp() {
    return brokerIp;
  }
  public void setBrokerIp(String brokerIp) {
    this.brokerIp = brokerIp;
  }
  public int getBrokerPort() {
    return brokerPort;
  }
  public void setBrokerPort(int brokerPort) {
    this.brokerPort = brokerPort;
  }
  public String[] getTypes() {
    return types;
  }
  public void setTypes(String[] types) {
    this.types = types;
  }
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
  public String getWorkDir() {
    return workDir;
  }
  public void setWorkDir(String workDir) {
    this.workDir = workDir;
  }
  public int getMaxLocalCache() {
    return maxLocalCache;
  }
  public void setMaxLocalCache(int maxLocalCache) {
    this.maxLocalCache = maxLocalCache;
  }
  
  
}
