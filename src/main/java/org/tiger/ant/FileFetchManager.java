package org.tiger.ant;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;

import org.tiger.ant.file.Consumer;
import org.tiger.ant.file.FileConsume;
import org.tiger.ant.file.FileManager;
import org.tiger.ant.file.FileMeta;
import org.tiger.ant.msg.FileFetchRequest;
import org.tiger.ant.util.FileCheckSumUtil;
import org.tiger.ant.util.FileStoreUtil;
import org.tiger.ant.util.JsonUtil;

public class FileFetchManager {


  private FileManager fileManager;
  public FileFetchManager(FileManager fileManager) {
    this.fileManager = fileManager;
  }

  public FileConsume acquireFileConsume(FileFetchRequest fetchReq) {
    String type = fetchReq.getType();
    Consumer consumer =
        fileManager.getConsumerSubInfo(fetchReq.getGroupId(), fetchReq.getConsumerId(), type);
    if (consumer == null) {
      consumer = new Consumer();
      consumer.setConsumerId(fetchReq.getConsumerId());
      consumer.setFtype(fetchReq.getType());
      consumer.setGroupId(fetchReq.getGroupId());
      consumer.setLastUpTime(new Date());
      consumer.setRegTime(new Date());
      fileManager.onFirstTimeSub(consumer);
      AntLogger.logger().debug(
          "new subscriber [" + consumer.getGroupId() + "," + consumer.getConsumerId() + ","
              + consumer.getFtype() + "]");
    }
    FileConsume consume =
        fileManager.acquireNextFileConsume(fetchReq.getGroupId(), fetchReq.getConsumerId(),
            fetchReq.getType());
    return consume;
  }

  public void sendNoMoreConsume(ChannelHandlerContext ctx, FileFetchRequest req) {
    AntLogger.logger()
        .debug("no more fileconsume [" + req.getGroupId() + "," + req.getType() + "]");
    ByteBuf buf = ByteBufAllocator.DEFAULT.buffer(1);
    buf.writeByte(Constants.FILE_FETCH_NO_MORE);
    ctx.writeAndFlush(buf);
  }

  public void sendConsume(ChannelHandlerContext ctx, FileConsume consume) throws IOException {
    FileInputStream in = null;
    try {
      FileMeta meta = new FileMeta();
      File file = new File(consume.getPath());
      meta.setCheckSum(true);
      meta.setCrc32CheckSum(FileCheckSumUtil.getCrc32CheckSum(file));
      meta.setFtype(consume.getType());
      meta.setLength(file.length());
      meta.setFileName(file.getName());
      meta.setDistDir(FileStoreUtil.getDistDir(consume.getPath(), consume.getType()));
      String json = JsonUtil.toJson(meta);
      byte meta_bytes[] = json.getBytes();
      in = new FileInputStream(file);
      byte b[] = new byte[4 * 1024];
      int c = 0;
      ByteBuf buf = ByteBufAllocator.DEFAULT.buffer(1024 * 4);
      buf.writeByte(Constants.FILE_UPLOAD_BEGIN);
      buf.writeInt(meta_bytes.length);
      buf.writeBytes(meta_bytes);
      ctx.writeAndFlush(buf);
      while ((c = in.read(b)) > 0) {
        buf = ByteBufAllocator.DEFAULT.buffer(1024 * 4);
        buf.writeBytes(b, 0, c);
        ctx.writeAndFlush(buf);
      }
      ByteBuf _buf = ByteBufAllocator.DEFAULT.buffer(1);
      _buf.writeByte(Constants.FILE_UPLOAD_END);
      ctx.writeAndFlush(_buf);

    } catch (IOException e) {
      fileReceiveFailed(consume);
      ctx.channel().close();
      e.printStackTrace();
      throw e;
    } finally {
      try {
        in.close();
      } catch (IOException e) {
        // e.printStackTrace();
      }
    }
  }


  public void fileReceiveSuccess(FileConsume consume) {
    this.fileManager.deleteFileConsumeById(consume.getId());
    AntLogger.logger().debug(
        "delete consumed file:[" + consume.getGroupId() + "," + consume.getConsumerId() + ","
            + consume.getPath() + "]");
  }


  public void fileReceiveFailed(FileConsume consume) {
    consume.setConsumeTime(null);
    consume.setStatus(FileConsume.NOT_CONSUMED);
    this.fileManager.onFileConsumeFailed(consume);
    AntLogger.logger().error(
        "consume file failed:[" + consume.getGroupId() + "," + consume.getConsumerId() + ","
            + consume.getPath() + "]");

  }



}
