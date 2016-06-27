package org.tiger.ant.client;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;

public class GFSUploadClient {
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
    while (true) {
      File _dir = new File(dir);
      System.out.println("file upload begin");
      for (File f : _dir.listFiles()) {
        if (f.isFile())
          continue;
        for (File _f : f.listFiles()) {
          boolean succ = false;
          while (!succ) {
            try {
              conn.sendFile(type,"", _f);
              succ = true;
            } catch (Exception e) {
              System.out.println("conn error");
              Thread.sleep(5000);
              conn = FileConnection.openConnection(ss[0], Integer.parseInt(ss[1]));
            }
          }
        }

      }
      System.out.println("file upload complete,sleep 1 hour");
      Thread.currentThread().sleep(60 * 1000 * 3600);
    }


  }
}
