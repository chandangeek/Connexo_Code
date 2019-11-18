/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/protocols/streams/AnalysingInputStream.java $
 * Version:     
 * $Id: AnalysingInputStream.java 2583 2011-01-26 16:16:21Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  30.08.2010 15:07:51
 */
package com.elster.protocols.streams;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Helper to analyze processed (read) data.<P>
 * See {@link #getBytes()}
 *
 * @author osse
 */
public class AnalysingInputStream extends FilterInputStream
{
  public AnalysingInputStream(InputStream in)
  {
    super(in);
  }

  @Override
  public int read() throws IOException
  {
    int readByte = in.read();

    if (readByte >= 0)
    {
      synchronized (readData)
      {
        readData.write(readByte);
      }
    }
    return readByte;
  }

  @Override
  public int read(byte[] b, int off, int len) throws IOException
  {
    int result = in.read(b, off, len);


    if (result > 0)
    {
      synchronized (readData)
      {
        readData.write(b, off, result);
      }
    }
    return result;
  }

  /**
   * Returns the bytes read thru this stream since the last call of this method.<P>
   * The first call will return the bytes read since the creation of this object.<P>
   * This allows to determine which bytes were read by an decoding step. (Or determining which bytes were
   * read at all.)
   *
   * @return
   */
  public byte[] getBytes()
  {
    byte[] result;
    synchronized (readData)
    {
      result = new byte[readData.size()];
      readData.read(result);
    }
    return result;
  }

  private final CircularByteBuffer readData = new CircularByteBuffer();
}
