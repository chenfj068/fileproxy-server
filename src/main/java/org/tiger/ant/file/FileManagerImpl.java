package org.tiger.ant.file;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.sql2o.Connection;
import org.sql2o.Query;
import org.sql2o.Sql2o;
import org.tiger.ant.ServerConfig;
import org.tiger.ant.file.dao.DAL;
import org.tiger.ant.file.dao.SQL;
import org.tiger.ant.util.FileStoreUtil;

public class FileManagerImpl implements FileManager {

  private ServerConfig config = ServerConfig.getInstance();
  private List<FileUpstream> upstreamList = new LinkedList<FileUpstream>();

  private Lock lock = new ReentrantLock();

  @Override
  public void fileBegin(FileMeta m) {
    // nothing to do

  }

  @Override
  public void fileEnd(FileMeta meta) {
    try {
      lock.lock();
      Sql2o sql2o = DAL.getSql2o();
      try (Connection conn = sql2o.beginTransaction(java.sql.Connection.TRANSACTION_SERIALIZABLE)) {
        FileInfo info = new FileInfo();
        info.setLength(meta.getLength());
        info.setName(meta.getFileName());
        info.setType(meta.getFtype());
        info.setUptime(new Date());
        String path = FileStoreUtil.getStorePath(config.getWorkdir(), meta);
        info.setPath(path);
        DAL.saveOrUpdateFileInfo(info, conn);
        boolean up = ServerConfig.getInstance().getProxyConfig().toBeUpstream(meta.getFtype());
        if (up) {
          FileUpstream upstream = new FileUpstream();
          upstream.setFileid(info.getId());
          upstream.setPath(path);
          upstream.setFileName(info.getName());
          upstream.setStatus(FileUpstream.NOT_UPSTREAM);
          upstream.setType(meta.getFtype());
          upstream.setUpstreamTime(null);
          upstream.setUptime(info.getUptime());
          DAL.saveOrUpdateFileUpstream(upstream, conn);
        }

        List<String> groups = DAL.queryConsumerGroupsByFtype(meta.getFtype());
        for (String group : groups) {
          FileConsume consume = new FileConsume();
          consume.setFileid(info.getId());
          consume.setFileName(meta.getFileName());
          consume.setType(meta.getFtype());
          consume.setGroupId(group);
          consume.setPath(path);
          consume.setStatus(FileConsume.NOT_CONSUMED);
          consume.setUptime(info.getUptime());
          consume.setConsumeTime(null);
          DAL.saveOrUpdateFileConsume(consume, conn);
        }
        conn.commit();


      }


    } finally {
      lock.unlock();
    }



  }

  @Override
  public void fileFailed(FileMeta meta) {

    // nothing to do

  }



  @Override
  public FileUpstream acquireNextFileUpStream() {
    try {
      lock.lock();
      if (this.upstreamList.size() == 0) {
        List<FileUpstream> list = DAL.queryFileUpstreamByStatus(FileUpstream.NOT_UPSTREAM);
        if (list.size() > 0)
          upstreamList.addAll(list);
      }
      if (upstreamList.size() == 0)
        return null;
      FileUpstream up = upstreamList.remove(0);
      up.setStatus(FileUpstream.IS_UPSTREAMING);
      up.setUptime(new Date());
      DAL.saveOrUpdateFileUpstream(up);
      return up;
    } finally {
      lock.unlock();
    }

  }



  @Override
  public void onFirstTimeSub(Consumer consumer) {
    try {
      lock.lock();
      DAL.saveOrUpdateConsumer(consumer);

    } finally {
      lock.unlock();
    }
  }


  @Override
  public FileConsume acquireNextFileConsume(String groupId, String consumerId, String type) {
    try {
      lock.lock();
      FileConsume consume =
          DAL.queryFileConsumeByGroupTypeStatus(groupId, type, FileConsume.NOT_CONSUMED);
      if (consume != null) {
        consume.setConsumerId(consumerId);
        consume.setConsumeTime(new Date());
        consume.setStatus(FileConsume.IN_CONSUME);
        DAL.saveOrUpdateFileConsume(consume);
        return consume;
      }
      return null;
    } finally {
      lock.unlock();
    }

  }


  @Override
  public void onFileUpstreamSuccess(FileUpstream up) {
    try {
      lock.lock();
      DAL.deleteFileUpstream(up.getId());

    } finally {
      lock.unlock();
    }
  }

  @Override
  public void onFileUpstreamFailed(FileUpstream up) {
    try {
      lock.lock();

      up.setStatus(FileUpstream.NOT_UPSTREAM);
      up.setUpstreamTime(null);
      DAL.saveOrUpdateFileUpstream(up);
    } finally {
      lock.unlock();
    }

  }

  @Override
  public void onFileConsumeSuccess(FileConsume consume) {
    try {
      lock.lock();
      DAL.deleteFileConsumeById(consume.getId());

    } finally {
      lock.unlock();
    }


  }

  @Override
  public void onFileConsumeFailed(FileConsume consume) {
    consume.setStatus(FileConsume.NOT_CONSUMED);
    consume.setConsumerId(null);
    consume.setConsumeTime(null);
    try {
      lock.lock();
      DAL.saveOrUpdateFileConsume(consume);
    } finally {
      lock.unlock();
    }


  }

  @Override
  public Consumer getConsumerById(String consumerid) {
    try {
      lock.lock();
      return DAL.queryConsumerByid(consumerid);
    } finally {
      lock.unlock();
    }
  }

  @Override
  public Consumer getConsumerSubInfo(String groupId, String consumerid, String ftype) {
    try {
      lock.lock();
      String sql =
          "select * from consumer where consumerid=:consumerid and groupid=:groupid and ftype=:ftype";
      Sql2o sql2o = DAL.getSql2o();
      try (Connection conn = sql2o.open()) {
        Query query = conn.createQuery(sql);
        query.addParameter("consumerid", consumerid);
        query.addParameter("groupid", groupId);
        query.addParameter("ftype", ftype);
        return query.executeAndFetchFirst(Consumer.class);
      }
    } finally {
      lock.unlock();
    }


  }

  @Override
  public void reset() {
    Sql2o sql2o = DAL.getSql2o();

    try (Connection conn = sql2o.beginTransaction(java.sql.Connection.TRANSACTION_SERIALIZABLE)) {
      Query query = conn.createQuery(SQL.SQL_RESET_FILECONSUME);
      query.executeUpdate();
      query = conn.createQuery(SQL.SQL_RESET_FILEUPSTREAM);
      conn.commit();
    }

  }

  @Override
  public List<FileInfo> getFileInfoByUpTimeLessThan(Date t, int maxcount) {
    Sql2o sql2o = DAL.getSql2o();
    try {
      lock.lock();
      try (Connection conn = sql2o.open()) {
        Query query = conn.createQuery(SQL.SQL_SELECT_FILEINFO_BY_UPTIME_LESS_THAN);
        query.addParameter("uptime", t);
        query.addParameter("maxcount", maxcount);
        return query.executeAndFetch(FileInfo.class);
      }
    } finally {
      lock.unlock();
    }
  }

  @Override
  public void deleteFileConsumeById(long id) {
    try {
      lock.lock();
      DAL.deleteFileConsumeById(id);
    } finally {
      lock.unlock();
    }

  }

  @Override
  public void deleteFileInfoById(long id) {
    try {
      lock.lock();
      try (Connection conn =
          DAL.getSql2o().beginTransaction(java.sql.Connection.TRANSACTION_SERIALIZABLE)) {
        DAL.deleteFileInfoById(id, conn);
        DAL.deleteFileConsumeByFileId(id, conn);
        DAL.deleteFileUpstreamByFileId(id, conn);
        conn.commit();
      }
    } finally {
      lock.unlock();
    }

  }

  @Override
  public void deleteFileUpstreamById(long id) {
    try {
      lock.lock();
      DAL.deleteFileUpstream(id);
    } finally {
      lock.unlock();
    }
  }

  @Override
  public void rebuildDB() {
    try {
      lock.lock();
      try (Connection conn = DAL.getSql2o().open()) {
        conn.createQuery("VACUUM").executeUpdate();
      }
    } finally {
      lock.unlock();
    }
  }

  @Override
  public FileInfo getFileInfoById(long id) {

    try {
      lock.lock();
      return DAL.getFileInfoById(id);
    } finally {
      lock.unlock();
    }
  }


}
