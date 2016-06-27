package org.tiger.ant.client;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;

public class DirectoryUploadClient {
  public static void main(String args[]) throws NumberFormatException, UnknownHostException,
      IOException, InterruptedException {
    if (args.length < 3) {
      System.out.println("java -cp xx ip type dir");
      System.exit(0);
    }
    String host = args[0];
    String type = args[1];
    String dir = args[2];
    String ss[] = host.split(":");
    FileConnection conn = FileConnection.openConnection(ss[0], Integer.parseInt(ss[1]));
    File _dir = new File(dir);
    for (File f : _dir.listFiles()) {
      boolean succ = false;
      while (!succ) {
        try {
          conn.sendFile(type, "",f);
        } catch (Exception e) {
          System.out.println("conn error");
          Thread.sleep(5000);
          conn = FileConnection.openConnection(ss[0], Integer.parseInt(ss[1]));
        }
      }
    }
  }
}
