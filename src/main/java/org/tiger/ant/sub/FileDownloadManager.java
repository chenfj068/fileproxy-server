package org.tiger.ant.sub;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;

import org.tiger.ant.AntLogger;
import org.tiger.ant.Constants;
import org.tiger.ant.file.FileMeta;
import org.tiger.ant.msg.FileFetchRequest;
import org.tiger.ant.util.JsonUtil;

public class FileDownloadManager implements FileFetchListener {

  private Channel channel;
  private ConsumerConfig config;
  private FileDownloadProcessor processor;
  private ScheduledExecutorService sch = Executors.newScheduledThreadPool(2);


  public FileDownloadManager(ConsumerConfig config, FileDownloadProcessor processor) {
    this.config = config;
    this.processor = processor;
  }

  @Override
  public void fileBegin(FileMeta m) {
    // nothing to do
    // System.out.println("file receive begin "+m.getFileName());
  }

  @Override
  public void fileEnd(FileMeta m) {
    String path =
        config.getWorkDir() + File.separator + m.getFtype() + File.separator + m.getFileName();
    boolean succ=false;
   for(int i=0;i<3;i++){
     try{
       processor.onFileReceived(m, new File(path));
       succ=true;
       break;
       }catch(Exception e){
         try {
          Thread.sleep(5000);
        } catch (InterruptedException e1) {
          e1.printStackTrace();
        }
       }
   }
    if(!succ){
      AntLogger.logger().error("file process failed");
    }
    writeFileReq();
  }

  @Override
  public void fileFailed(FileMeta m) {
    // System.out.println("file receive failed "+m.getFileName());
    this.channel.close();
    AntLogger.logger().error("file receive failed " + m.getFileName());


  }

  public void channelOpen(Channel channel) {
    this.channel = channel;
    writeFileReq();
 
  }

  public void channelClose(Channel channel) {
    this.channel = null;
  }

  @Override
  public void onNoMoreFileConsume() {
    sch.schedule(new Runnable() {

      @Override
      public void run() {
        writeFileReq();
      }

    }, 10, TimeUnit.SECONDS);


  }

  private void writeFileReq() {
    FileFetchRequest req = new FileFetchRequest();
    req.setConsumerId(this.config.getConsumerId());
    req.setGroupId(config.getGroupId());
    req.setOffset(0);
    req.setType(config.getTypes()[0]);
    byte b[] = null;
    try {
      b = JsonUtil.toJson(req).getBytes();
    } catch (IOException e) {
      e.printStackTrace();
    }
    ByteBuf buf = ByteBufAllocator.DEFAULT.buffer(1 + 4 + b.length);
    buf.writeByte(Constants.FILE_DOWNLOAD_REQ);
    buf.writeInt(b.length);
    buf.writeBytes(b);
    channel.writeAndFlush(buf);
  }


}
