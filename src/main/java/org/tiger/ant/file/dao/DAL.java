package org.tiger.ant.file.dao;


import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.sql2o.Connection;
import org.sql2o.Query;
import org.sql2o.Sql2o;
import org.sqlite.SQLiteDataSource;
import org.sqlite.SQLiteJDBCLoader;
import org.tiger.ant.AntLogger;
import org.tiger.ant.ServerConfig;
import org.tiger.ant.file.Consumer;
import org.tiger.ant.file.FileConsume;
import org.tiger.ant.file.FileInfo;
import org.tiger.ant.file.FileUpstream;

public class DAL {
  private static DataSource dataSource;
  private static Sql2o sql2o;
  private static Map<Class, String> tableMap = new HashMap<Class, String>();
  private static Map<Class, String> tableSqlMap = new HashMap<Class, String>();
  private static Class[] ENTITY_CLASSES = new Class[] {FileInfo.class, Consumer.class,
      FileUpstream.class, FileConsume.class};
  private static String[] ENTITY_TABLES = new String[] {"fileinfo", "consumer", "fileupstream",
      "fileconsume"};
  private static String[] ENTITY_TABLE_SQL = new String[] {SQL.TABLE_CREATE_FILEINFO,
      SQL.TABLE_CREATE_CONSUMER, SQL.TABLE_CREATE_FILEUPSTREAM, SQL.TABLE_CREATE_FILECONSUME};

  public static void initialize() throws Exception {
    boolean initialize = SQLiteJDBCLoader.initialize();
    if (!initialize)
      throw new RuntimeException("sqlite init failed");
    int i = 0;
    for (Class clazz : ENTITY_CLASSES) {
      tableMap.put(clazz, ENTITY_TABLES[i]);
      tableSqlMap.put(clazz, ENTITY_TABLE_SQL[i++]);
    }
    SQLiteDataSource dataSource = new SQLiteDataSource();
    String dbDir = ServerConfig.getInstance().getDbDir();
    String dbPath = dbDir + "/" + "ant.db";
    String jdbcUrl = "jdbc:sqlite:" + dbPath;
    dataSource.setUrl(jdbcUrl);
    DAL.dataSource = dataSource;
    sql2o = new Sql2o(DAL.dataSource);
    checkTable();
    checkIndex();
  }


  private static void checkTable() throws SQLException {
    for (Class clazz : ENTITY_CLASSES) {
      boolean exe = tableExists(clazz);
      if (!exe) {
        try (Connection conn = sql2o.open()) {
          String sql = tableSqlMap.get(clazz);
          conn.createQuery(sql).executeUpdate();
          System.out.println("table created " + tableMap.get(clazz));
        }
      }
    }
  }
  
  private static void checkIndex(){
    AntLogger.logger().info("check fileconsume.fileid index");
    executeUpdate(SQL.INDEX_FILECONSUME_FILE);
    AntLogger.logger().info("check fileinf.uptime index");
    executeUpdate(SQL.INDEX_FILEINFO_UPTIME);
    AntLogger.logger().info("check fileupstream.fileid index");
    executeUpdate(SQL.INDEX_FILEUPSTREAM_FILEID);
  }
  
  private static void executeUpdate(String sql){
    try (Connection conn = sql2o.open()) {
      conn.createQuery(sql).executeUpdate();
    }catch(Exception e){
      throw new RuntimeException(e);
    }
  }

  private static boolean tableExists(Class clazz) throws SQLException {
    try (Connection conn = sql2o.open()) {
      String table = tableMap.get(clazz);
      String sql = "SELECT name FROM sqlite_master WHERE name='" + table + "'";
      try {
        String name = conn.createQuery(sql).executeAndFetchFirst(String.class);
        return name != null;
      } catch (Exception e) {
        throw new SQLException(e);
      }
    }
  }

  public static List<String> queryConsumerGroupsByFtype(String type) {
    try (Connection conn = sql2o.open()) {
      Query query = conn.createQuery(SQL.SQL_SELECT_GROUP_BY_FTYPE);
      query.addParameter("ftype", type);
      return query.executeAndFetch(String.class);
    }catch(Exception e){
      throw new RuntimeException(e);
    }
  }

  public static Consumer queryConsumerByid(String consumerid,String groupid,String type) {
    try (Connection conn = sql2o.open()) {
      Query query = conn.createQuery(SQL.SQL_SELECT_CONSUMER_BY_ID);
      query.addParameter("consumerid", consumerid);
      query.addParameter("groupid", groupid);
      query.addParameter("ftype", type);
      return query.executeAndFetchFirst(Consumer.class);
    }catch(Exception e){
      throw new RuntimeException(e);
    }
  }


  public static void saveOrUpdateConsumer(Consumer consumer) {
    if (queryConsumerByid(consumer.getConsumerId(),consumer.getGroupId(),consumer.getFtype()) != null)
      return;
    try (Connection conn = sql2o.open()) {
      Query query = conn.createQuery(SQL.SQL_SAVE_CONSUMER);
      Map<String, Object> map = entityToPropertiesMap(consumer);
      for (String key : map.keySet()) {
        query.addParameter(key, map.get(key));
      }
      query.executeUpdate();
    }catch(Exception e){
      throw new RuntimeException(e);
    }
  }


  public static void saveOrUpdateFileInfo(FileInfo fileInfo) {
    String sql = SQL.SQL_SAVE_FILEINFO;

    if (fileInfo.getId() != 0) {
      sql = SQL.SQL_UPDATE_FILEINFO;
    }
    try (Connection conn = sql2o.open()) {
      Map<String, Object> map = entityToPropertiesMap(fileInfo);
      if (fileInfo.getId() == 0) {
        map.remove("id");
      }
      Query query = conn.createQuery(sql);
      for (String key : map.keySet()) {
        query.addParameter(key, map.get(key));
      }
      long id = query.executeUpdate().getKey(Long.class);
      fileInfo.setId(id);
    }catch(Exception e){
      throw new RuntimeException(e);
    }

  }
  
  public static FileInfo getFileInfoById(long id){
    String sql="select * from fileinfo where id=:id";
    try (Connection conn = sql2o.open()) {
      Query query = conn.createQuery(sql);
      query.addParameter("id", id);
      return query.executeAndFetchFirst(FileInfo.class);
    }catch(Exception e){
      throw new RuntimeException(e);
    }
  }

  public static void saveOrUpdateFileInfo(FileInfo fileInfo, Connection conn) {
    String sql = SQL.SQL_SAVE_FILEINFO;

    if (fileInfo.getId() != 0) {
      sql = SQL.SQL_UPDATE_FILEINFO;
    }

    Map<String, Object> map = entityToPropertiesMap(fileInfo);
    if (fileInfo.getId() == 0) {
      map.remove("id");
    }
    Query query = conn.createQuery(sql);
    for (String key : map.keySet()) {
      query.addParameter(key, map.get(key));
    }
    long id = query.executeUpdate().getKey(Long.class);
    fileInfo.setId(id);


  }

  public static void deleteFileInfoById(long id) {
    try (Connection conn = sql2o.open()) {

      Query query = conn.createQuery(SQL.SQL_DEL_FILEINFO_BY_ID);
      query.addParameter("id", id);
      query.executeUpdate();
    }catch(Exception e){
      throw new RuntimeException(e);
    }

  }
  
  public static void deleteFileInfoById(long id,Connection conn) {
      Query query = conn.createQuery(SQL.SQL_DEL_FILEINFO_BY_ID);
      query.addParameter("id", id);
      query.executeUpdate();
    

  }

  public static List<FileInfo> selectFileInfoByUptimeLessThan(Date t) {
    try (Connection conn = sql2o.open()) {
      Query query = conn.createQuery(SQL.SQL_SELECT_FILEINFO_BY_UPTIME_LESS_THAN);
      query.addParameter("uptime", t);
      return query.executeAndFetch(FileInfo.class);
    }catch(Exception e){
      throw new RuntimeException(e);
    }
  }

  public static void saveOrUpdateFileUpstream(FileUpstream upstream, Connection conn) {
    String sql = SQL.SQL_SAVE_FILEUPSTREAM;
    if (upstream.getId() != 0) {
      sql = SQL.SQL_UPDATE_FILEUPSTREAM;
    }
    Map<String, Object> map = entityToPropertiesMap(upstream);
    if (upstream.getId() == 0) {
      map.remove("id");
    }
    Query query = conn.createQuery(sql);
    for (String key : map.keySet()) {
      query.addParameter(key, map.get(key));
    }
    query.executeUpdate();


  }

  public static void saveOrUpdateFileUpstream(FileUpstream upstream) {
    String sql = SQL.SQL_SAVE_FILEUPSTREAM;
    if (upstream.getId() != 0) {
      sql = SQL.SQL_UPDATE_FILEUPSTREAM;
    }
    Map<String, Object> map = entityToPropertiesMap(upstream);
    if (upstream.getId() == 0) {
      map.remove("id");
    }
    try (Connection conn = sql2o.beginTransaction(java.sql.Connection.TRANSACTION_SERIALIZABLE)) {
      Query query = conn.createQuery(sql);
      for (String key : map.keySet()) {
        query.addParameter(key, map.get(key));
      }
      query.executeUpdate().commit();
    }catch(Exception e){
      throw new RuntimeException(e);
    }


  }


  public static void deleteFileUpstream(long id) {
    try (Connection conn = sql2o.beginTransaction(java.sql.Connection.TRANSACTION_SERIALIZABLE)) {
      String sql = SQL.SQL_DEL_FILEUPSTREAM_BY_ID;
      Query query = conn.createQuery(sql);
      query.addParameter("id", id);
      query.executeUpdate().commit();
    }catch(Exception e){
      throw new RuntimeException(e);
    }
  }

  public static List<FileUpstream> queryFileUpstreamTimeLessThan(Date time) {
    try (Connection conn = sql2o.open()) {
      String sql = SQL.SQL_SELECT_FILEUPSTREAM_BY_UPTIME_LESS_THAN;
      Query query = conn.createQuery(sql);
      query.addParameter("uptime", time);
      return query.executeAndFetch(FileUpstream.class);
    }catch(Exception e){
      throw new RuntimeException(e);
    }
  }


  public static List<FileUpstream> queryFileUpstreamByStatus(int status) {
    try (Connection conn = sql2o.open()) {
      String sql = SQL.SQL_SELECT_FILEUPSTREAM_BY_STATUS;
      Query query = conn.createQuery(sql);
      query.addParameter("status", status);
      return query.executeAndFetch(FileUpstream.class);
    }catch(Exception e){
      throw new RuntimeException(e);
    }
  }


  public static void deleteFileConsumeByFileId(long fileid,Connection conn){
   Query query= conn.createQuery(SQL.SQL_DEL_FILECONSUME_BY_FILEID);
   query.addParameter("fileid", fileid);
   query.executeUpdate();
  }
  public static void deleteFileUpstreamByFileId(long fileid,Connection conn){
    Query query= conn.createQuery(SQL.SQL_DEL_FILEUPSTREAM_BY_FILEID);
    query.addParameter("fileid", fileid);
    query.executeUpdate();
   }
  public static void saveOrUpdateFileConsume(FileConsume consume) {
    String sql = SQL.SQL_SAVE_FILECONSUME;
    if (consume.getId() > 0) {
      sql = SQL.SQL_UPDATE_FILECONSUME;
    }
    try (Connection conn = sql2o.beginTransaction(java.sql.Connection.TRANSACTION_SERIALIZABLE)) {
      Map<String, Object> map = entityToPropertiesMap(consume);
      if (consume.getId() == 0) {
        map.remove("id");
      }
      Query query = conn.createQuery(sql);
      for (String key : map.keySet()) {
        query.addParameter(key, map.get(key));
      }
      long id = query.executeUpdate().getKey(Long.class);
      conn.commit();
      if (consume.getId() == 0)
        consume.setId(id);
    }
  }

  public static void saveOrUpdateFileConsume(FileConsume consume, Connection conn) {
    String sql = SQL.SQL_SAVE_FILECONSUME;
    if (consume.getId() > 0) {
      sql = SQL.SQL_UPDATE_FILECONSUME;
    }
    Map<String, Object> map = entityToPropertiesMap(consume);
    if (consume.getId() == 0) {
      map.remove("id");
    }
    Query query = conn.createQuery(sql);
    for (String key : map.keySet()) {
      query.addParameter(key, map.get(key));
    }
    long id = query.executeUpdate().getKey(Long.class);
    if (consume.getId() == 0)
      consume.setId(id);

  }

  public static List<FileConsume> queryAllFileConsume() {
    String sql = "select * from fileconsume";
    try (Connection conn = sql2o.open()) {

      Query query = conn.createQuery(sql);

      return query.executeAndFetch(FileConsume.class);
    }catch(Exception e){
      throw new RuntimeException(e);
    }
  }
  
  public static FileConsume queryFileConsumeByGroupTypeStatus(String groupid,String type,int status){
    try (Connection conn = sql2o.open()) {

      Query query = conn.createQuery(SQL.SQL_SELECT_FILECONSUME_BY_GROUP_TYPE_STATUS);
      query.addParameter("groupid", groupid);
      query.addParameter("type", type);
      query.addParameter("status", status);
      return query.executeAndFetchFirst(FileConsume.class);
    }catch(Exception e){
      throw new RuntimeException(e);
    }
  }

  public static List<FileConsume> queryFileConsumeByGroupStatus(String group, int status) {
    String sql = SQL.SQL_SELECT_FILECONSUME_BY_GROUP_STATUS;
    try (Connection conn = sql2o.open()) {
      Query query = conn.createQuery(sql);
      query.addParameter("groupid", group);
      query.addParameter("status", status);
      return query.executeAndFetch(FileConsume.class);
    }catch(Exception e){
      throw new RuntimeException(e);
    }
  }

  public static void deleteFileConsumeByTimeLessThan(Date t) {
    try (Connection conn = sql2o.open()) {
      Query query = conn.createQuery(SQL.SQL_DEL_FILECONSUME_BY_TIME_LESS_THAN);
      query.addParameter("uptime", t);
      query.executeUpdate();
    }catch(Exception e){
      throw new RuntimeException(e);
    }
  }

  public static void deleteFileConsumeById(long id) {
    String sql = SQL.SQL_DEL_FILECONSUME_BY_ID;
    try (Connection conn = sql2o.open()) {
      Query query = conn.createQuery(sql);
      query.addParameter("id", id);
      query.executeUpdate();
    }catch(Exception e){
      throw new RuntimeException(e);
    }
  }

  private static Map<String, Object> entityToPropertiesMap(Object obj) {
    Field fileds[] = obj.getClass().getDeclaredFields();
    Map<String, Object> map = new HashMap<String, Object>();
    try {
      for (Field f : fileds) {
        String name = f.getName();
        String mname = "get" + name.toUpperCase().charAt(0) + name.substring(1);
        Method m =null;
        try{
          m=obj.getClass().getMethod(mname);
        }catch(Exception e){
          continue;
        }
        Object _obj = m.invoke(obj);
        map.put(name.toLowerCase(), _obj);
      }
    } catch (Exception e) {

    }

    return map;
  }

  public static Sql2o getSql2o() {
    return sql2o;
  }

}
