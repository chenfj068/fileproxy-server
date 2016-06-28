package org.tiger.ant.upstream;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.tiger.ant.AntLogger;
import org.tiger.ant.ProxyConfig;
import org.tiger.ant.ServerConfig;
import org.tiger.ant.client.FileConnection;
import org.tiger.ant.file.FileManager;
import org.tiger.ant.file.FileUpstream;
import org.tiger.ant.util.FileStoreUtil;
import org.tiger.ant.util.JsonUtil;

public class FileUpstreamManager{

  private ServerConfig serverConfig = ServerConfig.getInstance();
  private ProxyConfig proxyConfig=serverConfig.getProxyConfig();
  private ExecutorService ex;
  private FileManager fileManager;

  public FileUpstreamManager(FileManager fileManager) {
    this.fileManager=fileManager;
  }


  public void start() {
    int threadCount=serverConfig.getProxyConfig().getUpthreadNumber();
    ex = Executors.newFixedThreadPool(threadCount);
    for(int i=0;i<threadCount;i++){
      UpStreamWorker worker = new UpStreamWorker();
      ex.submit(worker);
    }
    AntLogger.logger().debug("fileupstream manager started with "+threadCount+" thread");
  }

  public void stop() {
    ex.shutdown();
  }
  
  
  class UpStreamWorker implements Runnable {
    FileConnection fileConnection;
    String lastType=null;
    String lastHost=null;
    

    @Override
    public void run() {
      while (true) {
        try {
          FileUpstream fm = fileManager.acquireNextFileUpStream();
          if(fm==null){
//            AntLogger.logger().debug("no more upstream file,sleep 2s");
            Thread.currentThread().sleep(2000);
            continue;
          }
          File file = new File(fm.getPath());
          if(!file.exists()){
            AntLogger.logger().warn("upstream file not exists ["+JsonUtil.toJson(fm)+"]");
            fileManager.onFileUpstreamSuccess(fm);
            Thread.sleep(5);
            continue;
          }
          if(fm.getType().equals(this.lastType)&&this.fileConnection!=null){
            try{
            fileConnection.sendFile(fm.getType(), FileStoreUtil.getDistDir(fm.getPath(), fm.getType()),new File(fm.getPath()), true);
            fileManager.onFileUpstreamSuccess(fm);
            AntLogger.logger().debug("upstream fileupstream success["+JsonUtil.toJson(fm)+"]");
            }catch(IOException ioe){
              AntLogger.logger().error("upstream failed ["+fm.getPath()+"->"+lastHost+"]");
              fileConnection.close();
              fileConnection=null;
              this.lastHost=null;
              fileManager.onFileUpstreamFailed(fm);
              Thread.sleep(2000);
            }
           
          }else{
            if(fileConnection!=null){
              fileConnection.close();
            }
            this.lastType=fm.getType();
            if(proxyConfig.getUpstreamServers(fm.getType())==null){
              fileManager.onFileUpstreamSuccess(fm);
              AntLogger.logger().warn("no upstream servers found ["+JsonUtil.toJson(fm)+"]");
              Thread.currentThread().sleep(50);
              continue;
            }
            lastHost=proxyConfig.getUpstreamServers(fm.getType())[0];
            String ss[] = this.lastHost.split(":");
            fileConnection=FileConnection.openConnection(ss[0], Integer.parseInt(ss[1]));
            try{
            fileConnection.sendFile(fm.getType(), FileStoreUtil.getDistDir(fm.getPath(), fm.getType()),new File(fm.getPath()), true);
            fileManager.onFileUpstreamSuccess(fm);
            AntLogger.logger().debug("upstream fileupstream success["+JsonUtil.toJson(fm)+"]");
            }catch(Exception ioe){
              AntLogger.logger().error("upstream failed ["+fm.getPath()+"->"+lastHost+"]");
              fileConnection.close();
              fileConnection=null;
              fileManager.onFileUpstreamFailed(fm);
              this.lastHost=null;
              Thread.sleep(2000);
            }
          }
        
        } catch (Exception e) {
          AntLogger.logger().error(e);
          e.printStackTrace();
        }
      }

    }

  }

 
}
