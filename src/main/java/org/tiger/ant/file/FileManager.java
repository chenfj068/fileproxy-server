package org.tiger.ant.file;

import java.util.Date;
import java.util.List;

import org.tiger.ant.FileUpListener;


/**
 * 
 * @time 2016年6月19日 上午8:25:47
 * @author tiger
 * @version $Rev$
 */
public interface FileManager extends FileUpListener {

  public FileUpstream acquireNextFileUpStream();
 
  public FileConsume acquireNextFileConsume(String groupId, String consumerId,String type);

  public void onFirstTimeSub(Consumer consumer);
  
  public Consumer getConsumerById(String consumerid);
  
  public Consumer getConsumerSubInfo(String groupId,String consumerid,String type);
  
  public void deleteFileConsumeById(long id);
  
  public void deleteFileInfoById(long id);
  
  public FileInfo getFileInfoById(long id);
  
  public void deleteFileUpstreamById(long id);
  
  public void onFileUpstreamSuccess(FileUpstream up);

  public void onFileUpstreamFailed(FileUpstream up);

  public void onFileConsumeSuccess(FileConsume consume);

  public void onFileConsumeFailed(FileConsume consume);
  
  public void reset();

  public List<FileInfo> getFileInfoByUpTimeLessThan(Date t,int maxcount);

  public void rebuildDB();

}
