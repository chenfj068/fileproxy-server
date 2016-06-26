package org.tiger.ant;

import org.tiger.ant.file.FileManager;
import org.tiger.ant.msg.AntMessageDecoder;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

public class FileServerChannelInitializer extends ChannelInitializer<SocketChannel>{
  private FileFetchManager fetchManager;
  private FileManager fileManager;
  public FileServerChannelInitializer(FileManager fileManager,FileFetchManager fetchManager){
    this.fetchManager=fetchManager;
    this.fileManager=fileManager;
  }
  
  public FileServerChannelInitializer(FileManager filemanager){
   this.fileManager=filemanager;
  }
  
  @Override
  protected void initChannel(SocketChannel ch) throws Exception {
    AntMessageDecoder decoder = new AntMessageDecoder();
    ch.pipeline().addLast(new IdleStateHandler(0, 0, 60));
    ch.pipeline().addLast(decoder);
    FileReceiveHandler recvHandler = new FileReceiveHandler(ServerConfig.getInstance().getWorkdir());
    recvHandler.addListener(fileManager);
    ch.pipeline().addLast(recvHandler);
    FileFetchHandler fetchHandler = new FileFetchHandler(this.fetchManager);
    ch.pipeline().addLast(fetchHandler);
  }

}
