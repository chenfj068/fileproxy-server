package org.tiger.ant.file.dao;

public interface SQL {
  
static final String TABLE_CREATE_FILEINFO="create table fileinfo (id INTEGER PRIMARY KEY AUTOINCREMENT,uptime DATETIME,type varchar(128),name varchar(255),path VARCHAR(255),length BIGINT)";
static final String TABLE_CREATE_CONSUMER="create table consumer (consumerid varchar(64),groupid varchar(64),regtime DATETIME,lastuptime DATETIME,ftype varchar(64) ,PRIMARY KEY(consumerid,groupid,ftype))";
static final String TABLE_CREATE_FILECONSUME="create table fileconsume(id INTEGER PRIMARY KEY AUTOINCREMENT,fileid INTEGER,groupid varchar(64),filename varchar(255),type varchar(64),path varchar(255),status int,consumerid varchar(64),consumetime DATETIME,uptime DATETIME)";
static final String TABLE_CREATE_FILEUPSTREAM="create table fileupstream(id INTEGER PRIMARY KEY AUTOINCREMENT,fileid INTEGER,type varchar(64),filename varchar(255),path varchar(255),upstreamtime DATETIME,status int,uptime DATETIME)";

static final String INDEX_FILEINFO_UPTIME="CREATE INDEX IF NOT EXISTS file_uptime_fileinfo ON fileinfo (uptime)";
static final String INDEX_FILEUPSTREAM_FILEID="CREATE INDEX IF NOT EXISTS file_id_fileupstream ON fileupstream(fileid)";
static final String INDEX_FILECONSUME_FILE="CREATE INDEX IF NOT EXISTS file_id_fileconsume ON fileconsume(fileid)";
static final String SQL_SAVE_FILEINFO="insert into fileinfo (uptime,type,name,path,length)values(:uptime,:type,:name,:path,:length)";
static final String SQL_UPDATE_FILEINFO="update fileinfo set uptime=:uptime,type=:type,name=:name,path=:path,length=:length whre id=:id";
static final String SQL_DEL_FILEINFO_BY_ID="delete from fileinfo where id=:id";
static final String SQL_SELECT_FILEINFO_BY_UPTIME_LESS_THAN="select * from fileinfo where uptime<:uptime limit :maxcount";


static final String SQL_SAVE_CONSUMER="insert into consumer(consumerid,groupid,regtime,lastuptime,ftype) values(:consumerid,:groupid,:regtime,:lastuptime,:ftype)";
static final String SQL_UPDATE_CONSUMER="update consumer set groupid=:groupid,regtime=:regtime,lastuptime=:lastuptime where consumerid=:consumerid and groupid=:groupid and ftype=:ftype";
static final String SQL_SELECT_CONSUMER_BY_ID="select * from consumer where consumerid=:consumerid and groupid=:groupid and ftype=:ftype";
static final String SQL_SELECT_GROUPS="select distinct(groupid) from consumer";
static final String SQL_SELECT_GROUP_BY_FTYPE="select distinct(groupid)  from consumer where ftype=:ftype";

static final String SQL_SAVE_FILEUPSTREAM="insert into fileupstream(fileid,type,filename,path,upstreamtime,status,uptime)values(:fileid,:type,:filename,:path,:upstreamtime,:status,:uptime)";
static final String SQL_UPDATE_FILEUPSTREAM="update fileupstream set fileid=:fileid,type=:type,filename=:filename,path=:path,upstreamtime=:upstreamtime,status=:status,uptime=:uptime where id=:id";
static final String SQL_SELECT_FILEUPSTREAM_BY_STATUS="select * from fileupstream where status=:status";
static final String SQL_SELECT_FILEUPSTREAM_BY_ID="select * from fileupstream where id=:id";
static final String SQL_DEL_FILEUPSTREAM_BY_ID="delete from fileupstream where id=:id";
static final String SQL_SELECT_FILEUPSTREAM_BY_UPTIME_LESS_THAN="select * from fileupstream where uptime<:uptime";
static final String SQL_SELECT_FILEUPSTREAM_BY_FILEID="select * from fileupstream where fileid=:fileid";
static final String SQL_DEL_FILEUPSTREAM_BY_FILEID="delete from fileupstream where fileid=:fileid";

static final String SQL_SAVE_FILECONSUME="insert into fileconsume(fileid,uptime,groupid,filename,type,path,status,consumerid,consumetime)values(:fileid,:uptime,:groupid,:filename,:type,:path,:status,:consumerid,:consumetime)";
static final String SQL_UPDATE_FILECONSUME="update fileconsume set fileid=:fileid,uptime=:uptime,groupid=:groupid,filename=:filename,type=:type,path=:path,status=:status,consumerid=:consumerid,consumetime=:consumetime where id=:id" ;
static final String SQL_SELECT_FILECONSUME_BY_GROUP_STATUS="select * from fileconsume where groupid=:groupid and status=:status";
static final String SQL_DEL_FILECONSUME_BY_ID="delete from fileconsume where id=:id";
static final String SQL_DEL_FILECONSUME_BY_TIME_LESS_THAN="delete from fileconsume where uptime<:uptime";
static final String SQL_SELECT_FILECONSUME_BY_FILEID="select * from fileconsume where fileid=:fileid";
static final String SQL_SELECT_FILECONSUME_BY_GROUP_TYPE_STATUS="select * from fileconsume where groupid=:groupid and type=:type and status=:status limit 1";
static final String SQL_DEL_FILECONSUME_BY_FILEID="delete from fileconsume where fileid=:fileid";
static final String SQL_RESET_FILEUPSTREAM="update fileupstream set status=0 where status=1";
static final String SQL_RESET_FILECONSUME="update fileconsume set status=0 where status=1";



}
