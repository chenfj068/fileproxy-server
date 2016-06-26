package org.tiger.ant.test;

import java.io.File;

import org.junit.Test;
import org.tiger.ant.file.FileMeta;
import org.tiger.ant.sub.ConsumerConfig;
import org.tiger.ant.sub.FileConsumer;
import org.tiger.ant.sub.FileDownloadProcessor;

public class FileConsumerTest {

  @Test
  public void testFileConsume() throws InterruptedException{
    ConsumerConfig config = new ConsumerConfig();
    config.setBrokerIp("54.223.81.103");
    config.setBrokerPort(9000);
    config.setGroupId("group1");
    config.setConsumerId("consumer1");
    config.setMaxLocalCache(1);
    config.setTypes(new String[]{"gfs"});
    config.setWorkDir("/Users/tiger/consumer");
    FileConsumer consumer = new FileConsumer(config,new FileDownloadProcessor(){
      @Override
      public void onFileReceived(FileMeta meta, File file) {
        try {
          System.out.println("receive file "+file.getAbsolutePath());
          Thread.sleep(80*1000);
          System.out.println("file processed "+file.getAbsolutePath());
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
      
    });
    consumer.run();
    
  }
}
