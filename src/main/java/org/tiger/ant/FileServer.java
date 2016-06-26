package org.tiger.ant;

import java.net.InetAddress;

import org.tiger.ant.file.FileCleanManager;
import org.tiger.ant.file.FileManager;
import org.tiger.ant.file.FileManagerImpl;
import org.tiger.ant.file.dao.DAL;
import org.tiger.ant.upstream.FileUpstreamManager;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.ResourceLeakDetector;
import io.netty.util.ResourceLeakDetector.Level;

/**
 * 
 * @time 2016年6月13日 下午4:00:32
 * @author tiger
 * @version $Rev$
 */
public class FileServer {

  public static void main(String[] args) throws Exception {
    ResourceLeakDetector.setLevel(Level.ADVANCED);
    ServerConfig config = ServerConfig.getInstance();
    DAL.initialize();
    int port = config.getPort();
    String ip = config.getIp();
    System.out.println("listen port " + port);
    System.out.println("bind ip " + ip);
    FileServer server = new FileServer();
    server.run();

  }


  private FileUpstreamManager fileUpManager;
  private FileFetchManager fetchManager;
  private FileManager fileManager;
  private FileCleanManager cleanManager;
  private ServerConfig config = ServerConfig.getInstance();

  private int port;
  private String ip;

  public FileServer() {
    fileManager=new FileManagerImpl();
    fileUpManager = new FileUpstreamManager(fileManager);
    this.port = this.config.getPort();
    this.ip = this.config.getIp();
    this.fetchManager = new FileFetchManager(this.fileManager);
    cleanManager=new FileCleanManager(this.fileManager);
    
  }

  public void run() throws Exception {
    fileManager.rebuildDB();
    fileManager.reset();
    FileServerChannelInitializer initializer = new FileServerChannelInitializer(this.fileManager,this.fetchManager);
    EventLoopGroup bossGroup = new NioEventLoopGroup();
    EventLoopGroup workerGroup = new NioEventLoopGroup();
    try {
      ServerBootstrap b = new ServerBootstrap();
      b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
          .childHandler(initializer).option(ChannelOption.SO_BACKLOG, 128).childOption(ChannelOption.SO_KEEPALIVE, true);
      InetAddress _ip = InetAddress.getByName(ip);
      ChannelFuture f = b.bind(_ip, port).sync();
      System.out.println("bind " + ip);
      fileUpManager.start();
      this.cleanManager.start();
      f.channel().closeFuture().sync();

    } finally {
      workerGroup.shutdownGracefully();
      bossGroup.shutdownGracefully();
    }
  }

}
