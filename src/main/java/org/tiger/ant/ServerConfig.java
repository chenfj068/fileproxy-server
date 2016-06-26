package org.tiger.ant;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.tiger.ant.util.JsonUtil;
/**
 * 
 * @time 2016年6月13日 下午4:00:16
 * @author tiger
 * @version $Rev$
 */
public class ServerConfig {

  private int mode;//0 proxy,1 pub/sub,2 both
  private int fileTTL=120;//minutes
  private static ServerConfig instance;
  private ProxyConfig proxyConfig;
  private int port;
  private String ip;
  private String dbDir;
  static {
    try {
      loadConfig();
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("load config failed",e);
    }
  }
  private String workdir;
  
  
  public String getWorkdir() {
    return this.workdir;
  }


  private ServerConfig(){}
  
  public static ServerConfig getInstance(){
    return instance;
  }
  private static void loadConfig() throws IOException {
    InputStream input = ServerConfig.class.getResourceAsStream("/server.json");
    Map<String, Object> map = JsonUtil.fromJsonInputStream(Map.class, input);
    input.close();

    ServerConfig conf = new ServerConfig();
    conf.workdir = (String) map.get("workdir");
    conf.mode=(int)map.get("mode");
    conf.ip=(String)map.get("ip");
    conf.port=(int)map.get("port");
    conf.dbDir=(String)map.get("dbDir");
    if(map.containsKey("fileTTL"))
      conf.fileTTL=(int)map.get("fileTTL");
    if(conf.mode==0||conf.mode==2){
      conf.proxyConfig=ProxyConfig.getInstance();
    }
    instance=conf;
  }
  
  public String getTmpDir(){
    return this.workdir+File.separator+"tmp";
  }

  public String getDataDir(){
    return this.workdir+File.separator+"data";
  }

  public int getMode() {
    return mode;
  }

  public void setMode(int mode) {
    this.mode = mode;
  }

  public ProxyConfig getProxyConfig(){
    return this.proxyConfig;
  }

  public int getPort() {
    return port;
  }

  public String getIp() {
    return ip;
  }




  public int getFileTTL() {
    return fileTTL;
  }




  public void setFileTTL(int fileTTL) {
    this.fileTTL = fileTTL;
  }




  public String getDbDir() {
    return dbDir;
  }




  public void setDbDir(String dbDir) {
    this.dbDir = dbDir;
  }

  
 

}
