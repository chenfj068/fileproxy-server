package org.tiger.ant.sub;

import org.tiger.ant.FileUpListener;

public interface FileFetchListener extends FileUpListener{

  public void onNoMoreFileConsume();
}
