package org.tiger.ant.util;

import java.io.File;
import java.io.IOException;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;

public class FileCheckSumUtil {
  
  public static String getCrc32CheckSum(File file) throws IOException{
    HashCode hc = Files.hash(file, Hashing.crc32());
    return hc.toString();
  }
  
  
}
