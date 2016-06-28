package org.tiger.ant.test;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.concurrent.CountDownLatch;


import org.junit.Test;
import org.tiger.ant.client.FileConnection;

public class FileConnectionTest {

  @Test
  public void testSendFile() throws UnknownHostException, IOException, InterruptedException {
    FileConnection conn = FileConnection.openConnection("127.0.0.1", 9000);
    sendHeartBeat(conn, 2, 500);

//    sendHeartBeat(conn, 5, 500);
    File dir = new File("/Users/tiger/rshard");
    File files[] = dir.listFiles();
    int i=0;
    for (File f : files) {
      System.out.println("upload file:" + f.getName() + " begin");
      conn.sendFile("nginx","rshard", f,i++%2==0);
      sendHeartBeat(conn, 2, 500);
      System.out.println("upload file:" + f.getName() + " end");
    }
  }

  private void sendHeartBeat(FileConnection conn, int count, int interval) throws IOException,
      InterruptedException {
    for (int i = 0; i < count; i++) {
      conn.sendHeartBeat();
      Thread.currentThread().sleep(interval);
    }
  }

  @Test
  public void testConcurrent() throws InterruptedException {
    File root = new File("/Users/tiger/amazon/nginx");
    File fs[] = root.listFiles();
    CountDownLatch counter = new CountDownLatch(fs.length);
    for (File f : root.listFiles()) {
      Task t = new Task(f.getAbsolutePath(), f.getName(), counter);
      Thread th = new Thread(t);
      th.start();

    }
    counter.await();
  }


  class Task implements Runnable {
    String path;
    String type;
    CountDownLatch counter;

    Task(String path, String type, CountDownLatch counter) {
      this.path = path;
      this.type = type;
      this.counter = counter;
    }

    @Override
    public void run() {
      try {
        FileConnection conn = FileConnection.openConnection("127.0.0.1", 9000);
        File dir = new File(this.path);
        int i=0;
        for (File f : dir.listFiles()) {
          System.out.println("upload file :" + this.type + " " + f.getName() + " begin");
          conn.sendFile(type, "",f,i%2==0);
          sendHeartBeat(conn, 2, 500);
          System.out.println("upload file :" + this.type + " " + f.getName() + " end");
        }
      } catch (Exception e) {
        e.printStackTrace();
      } finally {
        counter.countDown();
      }
    }
  }

 }
