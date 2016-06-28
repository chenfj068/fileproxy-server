package org.tiger.ant;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.tiger.ant.util.JsonUtil;

public class ProxyConfig {

  ProxyConfig() {

  }

  private Map<String, String[]> upstream_servers;
  private Map<String, String[]> proxy_map;
  private int up_stream_thread_num = 10;

  private static ProxyConfig instance;

  public synchronized static ProxyConfig getInstance() {
    if (instance == null) {
      try {
        instance = loadProxyConfig();
      } catch (Exception e) {
        throw new RuntimeException("load proxy config failed", e);
      }
    }
    return instance;
  }



  private static ProxyConfig loadProxyConfig() throws IOException {
    InputStream input = ProxyConfig.class.getResourceAsStream("/proxy.json");
    Map<String, Object> map = JsonUtil.fromJsonInputStream(Map.class, input);
    ProxyConfig config = new ProxyConfig();
    Map<String, String[]> _up_servers = new HashMap<String, String[]>();
    List<Map<String, String>> servers = (List<Map<String, String>>) map.get("upstream_servers");
    for (Map<String, String> p : servers) {
      String group_name = p.get("group_name");
      String ss[] = p.get("servers").split(";");
      _up_servers.put(group_name, ss);
    }
    config.upstream_servers = _up_servers;

    List<Map<String, String>> proxy_list_map = (List<Map<String, String>>) map.get("proxy_list");
    Map<String, String[]> typeMap = new HashMap<String, String[]>();
    for (Map<String, String> m : proxy_list_map) {
      String types[] = m.get("type").split(";");
      Set<String> serverSet = new HashSet<String>();
      String servers_array[] = null;
      String _servers = m.get("servers");
      if (_servers != null && _servers.length() > 0) {
        for (String s : _servers.split(";")) {
          serverSet.add(s);
        }
      }

      String group = m.get("server_group");
      if (group != null) {
        if (!_up_servers.containsKey(group))
          throw new RuntimeException("no server group defined " + group);
        String s[] = _up_servers.get(group);
        for (String _s : s) {
          serverSet.add(_s);
        }
      }
      servers_array = new String[serverSet.size()];
      int i = 0;
      for (String s : serverSet) {
        servers_array[i++] = s;
      }
      for (String t : types) {
        typeMap.put(t, servers_array);
      }
      config.proxy_map = typeMap;
    }
    if (map.containsKey("upstream_thread_number")) {
      config.up_stream_thread_num = (int) map.get("upstream_thread_number");
    }
    return config;
  }

  public String[] getUpstreamServers(String type) {
    for (String key : proxy_map.keySet()) {
      if (type.matches(key))
        return proxy_map.get(key);
    }
    return null;

  }

  public String[] getGroupServers(String group) {
    return this.upstream_servers.get(group);
  }

  public int getUpthreadNumber() {
    return this.up_stream_thread_num;
  }

  public boolean matchProxy(String type) {
    return toBeUpstream(type);
  }

  public Set<String> getUpstreamTypes() {
    return proxy_map.keySet();
  }

  public boolean toBeUpstream(String type) {
    for (String key : proxy_map.keySet()) {
      if (type.matches(key))
        return true;
    }
    return false;
  }

}
