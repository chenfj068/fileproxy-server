package org.tiger.ant.sub;

public interface ClientFileListener {
  
  public void fileBegin();

  public void fileEnd();

  public void fileFailed();
}
