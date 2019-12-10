/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/protocols/streams/CountingInputStream.java $
 * Version:     
 * $Id: CountingInputStream.java 2430 2010-12-06 13:56:06Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  30.04.2010 11:26:28
 */
package com.elster.protocols.streams;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * This class is a simple filter input stream, which counts the number of bytes
 * which passes the read methods and the skip method.
 *
 * @author osse
 */
public class CountingInputStream extends FilterInputStream
{
  int count = 0;

  public CountingInputStream(InputStream in)
  {
    super(in);
  }

  /**
   * Returns the number of bytes which passed the read methods.
   *
   * @return the counted number of bytes
   */
  public int getCount()
  {
    return count;
  }

  /**
   * Sets the count to specified value.
   *
   * @param count the new count.
   */
  public void setCount(int count)
  {
    this.count = count;
  }

  /**
   * Sets the count to 0.
   *
   */
  public void resetCount()
  {
    count = 0;
  }

  @Override
  public int read() throws IOException
  {
    int result = in.read();
    if (result >= 0)
    {
      count++;
    }
    return result;
  }

  @Override
  public int read(byte[] b, int off, int len) throws IOException
  {
    int bytesRead = in.read(b, off, len);
    count += bytesRead;
    return bytesRead;
  }

  @Override
  public long skip(long n) throws IOException
  {
    long bytesSkiped= in.skip(n);
    count += bytesSkiped;
    return bytesSkiped;
  }



}

