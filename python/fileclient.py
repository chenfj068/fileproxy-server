# -*- coding: UTF-8 -*-

import socket
import struct
import os
import json


""""
FileMeta java definition
  private String fileName;
  private String distDir="";
  private long length;
  private String ftype;
  private String client;
  private String crc32CheckSum;
  private boolean checkSum=false;

"""
class FileMeta(object):
    def __init__(self):
        self.fileName=""
        self.distDir=""
        self.length=0
        self.ftype=""
        self.client=""
        self.crc32CheckSum=""
        self.checkSum=False


def buildFileMetaJson(file,type,checksum,distdir):
    # m=FileMeta()
    stat = os.stat(file)
    # m.length=stat.st_size
    # m.fileName=file.split("/")[-1]
    # m.distDir=distdir
    # m.ftype=type
    # m.client=""
    # m.crc32CheckSum=""
    # m.checkSum=False
    d={}
    d["length"]=stat.st_size
    d["fileName"]=file.split("/")[-1]
    d["distDir"]=distdir
    d["ftype"]=type
    d["client"]=""
    d["crc32CheckSum"]=""
    d["checkSum"]=False
    return json.dumps(d)




class FileClient(object):
    def __init__(self,host,port):
        self.host=host
        self.port=port
        conn = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.conn=conn


    def connect(self):
        self.conn.connect((self.host,self.port))

    def sendFile(self,filepath,type,distdir):
        _meta_str=buildFileMetaJson(filepath,type,False,distdir)
        _meta_bytes=bytearray(_meta_str)
        # self.conn.recv_into()
        _start=struct.pack("b",1)
        _len=len(_meta_bytes)
        _met_len=struct.pack(">i",_len)
        print _meta_str,len(_meta_bytes)
        print _met_len
        self.conn.sendall(_start)
        self.conn.sendall(_met_len)
        self.conn.sendall(_meta_bytes)
        f=open(filepath)
        buf_size=1024
        buf=None
        while True:
            buf = f.read(buf_size)
            if len(buf)<=0:
                break
            self.conn.sendall(buf)
        _end=struct.pack("b",3)
        self.conn.sendall(_end)
        b=self.conn.recv(1)

    def close(self):
        self.conn.close()


if __name__=='__main__':
    f="/Users/tiger/amazon/MSP3_PMSC_OCF3H_ME_L88_GLB_201607011200_00000-36000.TXT"
    type="mp3"
    host="127.0.0.1"
    port=9000
    fc=FileClient(host,port)
    fc.connect()
    fc.sendFile(f,type,"a/b/c")
    fc.close()
    print "ok"




