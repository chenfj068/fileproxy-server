package org.tiger.ant.file;

import java.io.File;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.tiger.ant.AntLogger;
import org.tiger.ant.ServerConfig;
import org.tiger.ant.util.JsonUtil;

public class FileCleanTask implements Runnable {

  private FileManager fileManager;

  public FileCleanTask(FileManager fileManager) {
    this.fileManager = fileManager;
  }

  @Override
  public void run() {
    Calendar now =Calendar.getInstance();
    now.add(Calendar.MINUTE, -1*ServerConfig.getInstance().getFileTTL());
    Date t=now.getTime();
    AntLogger.logger().info("file clan begin");
    List<FileInfo> fileList=Collections.EMPTY_LIST;
    do{
      fileList = fileManager.getFileInfoByUpTimeLessThan(t,200);

      AntLogger.logger().info("fetch clean fileinfo "+fileList.size());
      for (FileInfo fileInfo : fileList) {
        try {
          File f = new File(fileInfo.getPath());
          f.delete();
          fileManager.deleteFileInfoById(fileInfo.getId());
          AntLogger.logger().info("delete timeout file:[" + JsonUtil.toJson(fileInfo) + "]");
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }while(fileList.size()>0);
    


  }

}
