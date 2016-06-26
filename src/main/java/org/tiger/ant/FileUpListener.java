package org.tiger.ant;

import org.tiger.ant.file.FileMeta;

public interface FileUpListener {

  public void fileBegin(FileMeta m);
  
  public void fileEnd(FileMeta m);
  
  public void fileFailed(FileMeta m);
  
}
