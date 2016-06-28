package org.tiger.ant.client;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.tiger.ant.AntLogger;
import org.tiger.ant.file.FileMeta;
import org.tiger.ant.sub.ConsumerConfig;
import org.tiger.ant.sub.FileConsumer;
import org.tiger.ant.sub.FileDownloadProcessor;
import org.tiger.ant.util.FileStoreUtil;

import com.mlog.oss.server.SimpleUploadServer;

public class OSSUploadClient {

  public static void main(String[] args) throws InterruptedException {

    String host=args[0];
    String types[]=args[1].split(",");
    String workdir=System.getProperty("user.dir")+File.separator+"work";
    File f = new File(workdir);
    f.mkdirs();
   
    ExecutorService ex=Executors.newFixedThreadPool(types.length);
    for(int i=0;i<types.length;i++){
      String ss[]=host.split(":");
      final  ConsumerConfig config = new ConsumerConfig();
      config.setBrokerIp(ss[0]);
      config.setBrokerPort(Integer.parseInt(ss[1]));
      config.setGroupId("ossgroup-"+types[i]);
      config.setConsumerId("ossconsumer-"+types[i]);
      config.setMaxLocalCache(1);
      config.setTypes(new String[]{types[i]});
      config.setWorkDir(workdir);
      
      ex.submit(new Runnable(){
        @Override
        public void run() {
         
          FileConsumer consumer = new FileConsumer(config,new UploadOSSProcessor());
          try {
            consumer.run();
          } catch (InterruptedException e) {
            e.printStackTrace();
            return;
          }
        }
        
      });
    }
    
  }

  static class UploadOSSProcessor implements FileDownloadProcessor{

    @Override
    public void onFileReceived(FileMeta meta, File file) {
        AntLogger.logger().info("file receive success "+file.getName());
        SimpleUploadServer sus = new SimpleUploadServer("SHANGHAI","store");
        String path=FileStoreUtil.getStorePath("rawdata", meta);
        
        sus.upload(path, file);  
        file.delete();
        AntLogger.logger().info("file upload success "+file.getName());
      }
      
    }


}
