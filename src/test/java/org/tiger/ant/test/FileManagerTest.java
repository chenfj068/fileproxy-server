package org.tiger.ant.test;

import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.tiger.ant.file.FileInfo;
import org.tiger.ant.file.FileManager;
import org.tiger.ant.file.FileManagerImpl;
import org.tiger.ant.file.dao.DAL;

public class FileManagerTest {

  @Test
  public void testDeleteFileInfo() throws Exception{
    DAL.initialize();
    FileManager fm = new FileManagerImpl();
    List<FileInfo> list=fm.getFileInfoByUpTimeLessThan(new Date(),200);
    System.out.println(list.size());
   FileInfo f= fm.getFileInfoById(1);
   System.out.println(f.getUptime());
  }
}
