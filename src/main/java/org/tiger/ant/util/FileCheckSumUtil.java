package org.tiger.ant.util;

import java.io.File;
import java.io.IOException;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;

public class FileCheckSumUtil {

//  public static String getMD5CheckSum(File file) throws IOException{
//    FileInputStream fis = new FileInputStream(file);
//    String md5 = org.apache.commons.codec.digest.DigestUtils.md5Hex(fis);
//    fis.close();
//    return md5;
//  }
  
  public static String getCrc32CheckSum(File file) throws IOException{
    HashCode hc = Files.hash(file, Hashing.crc32());
    return hc.toString();
  }
  
  
}
