package org.tiger.ant;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.tiger.ant.file.FileMeta;
import org.tiger.ant.util.JsonUtil;
import org.tiger.ant.util.FileCheckSumUtil;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * 
 * @time 2016年6月13日 下午4:00:23
 * @author tiger
 * @version $Rev$
 */
@Deprecated
public class ServerFileReceiveHandler extends ChannelInboundHandlerAdapter {
  private int mode = 0;// 0 length mode,1 stream mode
  private int meta_length = 0;
  private FileMeta meta;
  private long firedLength = 0;
  private FileOutputStream fos;
  private String workdir;
  private byte byteState = 0;// 00 heartbeat 01 prepare to send file(next is meta info) 02 infile 03
                             // fileend
  private ByteBuf _lenght_buf = ByteBufAllocator.DEFAULT.buffer();
  private ByteBuf _meta_buf = ByteBufAllocator.DEFAULT.buffer();
  private String tmpFilePath;
  private String filePath;
  
  
  private List<FileUpListener> fileListeners=Collections.synchronizedList(new ArrayList<FileUpListener>(2));

  public ServerFileReceiveHandler() {
    
  }



  // [meta_length][namelength][name][length]
  // [meta_legth][meta_json]
  /**
   * { name:"filename", length:length, type:"", client:"" }
   * 
   */
  @Override
  public void handlerAdded(ChannelHandlerContext ctx) {
    this.reset();
    this.workdir = ServerConfig.getInstance().getWorkdir();

  }

  @Override
  public void handlerRemoved(ChannelHandlerContext ctx) {

  }


  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws IOException,
      InterruptedException {
    ByteBuf m = (ByteBuf) msg;
    try {
      while (m.readableBytes() > 0) {
        if (this.byteState == 0) {//心跳
          this._prepare(m);
        }

        if (this.byteState == 1) {//上传文件
          this._prepareRead(m);
        }

        if (this.byteState == 2) {//传文件中
          this._read(m);
        }
        if (this.byteState == 3) {//文件结束
          this._prepareEnd(ctx, m);
        }else if(this.byteState==4){//file fetch
          
        }
      }
      if (m.readableBytes() > 0) {
        throw new RuntimeException("buf is not empty " + m.readableBytes() + " " + this.byteState);
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      m.release();
    }
  }


  private void reset() {
    this.firedLength = 0;
    this.fos = null;
    this.meta_length = 0;
    this.meta = null;
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    cause.printStackTrace();
    ctx.channel().close();
    ctx.close();
    this._meta_buf.clear();
    _meta_buf.release();
    this._lenght_buf.clear();
    _lenght_buf.release();
    this.reset();
  }

  @Override
  public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    ctx.fireChannelInactive();
    ctx.channel().close();
    this._meta_buf.clear();
    if (_meta_buf.refCnt() > 0)
      _meta_buf.release();
    this._lenght_buf.clear();
    if (_lenght_buf.refCnt() > 0)
      _lenght_buf.release();
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
        this._meta_buf.clear();
        _meta_buf.release();
        this._lenght_buf.clear();
        _lenght_buf.release();
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


  /**
   * read until 1
   * 
   * @param m
   * @throws IOException
   */
  private void _prepare(ByteBuf m) throws IOException {
    if (this.byteState != 0) {
      throw new RuntimeException("byteState !=0 " + this.byteState);
      // return;
    }
    while (byteState == 0 && m.readableBytes() > 0) {
      byte b = m.readByte();
      if (b == 1) {
        this.byteState = 1;
        break;
      }else if(b==4){
        this.byteState=4;
        break;
      }
    }

  }

  /**
   * read meta
   * 
   * @param m
   * @throws IOException
   */
  private void _prepareRead(ByteBuf m) throws IOException {
    if (this.byteState != 1) {
      throw new RuntimeException("byteState !=1");
    }
    
    if (this.meta_length == 0) {
      if (m.readableBytes() > 0) {
        int _l = 4 - this._lenght_buf.readableBytes();
        int l = m.readableBytes();
        int c = l >= _l ? _l : l;
        m.readBytes(_lenght_buf, c);
      }
      if (_lenght_buf.readableBytes() == 4) {
        this.meta_length = _lenght_buf.readInt();
      }
    }

    if (m.readableBytes() > 0) {
      int _l = this.meta_length - this._meta_buf.readableBytes();
      int l = m.readableBytes();
      int c = l >= _l ? _l : l;
      m.readBytes(this._meta_buf, c);
      if (_meta_buf.readableBytes() == this.meta_length) {
        this.byteState = 2;
        byte b[] = new byte[this.meta_length];
        this._meta_buf.readBytes(b);
        this.meta = JsonUtil.fromJson(FileMeta.class, new String(b));
        if (meta.getLength() == 0) {
          this.byteState = 3;
        }
        this.checkdir(meta.getFtype());
        this.filePath =
            this.workdir + File.separator + this.meta.getFtype() + File.separator
                + meta.getFileName();
        this.tmpFilePath = filePath + "_" + System.currentTimeMillis() + ".tmp";
        this.fos = new FileOutputStream(this.tmpFilePath);
        this.fileBegin(meta);
        System.out.println("receive file begin " + this.meta.getFtype() + " "
            + this.meta.getFileName() + " " + this.meta.getLength());
      }
    }

  }

  /**
   * read file
   * 
   * @param m
   * @throws IOException
   */
  private void _read(ByteBuf m) throws IOException {
    if (this.byteState != 2) {
      return;

    }
    int l = 0;
    while ((l = m.readableBytes()) > 0 && this.meta.getLength() > this.firedLength) {
      long _l = this.meta.getLength() - this.firedLength;
      int c = l < _l ? l : (int) _l;
      byte b[] = new byte[c];
      m.readBytes(b);
      this.fos.write(b);
      this.fos.flush();
      this.firedLength += c;
      if (this.firedLength == this.meta.getLength()) {
        this.byteState = 3;
        break;
      }
    }

  }


  private void _prepareEnd(ChannelHandlerContext ctx, ByteBuf m) throws IOException {
    if (m.readableBytes() > 0) {
      byte b = m.readByte();
      if (b == 3) {
        System.out.println("receive file end " + this.meta.getFtype() + " "
            + this.meta.getFileName() + " " + this.meta.getLength());
        this.byteState = 0;
        this._meta_buf.clear();
        this._lenght_buf.clear();
        this.meta_length = 0;
        this.firedLength = 0;
        this.fos.close();
        File f = new File(this.tmpFilePath);
        boolean succ = true;
        if (this.meta.isCheckSum()) {
          String _md5 = FileCheckSumUtil.getCrc32CheckSum(f);
          if (!meta.getCrc32CheckSum().equals(_md5)) {
            succ = false;
            f.delete();
          } 
        }
        if (succ) {
          f.renameTo(new File(this.filePath));
          this.fileEnd(meta);
          writeACK(ctx, true);
        } else {
          this.fileFailed(meta);
          writeACK(ctx, false);
        }
        this.meta = null;
      } else {
        writeACK(ctx, false);
        this.fileFailed(meta);
        throw new RuntimeException("end is not 3 " + b);
      }
    }
  }

  private void writeACK(ChannelHandlerContext ctx, boolean succ) {
    ByteBuf buf = ByteBufAllocator.DEFAULT.buffer(1);
    if (succ) {
      buf.writeByte(Constants.RECEIVE_SUCCESS);
    } else {
      buf.writeByte(Constants.RECEIVE_FAILED);
    }
    ctx.writeAndFlush(buf);
    buf.clear();
    if (buf.refCnt() > 0)
      buf.release();
  }
  
  
  public void addListener(FileUpListener listener){
    this.fileListeners.add(listener);
  }
  
  private void fileBegin(FileMeta meta){
    for(FileUpListener l:this.fileListeners){
      l.fileBegin(meta);
    }
  }
  
  private void fileEnd(FileMeta meta){
    for(FileUpListener l:this.fileListeners){
      l.fileEnd(meta);
    }
  }
  
  private void fileFailed(FileMeta meta){
    for(FileUpListener l:this.fileListeners){
      l.fileFailed(meta);
    }
  }
}
