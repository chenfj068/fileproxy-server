package org.tiger.ant.msg;

import java.io.IOException;
import java.util.List;

import org.tiger.ant.Constants;
import org.tiger.ant.FileHandlerMode;
import org.tiger.ant.file.FileMeta;
import org.tiger.ant.util.JsonUtil;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

/**
 * 
 * @time 2016年6月17日 上午10:42:43
 * @author tiger
 * @version $Rev$
 */
public class AntMessageDecoder extends ByteToMessageDecoder {
  private byte byteState = 0;
  private FileMeta currentFileMeta;
  private long firedLength = 0;
  private FileHandlerMode mode = FileHandlerMode.FileReceive;

  @Override
  protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
    Object msg = null;
    while ((msg = this.decode(in)) != null) {
      out.add(msg);
    }
  }

  private Object decode(ByteBuf in) throws IOException {
    if(in.readableBytes()==0)
      return null;
    switch (this.byteState) {
      case 0:
        byte b = in.getByte(in.readerIndex());
        if (b == 0) {
          in.readSlice(1);
          return null;
        }
        if (b == 1) {// prepare to send file
          if (in.readableBytes() > 5) {
            ByteBuf len_buf = ByteBufAllocator.DEFAULT.buffer(4);
            in.getBytes(in.readerIndex()+1, len_buf);
            int len = len_buf.readInt();
            len_buf.release();
            if (in.readableBytes() >= 5 + len) {
              in.readSlice(5);//ignore start-flag and length int
              byte meta_bytes[] = new byte[len];
              in.readSlice(len).readBytes(meta_bytes);
              String json = new String(meta_bytes);
              FileMeta meta = JsonUtil.fromJson(FileMeta.class, json);
              this.currentFileMeta = meta;
              FileTransStart start = new FileTransStart();
              start.setFileMeta(meta);
              this.byteState = 1;
              return start;
            } else {
              return null;
            }
          } else {
            return null;
          }
        } else if (b == 4) {// file fetch
          if (in.readableBytes() > 5) {
            ByteBuf len_buf = ByteBufAllocator.DEFAULT.buffer(4);
            in.getBytes(in.readerIndex()+1, len_buf);
            int len = len_buf.readInt();
            len_buf.release();
            if (in.readableBytes() >= 5 + len) {
              in.readSlice(5);// ignore start-flag and len int
              byte meta_bytes[] = new byte[len];
              in.readSlice(len).readBytes(meta_bytes);
              String json = new String(meta_bytes);
              FileFetchRequest fileFetch = JsonUtil.fromJson(FileFetchRequest.class, json);
              this.byteState = Constants.FILE_DOWNLOAD_REQ;
              this.mode = FileHandlerMode.FileDownLoad;
              return fileFetch;
            } else {
              return null;
            }
          } else {
            return null;
          }
        }else if(b==Constants.FILE_FETCH_NO_MORE){
          in.readSlice(1);
          this.byteState=0;
          return new NoMoreFileConsume();
        }
        break;
      case 1:
        int l = in.readableBytes();
        long _l = this.currentFileMeta.getLength() - this.firedLength;
        long c = l > _l ? _l : l;
        this.firedLength += c;
        if (this.firedLength == this.currentFileMeta.getLength())
          this.byteState = 3;
       return in.readBytes((int)c);
      case 3:
        byte b1 = in.readSlice(1).readByte();
        if (b1 != Constants.FILE_UPLOAD_END) {
          System.out.println("error byte "+b1);
          throw new IOException("file end flag error");
        }
        FileTransEnd end = new FileTransEnd();
        end.setFileMeta(this.currentFileMeta);
        this.byteState = 0;
        this.currentFileMeta = null;
        this.firedLength = 0;
        return end;
      case 4:// in file output(download)
        byte b2 = 0;
        while ((b2 = in.readSlice(1).readByte()) == 0) {}//ignore all heart beat
        if (b2 == Constants.RECEIVE_FAILED) {
          FileReceiveStatus status=new FileReceiveStatus();
          status.setFileMeta(this.currentFileMeta);
          status.setSuccess(false);
          this.byteState=Constants.HEART_BEAT;
          return status;
        } else if(b2==Constants.RECEIVE_SUCCESS){
          FileReceiveStatus  _status=new FileReceiveStatus();
          _status.setFileMeta(this.currentFileMeta);
          _status.setSuccess(true);
          this.byteState=Constants.HEART_BEAT;
          return _status;
        }else if(b2==Constants.FILE_FETCH_NO_MORE_ACK){
          this.byteState=0;
          in.readSlice(1);
          return null;
          //nothing to do
        }else {
          in.readSlice(1);
          this.byteState=Constants.HEART_BEAT;
          return null;
        }
      default:
        in.readSlice(in.readableBytes());
    }
    return null;
  }

}
