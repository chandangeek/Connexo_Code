/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/protocols/streams/SafeReadInputStream.java $
 * Version:     
 * $Id: SafeReadInputStream.java 2343 2010-11-15 17:05:58Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  30.04.2010 11:46:12
 */
package com.elster.protocols.streams;

import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * This class ensures that the read methods returning the desired amount of data
 * -otherwise an EOFException is thrown.
 * <P>
 * This means, even the end of file condition
 * (normally resulting in an return value of -1 in the read methods) leads to
 * an IOException.
 * <P>
 * The skip method is not directly handled by this class. If an underlaying
 * input
 *
 * @author osse
 */
public class SafeReadInputStream extends FilterInputStream
{
  public SafeReadInputStream(InputStream in)
  {
    super(in);
  }

  @Override
  public int read() throws IOException
  {
    int b = in.read();
    if (b < 0)
    {
      throw new EOFException();
    }
    return b;
  }

  @Override
  public int read(byte[] b, int off, int len) throws IOException
  {
    int bytesRead = 0;
    int block = 0;

    while (bytesRead < len)
    {
      block = in.read(b, off+bytesRead, len-bytesRead);

      if (block < 0)
      {
        throw new EOFException();
      }

      bytesRead += block;
    }
    return bytesRead;
  }

  @Override
  public long skip(long len) throws IOException
  {
    long bytesSkiped = 0;
    long block = 0;

    while (bytesSkiped < len)
    {
      block = in.skip(len-bytesSkiped);

      if (block < 0)
      {
        throw new EOFException();
      }

      bytesSkiped += block;
    }
    return bytesSkiped;
  }




}
