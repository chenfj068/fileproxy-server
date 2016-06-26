package org.tiger.ant.test;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.tiger.ant.file.FileMeta;
import org.tiger.ant.util.JsonUtil;


public class FileClient {

  public static void main(String[] args) throws Exception {

    File file = new File("/Users/tiger/api-console-2.0.5.zip");
    long length = file.length();
    String name = "api-console-2.0.5-7.zip";
    
    Socket socket = new Socket("127.0.0.1", 9000);
    OutputStream oos = socket.getOutputStream();
    DataOutputStream doos = new DataOutputStream(oos);

    for(int i=0;i<10;i++){
      for(int j=0;j<2;j++){
        doos.write((byte)0);//heart beat
        Thread.currentThread().sleep(100);
      }
      name=name+i;
      FileMeta meta = new FileMeta();
      meta.setClient("hello");
      meta.setFileName(name);
      meta.setFtype("xxx");
      meta.setLength(length);
      String json = JsonUtil.toJson(meta);
      int meta_length = json.getBytes().length;
      doos.write((byte)1);//prepare to send file
      doos.writeInt(meta_length);
      doos.write(json.getBytes());
      InputStream in = new FileInputStream(file);
      byte b[] = new byte[1024];
      int count = 0;
      while ((count = in.read(b)) > 0) {
        doos.write(b, 0, count);
      }
      doos.write((byte)3);
      doos.flush();
      in.close();
      for(int j=0;j<5;j++){
        doos.write((byte)0);//heart beat
        Thread.currentThread().sleep(10);
      }
    }
    
    Thread.sleep(500);
    doos.close();
    socket.close();

  }

}
