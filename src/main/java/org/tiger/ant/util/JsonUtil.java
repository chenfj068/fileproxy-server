package org.tiger.ant.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonUtil {
  private static ObjectMapper objMapper = new ObjectMapper();
  static{
      objMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
      objMapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
  }
  
  public static  String toJson(Object obj) throws IOException{
      return objMapper.writeValueAsString(obj);
  }
  
  public static <T> T fromJson(Class<T> clazz,String json) throws IOException{
      return objMapper.readValue(json, clazz);        
  }
  
  public static<T> T fromJsonFile(Class<T> clazz,File f)throws IOException{
    return objMapper.readValue(f, clazz);
  }
  
  public static<T> T fromJsonInputStream(Class<T> clazz,InputStream f)throws IOException{
    return objMapper.readValue(f, clazz);
  }
  
  
  public static <T> List<T> fromJsonList(Class<T> clazz,String json) throws IOException{
      return objMapper.readValue(json, objMapper.getTypeFactory().constructCollectionType(List.class, clazz));        
  }
}
