package org.tiger.ant.sub;

import java.io.IOException;

import org.tiger.ant.Constants;
import org.tiger.ant.msg.NoMoreFileConsume;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class NoMoreFileConsumeHandler extends ChannelInboundHandlerAdapter {
  FileFetchListener listener;

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws IOException,
      InterruptedException {
    if (msg instanceof NoMoreFileConsume) {
      ByteBuf buf = ByteBufAllocator.DEFAULT.buffer(1);
      buf.writeByte(Constants.FILE_FETCH_NO_MORE_ACK);
      ctx.writeAndFlush(buf);
      if (buf.refCnt() > 0)
        buf.release();
      listener.onNoMoreFileConsume();
    } else {
      ctx.fireChannelRead(msg);
    }


  }

  public void setFetchListener(FileFetchListener listener) {
    this.listener = listener;
  }
}
