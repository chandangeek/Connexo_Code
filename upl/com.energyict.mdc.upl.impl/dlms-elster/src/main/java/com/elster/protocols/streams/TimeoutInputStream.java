/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/protocols/streams/TimeoutInputStream.java $
 * Version:     
 * $Id: TimeoutInputStream.java 4874 2012-07-19 15:23:43Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  Feb 8, 2011 11:15:59 AM
 */
package com.elster.protocols.streams;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Input stream which checks for timeout.
 * <P>
 * If a timeout occurs a {@link TimeoutIOException} will be thrown.
 * <P>
 * This stream expects that the read methods of the underlying stream
 * are returning after a short time, if no data was received.
 *
 * @author osse
 */
public class TimeoutInputStream extends FilterInputStream
{
  int defaultTimeout = 5000;

  public TimeoutInputStream(final InputStream in)
  {
    super(in);
  }

  /**
   * Constructor 
   * 
   * @param in The underlying input stream. 
   * @param timeoutMs The time out for the inherited read methods.
   */
  public TimeoutInputStream(final InputStream in, final int timeoutMs)
  {
    super(in);
    defaultTimeout = timeoutMs;
  }

  @Override
  public int read() throws IOException
  {
    final byte oneByte[] = new byte[1];
    int r = read(oneByte, 0, 1);

    if (r <= 0)
    {
      return r;
    }
    else
    {
      return 0xFF & oneByte[0];
    }
  }

  /**
   * Reads one octet respecting the specified timeout.
   * 
   * @param timeMs The timeout in milliseconds
   * @return The octet or -1 if the end of file was reached.
   * @throws IOException 
   */
  public int readTO(final int timeMs) throws IOException
  {
    final byte oneByte[] = new byte[1];
    final int r = readTO(oneByte, 0, 1, timeMs);

    if (r < 0)
    {
      return r;
    }
    else
    {
      return 0xFF & oneByte[0];
    }
  }

  @Override
  public int read(final byte[] b, final int off, final int len) throws IOException
  {
    return readTO(b, off, len, defaultTimeout);
  }

  /**
   * Like {@link #read(byte[], int, int)} with an specified timeout.
   * 
   * @param b The buffer.
   * @param off The offset in the buffer.
   * @param len The number of bytes to read.
   * @param timeoutMs The timeout 
   * @return The number of bytes read.
   * @throws IOException 
   */
  public int readTO(final byte[] b, int off, int len, final int timeoutMs) throws IOException
  {
    long startTime = System.currentTimeMillis();
    int totalBytesRead = 0;

    while (len > 0)
    {
      final int br = in.read(b, off, len);

      if (br < 0) //EOF
      {
        if (totalBytesRead > 0)
        {
          return totalBytesRead;
        }
        else
        {
          return br;
        }
      }

      if (br > 0)
      {
        startTime = System.currentTimeMillis();
        off += br;
        len -= br;
        totalBytesRead += br;
      }
      else
      {
        if (System.currentTimeMillis() > startTime + timeoutMs)
        {
          throw new TimeoutIOException();
        }
      }
    }

    return totalBytesRead;
  }

  public int getTimeout()
  {
    return defaultTimeout;
  }

  public void setTimeout(final int timeout)
  {
    this.defaultTimeout = timeout;
  }

}
