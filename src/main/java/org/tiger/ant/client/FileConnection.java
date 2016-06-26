package org.tiger.ant.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.tiger.ant.Constants;
import org.tiger.ant.file.FileMeta;
import org.tiger.ant.util.JsonUtil;
import org.tiger.ant.util.FileCheckSumUtil;

public class FileConnection {

  private Socket socket;
  private DataOutputStream doos;
  private DataInputStream input;
  private boolean busy = false;
  private ReentrantLock lock = new ReentrantLock();

  private FileConnection() {

  }

  public static FileConnection openConnection(String host, int port) throws UnknownHostException,
      IOException {
    FileConnection _conn = new FileConnection();
    _conn.socket = new Socket(host, port);
    OutputStream oos = _conn.socket.getOutputStream();
    DataOutputStream _doos = new DataOutputStream(oos);
    _conn.doos = _doos;
    _conn.input = new DataInputStream(_conn.socket.getInputStream());
    return _conn;
  }

  public void sendFile(String type, File f, boolean withMd5) throws IOException,
      InterruptedException {
    boolean ok = lock.tryLock(5000, TimeUnit.SECONDS);
    if (ok) {
      try {
        busy = true;
        sendFile0(type, f, true);
      } catch (IOException ioe) {
        this.socket.close();
        this.busy = false;
        throw ioe;
      } finally {
        lock.unlock();
        busy = false;
      }
    } else {
      throw new RuntimeException("time out");
    }
  }


  public void sendFile(String type, File f) throws IOException, InterruptedException {
    boolean ok = lock.tryLock(5000, TimeUnit.SECONDS);
    if (ok) {
      try {
        busy = true;
        sendFile0(type, f, false);
      } catch (IOException ioe) {
        this.socket.close();
        this.busy = false;
        throw ioe;
      } finally {
        lock.unlock();
        busy = false;
      }
    } else {
      throw new RuntimeException("time out");
    }
  }

  private void sendFile0(String type, File f, boolean withMd5) throws IOException {
    FileMeta meta = new FileMeta();
    meta.setClient("");
    meta.setFileName(f.getName());
    meta.setFtype(type);
    meta.setLength(f.length());
    if (withMd5) {
      String md5 = FileCheckSumUtil.getCrc32CheckSum(f);
      meta.setCrc32CheckSum(md5);
      meta.setCheckSum(true);
    }

    String json = JsonUtil.toJson(meta);
    int meta_len = json.getBytes().length;
    doos.write((byte) 1);// prepare to send file
    doos.writeInt(meta_len);
    doos.write(json.getBytes());
    doos.flush();
    InputStream in = new FileInputStream(f);
    byte b[] = new byte[1024];
    int count = 0;
    while ((count = in.read(b)) > 0) {
      doos.write(b, 0, count);
      doos.flush();
    }
    doos.write((byte) 3);
    doos.flush();
    if (this.input.readByte() != Constants.RECEIVE_SUCCESS) {
      this.socket.close();
      throw new IOException("file upload failed");
    }
    in.close();
  }

  public void close() throws IOException {
    this.socket.close();
  }

  public void sendHeartBeat() throws IOException, InterruptedException {
    if (busy)
      return;
    boolean ok = lock.tryLock(200, TimeUnit.SECONDS);
    if (ok) {
      try {
        this.doos.writeByte(0);
      } catch(IOException e){
        throw e;
      } finally {
        lock.unlock();
      }
    }
  }
}
