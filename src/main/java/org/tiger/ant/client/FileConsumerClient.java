package org.tiger.ant.client;

import java.io.File;

import org.tiger.ant.file.FileMeta;
import org.tiger.ant.sub.ConsumerConfig;
import org.tiger.ant.sub.FileConsumer;
import org.tiger.ant.sub.FileDownloadProcessor;

public class FileConsumerClient {

  public static void main(String[] args) throws InterruptedException {
    String host=args[0];
    String type=args[1];
    String workdir=System.getProperty("user.dir")+File.separator+"work";
    File f = new File(workdir);
    f.mkdirs();
    ConsumerConfig config = new ConsumerConfig();
    String ss[]=host.split(":");
    config.setBrokerIp(ss[0]);
    config.setBrokerPort(Integer.parseInt(ss[1]));
    config.setGroupId("group1");
    config.setConsumerId("consumer1");
    config.setMaxLocalCache(1);
    config.setTypes(new String[]{type});
    config.setWorkDir(workdir);
    FileConsumer consumer = new FileConsumer(config,new FileDownloadProcessor(){
      @Override
      public void onFileReceived(FileMeta meta, File file) {
        System.out.println("file received "+file.getAbsolutePath());
      }
      
    });
    consumer.run();

  }

}
