/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/protocols/streams/IntervalInputStreamPipe.java $
 * Version:     
 * $Id: IntervalInputStreamPipe.java 4017 2012-02-15 15:31:00Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  27.04.2010 14:42:21
 */
package com.elster.protocols.streams;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;

/**
 * This class one side of an pipe which supports timeouts.
 * <P>
 * It can be used with an {@link TimeoutableOutputStreamPipe}<br>
 * It uses an unlimited dynamic buffer.<br>
 * It also supports the standard input stream functionality, so it can be used
 * as an normal pipe (with an dynamic buffer).<br><P>
 * This implementation is thread safe.
 * <P>
 * The standard input stream readTO methods will block until the requested data is available or
 * the end of data was signaled by {@link #finishPut()}.
 *
 * @author osse
 */
public class IntervalInputStreamPipe extends InputStream implements IByteSink
{
  private int intervall;
  private boolean finished = false;
  private final CircularByteBuffer buffer = new CircularByteBuffer(32);

  public IntervalInputStreamPipe()
  {
    intervall = 100;
  }

  public IntervalInputStreamPipe(final int intervall)
  {
    this.intervall = intervall;
  }

  /**
   * Puts the data in the buffer.
   * <P>
   * Call {@link #flush()} to notify waiting threads.
   *
   * @param data The complete array will be put in the buffer.
   */
  public synchronized void put(final byte[] data)
  {
    if (buffer.capacity() < data.length + buffer.size())
    {
      notifyAll();
    }

    buffer.write(data);
    //notifyAll();
  }

  /**
   * Puts the data in the buffer.
   * <P>
   * Call {@link #flush()} to notify waiting threads.
   *
   * @param data  The data.
   * @param offset An offset in the data array.
   * @param len The amount of data to put.
   */
  public synchronized void put(byte[] data, int offset, int len)
  {
    if (buffer.capacity() < len + buffer.size())
    {
      notifyAll();
    }

    buffer.write(data, offset, len);
    //notifyAll();
  }

  /**
   * Puts one byte into the buffer.
   * <P>
   * Call {@link #flush()} to notify waiting threads.*
   *
   * @param singleByte One byte to put in the buffer.
   */
  public synchronized void put(byte singleByte)
  {
    if (buffer.capacity() < 1 + buffer.size())
    {
      notifyAll();
    }

    buffer.write(singleByte);
    //notifyAll();
  }

  private int pull()
  {
    if (buffer.size() == 0)
    {
      if (finished)
      {
        return -1;
      }
      else
      {
        throw new IllegalStateException("no data available");
      }
    }
    return buffer.read();
  }

  private int pull(byte[] b, int off, int len)
  {

    if (len > buffer.size())
    {
      if (finished && buffer.size() == 0)
      {
        return -1;
      }
      else
      {
        len = buffer.size();
      }
    }

    buffer.read(b, off, len);
    return len;
  }

  @Override
  public synchronized int read(byte[] b, int off, int len)
          throws IOException
  {
    //shortcut
    if (buffer.size() >= len || finished)
    {
      return pull(b, off, len);
    }

    try
    {
      int bytesRead = 0;
      long endTime = intervall + System.currentTimeMillis();

      while (len > 0)
      {
        int br = pull(b, off, len);
        if (br < 0)
        {
          if (bytesRead == 0)
          {
            bytesRead = -1;
          }
          break;
        }

        len -= br;
        off += br;
        bytesRead += br;

        if (len > 0)
        {
          long now = System.currentTimeMillis();
          if (now >= endTime)
          {
            break;
          }
          wait(endTime - now);
        }

      }
      return bytesRead;
    }
    catch (InterruptedException ex)
    {
      throw new InterruptedIOException(ex.getMessage());
    }
  }

  @Override
  public synchronized int available() throws IOException
  {
    return buffer.size();
  }

  @Override
  public synchronized int read() throws IOException
  {
    if (buffer.size() > 0 || finished)
    {
      return pull();
    }
    else
    {
      final long endTime = System.currentTimeMillis() + intervall;
      long timeToWait = intervall;

      while (timeToWait > 0)
      {
        try
        {
          wait(timeToWait);
          if (buffer.size() > 0 || finished)
          {
            return pull();
          }
        }
        catch (InterruptedException ex)
        {
          throw new InterruptedIOException(ex.getMessage());
        }
        timeToWait = endTime - System.currentTimeMillis();
      }
      return -2;
    }
  }

  /**
   * Call this method to indicate that no more data will be written to the buffer.
   * <P>
   * After calling this method the put methods must not be called anymore.
   *
   */
  public synchronized void finishPut()
  {
    finished = true;
    if (buffer.capacity() > 128 && buffer.capacity() > 2 * buffer.size())
    {
      pack();
    }
    notifyAll();
  }

  /**
   * Notifies waiting threads.
   */
  public synchronized void flush()
  {
    notifyAll();
  }

  /**
   * Returns {@code true} if no more data will be put to the buffer.
   * <P>
   * See {@link #finishPut() }
   *
   * @return  {@code true} if no more data will be put to the buffer. Otherwise {@code false}
   */
  public synchronized boolean isFinished()
  {
    return finished;
  }

  private void pack()
  {
    buffer.pack();
  }

//  public CircularByteBuffer getBuffer()
//  {
//    return buffer;
//  }
//
//  public void setBuffer(CircularByteBuffer buffer)
//  {
//    this.buffer = buffer;
//  }
  public synchronized int getIntervall()
  {
    return intervall;
  }

  public synchronized void setIntervall(int intervall)
  {
    this.intervall = intervall;
  }

}
