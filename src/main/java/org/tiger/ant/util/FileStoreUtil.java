package org.tiger.ant.util;

import java.io.File;

import org.tiger.ant.file.FileMeta;


public class FileStoreUtil {

  public static String getStorePath(String rootDir,FileMeta meta){
    String path=rootDir+File.separator+meta.getFtype()+File.separator+meta.getFileName();
    return path;
  }
  
  public static String getTempFileStorePath(String rootDir,FileMeta meta,String postfix){
    return getStorePath(rootDir,meta)+File.separator+"_"+System.currentTimeMillis()+postfix;
  }
  
  public static String getTempFileStorePath(String rootDir,FileMeta meta){
    return getTempFileStorePath(rootDir,meta,".tmp");
  }
}
