/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/protocols/streams/HeaderAddingInputStream.java $
 * Version:     
 * $Id: HeaderAddingInputStream.java 3665 2011-10-04 17:34:41Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  18.05.2010 10:15:40
 */
package com.elster.protocols.streams;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * This class is an FilterInputStream that adds an predefined header to an other input stream.
 *
 * @author osse
 */
public class HeaderAddingInputStream extends FilterInputStream
{
  private byte[] header;
  private int headerBytesLeft;

  /**
   * Constructor
   *
   * @param in The "other input stream (see class description).
   * @param header The header to add.
   */
  public HeaderAddingInputStream(final InputStream in, final byte[] header)
  {
    super(in);
    this.header = header.clone();
    headerBytesLeft = header.length;
  }

  @Override
  public int available() throws IOException
  {
    return headerBytesLeft + in.available();
  }

  @Override
  public boolean markSupported()
  {
    return false;
  }

  @Override
  public int read() throws IOException
  {
    if (headerBytesLeft > 0)
    {
      int b = 0xFF & header[header.length - headerBytesLeft];
      headerBytesLeft--;

      if (headerBytesLeft == 0)
      {
        header = null;
      }
      return b;
    }
    else
    {
      return in.read();
    }

  }

  @Override
  public int read(byte[] b, int off, int len) throws IOException
  {

    if (headerBytesLeft > 0)
    {
      int l = Math.min(headerBytesLeft, len);
      System.arraycopy(header, header.length - headerBytesLeft, b, off, l);
      headerBytesLeft -= l;
      off += l;
      len -= l;

      if (headerBytesLeft == 0)
      {
        header = null;
      }

      //more data to read
      if (len > 0)
      {
        int bytesRead = in.read(b, off, len);
        if (bytesRead > 0)
        {
          return bytesRead + l;
        }
        else
        {
          return l; //EOF
        }
      }
      else
      {
        return l;
      }
    }
    else
    {
      return in.read(b, off, len);
    }
  }

  @Override
  public long skip(long n) throws IOException
  {
    if (headerBytesLeft > 0)
    {
      if (n <= headerBytesLeft)
      {
        headerBytesLeft -= n;
        return n;
      }
      else
      {
        int skipedBytes = headerBytesLeft;
        n -= headerBytesLeft;
        headerBytesLeft = 0;
        return skipedBytes + in.skip(n);
      }
    }
    else
    {
      return in.skip(n);
    }
  }
}
