package org.tiger.ant.test;

import java.util.List;

import org.junit.Test;
import org.tiger.ant.file.FileConsume;
import org.tiger.ant.file.dao.DAL;

public class DALTest {

  @Test
 public void testCreateTableInfo() throws Exception{
   DAL.initialize();
  
   FileConsume consume =new FileConsume();
   consume.setFileName("test");
   consume.setGroupId("group1");
   consume.setConsumerId("consumer1");
   consume.setPath("/usr/local/ant/data/type1/xx");
   consume.setStatus(2);
   consume.setType("type1");
   DAL.saveOrUpdateFileConsume(consume);
   List<FileConsume> list = DAL.queryFileConsumeByGroupStatus("group1", 2);
   System.out.println(list.size());

//   FileConsume c=list.get(0);
//   c.setStatus(2);
//   c.setGroupId("group2");
//   c.setConsumeTime(new Date());
//   DAL.saveOrUpdateFileConsume(c);
 }
}
