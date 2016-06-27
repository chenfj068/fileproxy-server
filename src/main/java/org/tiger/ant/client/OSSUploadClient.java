package org.tiger.ant.client;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.tiger.ant.AntLogger;
import org.tiger.ant.file.FileMeta;
import org.tiger.ant.sub.ConsumerConfig;
import org.tiger.ant.sub.FileConsumer;
import org.tiger.ant.sub.FileDownloadProcessor;

import com.mlog.oss.server.SimpleUploadServer;

public class OSSUploadClient {

  public static void main(String[] args) throws InterruptedException {

    String host=args[0];
    String type=args[1];
    int thunm=Integer.parseInt(args[2]);
    String workdir=System.getProperty("user.dir")+File.separator+"work";
    File f = new File(workdir);
    f.mkdirs();
  final  ConsumerConfig config = new ConsumerConfig();
    String ss[]=host.split(":");
    config.setBrokerIp(ss[0]);
    config.setBrokerPort(Integer.parseInt(ss[1]));
    config.setGroupId("group-ec");
    config.setConsumerId("consumer-ec");
    config.setMaxLocalCache(1);
    config.setTypes(new String[]{type});
    config.setWorkDir(workdir);
    ExecutorService ex=Executors.newFixedThreadPool(thunm);
    for(int i=0;i<thunm;i++){
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
        String type=meta.getFtype();
        String distdir=meta.getDistDir();
        String path=type+File.separator+distdir+File.separator+file.getName();
        sus.upload(path, file);  
        file.delete();
        AntLogger.logger().info("file upload success "+file.getName());
      }
      
    }


}
