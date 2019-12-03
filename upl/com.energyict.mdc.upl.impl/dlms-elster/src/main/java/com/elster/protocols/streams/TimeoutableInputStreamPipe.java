/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/protocols/streams/TimeoutableInputStreamPipe.java $
 * Version:     
 * $Id: TimeoutableInputStreamPipe.java 6772 2013-06-14 15:12:55Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  27.04.2010 14:42:21
 */
package com.elster.protocols.streams;

import java.io.IOException;
import java.io.InterruptedIOException;

/**
 * This class one side of an pipe which supports timeouts.
 * <P>
 * It can be used with an {@link TimeoutableOutputStreamPipe}<br>
 * It uses an unlimited dynamic buffer.<br>
 * It also supports the standard input stream functionality, so it can be used
 * as an normal pipe (with an dynamic buffer).<br>
 * This implementation is thread safe.
 * <P>
 * The standard input stream readTO methods will block until the requested data is available or
 * the end of data was signaled by {@link #finishPut()}.
 *
 * @author osse
 */
public class TimeoutableInputStreamPipe extends AbstractTimeoutableInputStream implements IByteSink
{
  private final static int READ_BLOCKSIZE = 16;
  //private final int readBlocksize = MIN_READ_BLOCKSIZE;
  private int timeout = 10000;
  private int totalTimeout = 60000;
  private boolean finished = false;
  private final CircularByteBuffer buffer = new CircularByteBuffer(32);

  public TimeoutableInputStreamPipe()
  {
  }

  public TimeoutableInputStreamPipe(int timeout, int totalTimeout)
  {
    this.timeout = timeout;
    this.totalTimeout = totalTimeout;
  }

  /**
   * Puts the data in the buffer.
   * <P>
   * Call {@link #flush()} to notify waiting threads.
   *
   * @param data The complete array will be put in the buffer.
   */
  public synchronized void put(byte[] data)
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
   * Puts the byte into the buffer.
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
      if (finished)
      {
        if (buffer.size() == 0)
        {
          return -1;
        }
        else
        {
          len = buffer.size();
        }
      }
      else
      {
        throw new IllegalStateException("no data available");
      }
    }

    buffer.read(b, off, len);
    return len;
  }

  @Override
  public int read() throws IOException
  {
    return readTO(timeout);
  }

  @Override
  public int read(byte[] b, int off, int len) throws IOException
  {
    return readTO(b, off, len, totalTimeout, timeout);
  }

  @Override
  public synchronized int readTO(byte[] b, int off, int len, int totalTimeoutMillis, int intervalTimeoutMillis)
          throws IOException
  {
    //shortcut
    if (buffer.size() >= len || finished)
    {
      return pull(b, off, len);
    }

    int bytesRead = 0;

    long totalEndTime;
    if (totalTimeoutMillis > 0)
    {
      totalEndTime = totalTimeoutMillis + System.currentTimeMillis();
    }
    else
    {
      totalEndTime = Long.MAX_VALUE;
    }

    long intervallEndTime = Long.MAX_VALUE;
    if (intervalTimeoutMillis > 0)
    {
      intervallEndTime = intervalTimeoutMillis + System.currentTimeMillis();
    }
    else
    {
      intervallEndTime = Long.MAX_VALUE;
    }

    while (len > 0)
    {
      while (buffer.size() < len && buffer.size() < READ_BLOCKSIZE && !finished)
      {
        try
        {
          long endTime = Math.min(intervallEndTime, totalEndTime);
          long now = System.currentTimeMillis();

          if (now > endTime)
          {
            throw new TimeoutIOException();
          }
          int oldLength = buffer.size();

          if (endTime != Long.MAX_VALUE)
          {
            wait(endTime - now + 1); //prevent 0
          }
          else
          {
            wait();
          }

          if (intervalTimeoutMillis > 0 && oldLength != buffer.size())
          {
            intervallEndTime = System.currentTimeMillis() + intervalTimeoutMillis;
          }
        }
        catch (InterruptedException ex)
        {
          throw new InterruptedIOException(ex.getMessage());
        }
      }

      if (buffer.size() >= len || finished)
      {
        int br = pull(b, off, len);
        if (br < 0)
        {
          if (bytesRead == 0)
          {
            bytesRead = -1;
          }
        }
        else
        {
          bytesRead += len;
        }
        len = 0;
      }
      else
      {
        int blocksize = buffer.size();
        pull(b, off, blocksize);
        len -= blocksize;
        off += blocksize;
        bytesRead += blocksize;
        if (intervalTimeoutMillis > 0)
        {
          intervallEndTime = System.currentTimeMillis() + intervalTimeoutMillis;
        }
      }
    }
    return bytesRead;
  }

  @Override
  public synchronized int available()
  {
    return buffer.size();
  }

  @Override
  public synchronized int readTO(int timeoutMillis) throws IOException
  {
    if (buffer.size() > 0 || finished)
    {
      return pull();
    }
    else
    {
      long endTime;

      if (timeoutMillis > 0)
      {
        endTime = timeoutMillis + System.currentTimeMillis();
      }
      else
      {
        endTime = Long.MAX_VALUE;
      }



      long timeToWait = endTime - System.currentTimeMillis();

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

      throw new TimeoutIOException();
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

  public int getTimeout()
  {
    return timeout;
  }

  public void setTimeout(int timeout)
  {
    this.timeout = timeout;
  }

  public int getTotalTimeout()
  {
    return totalTimeout;
  }

  public void setTotalTimeout(int totalTimeout)
  {
    this.totalTimeout = totalTimeout;
  }
  
  

}
