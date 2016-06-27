package org.tiger.ant;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.tiger.ant.file.FileMeta;
import org.tiger.ant.msg.FileTransEnd;
import org.tiger.ant.msg.FileTransStart;
import org.tiger.ant.util.FileStoreUtil;
import org.tiger.ant.util.JsonUtil;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

public class FileReceiveHandler extends ChannelInboundHandlerAdapter {

  private FileMeta meta;
  private long firedLength = 0;
  private FileOutputStream fos;
  private String workdir;
  private String tmpFilePath;
  private String filePath;

  private List<FileUpListener> fileListeners = Collections
      .synchronizedList(new ArrayList<FileUpListener>(2));

  public FileReceiveHandler(String workdir) {
    this.workdir=workdir;
  }

  @Override
  public void handlerAdded(ChannelHandlerContext ctx) {
    this.reset();

  }

  @Override
  public void handlerRemoved(ChannelHandlerContext ctx) {

  }

  
  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws IOException,
      InterruptedException {
    if (this.meta != null) {
      if (this.firedLength < this.meta.getLength()) {
        ByteBuf m = (ByteBuf) msg;
        try{
        int l = m.readableBytes();
        m.readBytes(fos, m.readableBytes());
        this.firedLength += l;
        fos.flush();
        }finally{
          if(m.refCnt()>0)
            m.release();
        }
      } else if (msg instanceof FileTransEnd) {
        boolean succ = true;
        if (this.firedLength == this.meta.getLength())
          this.writeACK(ctx, true);
        else {
          this.writeACK(ctx, false);
          succ = false;
        }
        this.firedLength = 0;
        this.fos.close();
        if (succ) {
          File f = new File(this.tmpFilePath);
          f.renameTo(new File(this.filePath));
          this.fileEnd(meta);
          AntLogger.logger().debug("file receive success["+JsonUtil.toJson(this.meta)+"]");
        } else {
          File f = new File(this.tmpFilePath);
          f.delete();
          this.fileFailed(meta);
          AntLogger.logger().error("file receive failed["+JsonUtil.toJson(this.meta)+"]");
        }
        this.meta = null;
      }
    } else if (msg instanceof FileTransStart) {
      FileTransStart start = (FileTransStart) msg;
      this.meta = start.getFileMeta();
      this.filePath =FileStoreUtil.getStorePath(workdir, meta);
      this.tmpFilePath = FileStoreUtil.getTempFileStorePath(workdir, meta);
      this.fos = new FileOutputStream(this.tmpFilePath);
      this.fileBegin(meta);
      AntLogger.logger().debug("file receive begin ["+JsonUtil.toJson(this.meta)+"]");

    }else{
      ctx.fireChannelRead(msg);
    }

  }

  private void reset() {
    this.firedLength = 0;
    this.fos = null;
    this.meta = null;
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    cause.printStackTrace();
    ctx.channel().close();
    ctx.close();
    this.reset();
  }

  @Override
  public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    ctx.fireChannelInactive();
    ctx.channel().close();
    ctx.close();
    super.channelUnregistered(ctx);
  }

  @Override
  public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
    if (evt instanceof IdleStateEvent) {
      IdleStateEvent e = (IdleStateEvent) evt;
      if (e.state() == IdleState.READER_IDLE) {
        ctx.channel().close();
        ctx.close();
      }
    }
  }

  private void checkdir(String ftype) {
    File f = new File(this.workdir + File.separator + ftype);
    if (!f.exists()) {
      f.mkdirs();
    } else if (f.isFile()) {
      throw new RuntimeException("error, file exists:" + f.getAbsolutePath());
    }
  }

  private void writeACK(ChannelHandlerContext ctx, boolean succ) {
    ByteBuf buf = ByteBufAllocator.DEFAULT.buffer(1);
    if (succ) {
      buf.writeByte(Constants.RECEIVE_SUCCESS);//receive success
    } else {
      buf.writeByte(Constants.RECEIVE_FAILED);//receive failed
    }
    ctx.writeAndFlush(buf);
    buf.clear();
    if (buf.refCnt() > 0)
      buf.release();
  }


  public void addListener(FileUpListener listener) {
    this.fileListeners.add(listener);
  }

  private void fileBegin(FileMeta meta) {
    for (FileUpListener l : this.fileListeners) {
      l.fileBegin(meta);
    }
  }

  private void fileEnd(FileMeta meta) {
    for (FileUpListener l : this.fileListeners) {
      l.fileEnd(meta);
    }
  }

  private void fileFailed(FileMeta meta) {
    for (FileUpListener l : this.fileListeners) {
      l.fileFailed(meta);
    }
  }
}
