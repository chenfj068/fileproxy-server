package org.tiger.ant;

import org.apache.log4j.Logger;


public class AntLogger {
  private static Logger logger = Logger.getLogger("ant");

  public static Logger logger() {
    return logger;

  }
}
