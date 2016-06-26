package org.tiger.ant;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.tiger.ant.file.FileConsume;
import org.tiger.ant.file.FileManager;
import org.tiger.ant.msg.FileFetchRequest;
import org.tiger.ant.msg.FileReceiveStatus;
import org.tiger.ant.util.JsonUtil;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * 
 * @time 2016年6月17日 上午10:39:21
 * @author tiger
 * @version $Rev$
 */
public class FileFetchHandler extends ChannelInboundHandlerAdapter {

  private FileFetchRequest fetchRequest;
  private FileConsume consume;
  private FileFetchManager fetchManager;
  private List<FileUpListener> fileListeners = Collections
      .synchronizedList(new ArrayList<FileUpListener>(2));

  public FileFetchHandler(FileFetchManager fetchManager) {
    this.fetchManager = fetchManager;
  }

  @Override
  public void handlerAdded(ChannelHandlerContext ctx) {

  }

  @Override
  public void handlerRemoved(ChannelHandlerContext ctx) {

  }


  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    if (msg instanceof FileFetchRequest) {
      FileFetchRequest req = (FileFetchRequest) msg;
      consume = this.fetchManager.acquireFileConsume(req);
      if (consume == null) {
        fetchManager.sendNoMoreConsume(ctx, req);
      } else {
        File f = new File(consume.getPath());
        if (!f.exists()) {
          AntLogger.logger().error("consume file not exists["+JsonUtil.toJson(consume)+"]");
          fetchManager.fileReceiveSuccess(consume);
          fetchManager.sendNoMoreConsume(ctx, req);
        } else {
          fetchManager.sendConsume(ctx, consume);
        }
      }
    } else if (msg instanceof FileReceiveStatus) {
      FileReceiveStatus status = (FileReceiveStatus) msg;
      if (status.isSuccess())
        this.fetchManager.fileReceiveSuccess(consume);
      else
        this.fetchManager.fileReceiveFailed(consume);
      consume = null;
    } else {
      throw new RuntimeException("can not handle it");
    }
  }


  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws IOException {
    if (consume != null) {
      fetchManager.fileReceiveFailed(consume);
      AntLogger.logger().error("file consume failed[" + JsonUtil.toJson(this.consume) + "]");
    }
    // cause.printStackTrace();
    ctx.channel().close();
    ctx.close();

  }

  @Override
  public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    ctx.fireChannelInactive();
    ctx.channel().close();
    ctx.close();
    if (consume != null) {
      fetchManager.fileReceiveFailed(consume);
      AntLogger.logger().error("file consume failed[" + JsonUtil.toJson(this.consume) + "]");
    }
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
    } else {
      System.out.println(evt.getClass().getCanonicalName());
    }
  }


}
