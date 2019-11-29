/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/protocols/streams/InputStreamMultiplexer.java $
 * Version:     
 * $Id: InputStreamMultiplexer.java 3665 2011-10-04 17:34:41Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  26.05.2010 14:59:02
 */
package com.elster.protocols.streams;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * This class copies the input from one input stream to a predefined number of other inputstreams.
 * <P>
 * <b>This implementation does not work at the moment<b>
 *
 * @author osse
 */
class InputStreamMultiplexer

{
  private final InputStream source;
  private final ConcurrentCircularByteBuffer circularByteBuffer = new ConcurrentCircularByteBuffer();
  private long edgePos = 0;
  private final InternalInputStream[] ins;
  ReentrantReadWriteLock edgePosLock = new ReentrantReadWriteLock();
  ReentrantReadWriteLock fillLock = new ReentrantReadWriteLock();

  public InputStreamMultiplexer(InputStream source, int count)
  {
    this.source = source;
    ins = new InternalInputStream[count];
    for (int i = 0; i < count; i++)
    {
      ins[i] = new InternalInputStream();
    }

    throw new UnsupportedOperationException("This implementation does not work at the moment");
  }

  public InputStream getInputStream(int index)
  {
    return ins[index];
  }

  public void open()
  {
  }

  public void close()
  {
  }


  private final byte[] transportBuffer = new byte[128];

  private int fill(long checkSize) throws IOException
  {
    fillLock.writeLock().lock();
    try
    {
      edgePosLock.readLock().lock();
      try
      {
        if (checkSize != circularByteBuffer.size() + edgePos)
        {
          return (int)(circularByteBuffer.size() + edgePos - checkSize);
        }
      }
      finally
      {
        edgePosLock.readLock().unlock();
      }

      trimEdge();

      if (source.available() > 0)
      {
        int s = Math.min(source.available(), transportBuffer.length);
        int bytesRead = source.read(transportBuffer, 0, s);
        if (bytesRead > 0)
        {
          circularByteBuffer.write(transportBuffer, 0, bytesRead);
        }
        return bytesRead;
      }
      else
      {
        int singleByte = source.read();

        if (singleByte >= 0)
        {
          circularByteBuffer.write(singleByte);
          return 1;
        }
        else
        {
          return singleByte;
        }
      }
    }
    finally
    {
      fillLock.writeLock().unlock();
    }




  }

  private void trimEdge()
  {

    if (edgePosLock.writeLock().tryLock())
    {
      try
      {

        long minPos = ins[0].position;

        for (int i = 1; i < ins.length; i++)
        {
          if (minPos == edgePos)
          {
            return;
          }

          if (ins[i].position < minPos)
          {
            minPos = ins[i].position;
          }

        }
        if (minPos == edgePos)
        {
          return;
        }
        circularByteBuffer.skip((int)(minPos - edgePos));
        edgePos = minPos;
      }
      finally
      {
        edgePosLock.writeLock().unlock();
      }
    }
  }

  class InternalInputStream extends InputStream
  {
    private final byte[] singleByteBuffer = new byte[1];
    long position;

    @Override
    public int read() throws IOException
    {
      if (1 == read(singleByteBuffer, 0, 1))
      {
        return singleByteBuffer[0];
      }
      else
      {
        return -1;
      }
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException
    {
      int bytesRead = len;

      while (bytesRead < len)
      {
        long realSize;

        edgePosLock.readLock().lock();
        try
        {
          realSize = circularByteBuffer.size() + edgePos;
        }
        finally
        {
          edgePosLock.readLock().unlock();
        }

        int chunk = (int)Math.min(realSize - position, len - bytesRead);

        if (chunk == 0)
        {
          int fillResult = fill(realSize);
          if (fillResult < 0)
          {
            return fillResult;
          }
        }
        else
        {
          edgePosLock.readLock().lock();
          try
          {
            circularByteBuffer.peek(b, (int)(off + (position - edgePos)), chunk);
            bytesRead += chunk;
          }
          finally
          {
            edgePosLock.readLock().unlock();
          }
          position += bytesRead;
        }
      }
      return bytesRead;

    }

  }

}
