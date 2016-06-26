package org.tiger.ant.file;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.tiger.ant.AntLogger;

public class FileCleanManager {
  private ScheduledExecutorService schex=Executors.newScheduledThreadPool(2);
  FileManager fileManager;

  public FileCleanManager(FileManager fileManager){
    this.fileManager=fileManager;
  }
  public void start(){
    schex.scheduleAtFixedRate(new FileCleanTask(fileManager), 10, 3600, TimeUnit.SECONDS);
    schex.scheduleAtFixedRate(new Runnable(){
      @Override
      public void run() {
        AntLogger.logger().info("rebuild db start");
        fileManager.rebuildDB();  
        AntLogger.logger().info("rebuild db end");
      }},1, 8, TimeUnit.HOURS);
  }
  
}
