package org.tiger.ant.util;

import java.io.File;

import org.tiger.ant.file.FileMeta;


public class FileStoreUtil {

  public static String getStorePath(String rootDir, FileMeta meta) {
    String distDir = meta.getDistDir();
    if (distDir.endsWith("/")) {
      distDir = distDir.substring(0, distDir.length() - 1);
    }
    String dir = "";
    if (distDir != "") {
      dir = rootDir + File.separator + meta.getFtype()+File.separator+distDir;
    } else {
      dir = rootDir + File.separator + meta.getFtype();
    }
    File _dir = new File(dir);
    _dir.mkdirs();
    return dir + File.separator + meta.getFileName();

  }

  public static String getTempFileStorePath(String rootDir, FileMeta meta, String postfix) {

    return getStorePath(rootDir, meta) + "_" + System.currentTimeMillis()
        + postfix;

  }

  public static String getTempFileStorePath(String rootDir, FileMeta meta) {
    return getTempFileStorePath(rootDir, meta, ".tmp");
  }
  
  public static String getDistDir(String filepath,String type){
    File file = new File(filepath);
    String dir=file.getParentFile().getAbsolutePath();
    return dir.substring(dir.indexOf(type)+type.length()+1);
    
  }
  
}
