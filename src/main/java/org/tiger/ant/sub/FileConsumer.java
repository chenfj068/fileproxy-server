package org.tiger.ant.sub;



import org.tiger.ant.AntLogger;
import org.tiger.ant.FileReceiveHandler;
import org.tiger.ant.msg.AntMessageDecoder;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.ResourceLeakDetector;
import io.netty.util.ResourceLeakDetector.Level;

/**
 * 
 * @time 2016年6月16日 下午4:19:38
 * @author tiger
 * @version $Rev$
 */
public class FileConsumer {

  EventLoopGroup workerGroup = new NioEventLoopGroup();

  private ConsumerConfig config;

  private FileDownloadManager downloadManager;

  public FileConsumer(ConsumerConfig config,FileDownloadProcessor processor) {
    this.config = config;
    downloadManager = new FileDownloadManager(config,processor);
  }

  public ConsumerConfig getConfig() {
    return this.config;
  }


  public void run() throws InterruptedException {
    ResourceLeakDetector.setLevel(Level.ADVANCED);
    try {
      Bootstrap b = new Bootstrap();
      b.group(workerGroup);
      b.channel(NioSocketChannel.class);
      b.option(ChannelOption.SO_KEEPALIVE, true);
      b.handler(new ChannelInitializer<SocketChannel>() {
        @Override
        public void initChannel(SocketChannel ch) throws Exception {
          ch.pipeline().addLast(new IdleStateHandler(0, 0, 20));
          ch.pipeline().addLast(new AntMessageDecoder());
          FileReceiveHandler rechandler = new FileReceiveHandler(config.getWorkDir());
          downloadManager.channelOpen(ch);
          rechandler.addListener(downloadManager);
          ch.pipeline().addLast(rechandler);
          NoMoreFileConsumeHandler nomoreHandler = new NoMoreFileConsumeHandler();
          nomoreHandler.setFetchListener(downloadManager);
          ch.pipeline().addLast(nomoreHandler);
          
        }
      });
      // Start the client.
      for (;;) {
        try{
          ChannelFuture f = b.connect(this.config.getBrokerIp(), this.config.getBrokerPort()).sync(); 
          downloadManager.channelOpen(f.channel());
          f.channel().closeFuture().sync();
          this.downloadManager.channelClose(f.channel());
          AntLogger.logger().error("connection broken");
          Thread.sleep(1000);
        }catch(Exception e){
          AntLogger.logger().error(e);
          Thread.sleep(5000);
        }
        
      }
      // write consumer info
      // Wait until the connection is closed.
    } finally {
      workerGroup.shutdownGracefully();
    }
  }

  

}
