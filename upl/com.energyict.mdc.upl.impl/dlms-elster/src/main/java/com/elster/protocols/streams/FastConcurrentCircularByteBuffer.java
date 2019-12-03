/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/protocols/streams/FastConcurrentCircularByteBuffer.java $
 * Version:     
 * $Id: FastConcurrentCircularByteBuffer.java 3665 2011-10-04 17:34:41Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  27.04.2010 14:42:21
 */
package com.elster.protocols.streams;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * This class implements an circular FIFO byte buffer.<P>
 * This class is thread safe.<P>
 * The buffer automatically increases as needed.
 *
 * @author osse
 */
public class FastConcurrentCircularByteBuffer
{
  private byte[] buffer = new byte[1];
  private int first = 0;
  private int last = -1;
  private final AtomicInteger atomicLength = new AtomicInteger(0);
  ReentrantReadWriteLock readLock = new ReentrantReadWriteLock();
  ReentrantReadWriteLock writeLock = new ReentrantReadWriteLock();

  /**
   * Sole constructor.
   */
  public FastConcurrentCircularByteBuffer()
  {
  }

  /**
   * Constructs the buffer with the specified initial capacity.
   * <P>
   * An initial capacity of 0 (or less) will be ignored and the default initial capacity of 1
   * will be used.
   *
   *
   * @param initialCapacity The initial capacity.
   */
  public FastConcurrentCircularByteBuffer(int initialCapacity)
  {
    if (initialCapacity > 0)
    {
      buffer = new byte[initialCapacity];
    }
  }

  //writeLock.writeLock() must be locked
  private void ensureAdditionalCapacity(int countOfElementsToAdd)
  {

    if ( buffer.length >=  atomicLength.get() + countOfElementsToAdd)
    {
      return; //nothing to do.
    }

    readLock.writeLock().lock();
    try
    {
      int bufferSize = buffer.length;
      int length = atomicLength.get();

      if (bufferSize < length + countOfElementsToAdd)
      {
        int newBufferSize = (length + countOfElementsToAdd);

//        newBufferSize = newBufferSize + 10* countOfElementsToAdd;
        newBufferSize = 2 * newBufferSize;


        byte[] oldBuffer = buffer;
        buffer = new byte[newBufferSize];

        if (first <= last)
        {
          System.arraycopy(oldBuffer, first, buffer, 0, length);
        }
        else
        {
          System.arraycopy(oldBuffer, first, buffer, 0, bufferSize - first);
          System.arraycopy(oldBuffer, 0, buffer, bufferSize - first, last + 1);
        }

        first = 0;
        last = length - 1;
      }
    }
    finally
    {
      readLock.writeLock().unlock();
    }
  }

  /**
   * Sets the internal buffer size to the count of bytes. <P>
   * The content will be preserved. This method can be called to free unused buffer. <P>
   * (The capacity of an empty buffer will be set to 1)
   */
  public void pack()
  {
    readLock.writeLock().lock();
    writeLock.writeLock().lock();
    try
    {
      int bufferSize = buffer.length;
      int length = atomicLength.get();

      if (bufferSize != length)
      {
        int newBufferSize = length;
        if (newBufferSize == 0)
        {
          newBufferSize = 1;
        }

        byte[] oldBuffer = buffer;
        buffer = new byte[newBufferSize];

        if (first <= last)
        {
          System.arraycopy(oldBuffer, first, buffer, 0, length);
        }
        else
        {
          System.arraycopy(oldBuffer, first, buffer, 0, bufferSize - first);
          System.arraycopy(oldBuffer, 0, buffer, bufferSize - first, last + 1);
        }

        first = 0;
        last = length - 1;
      }
    }
    finally
    {
      writeLock.writeLock().unlock();
      readLock.writeLock().unlock();
    }
  }

  /**
   * Writes the data to the buffer.
   *
   * @param data The data.
   */
  public void write(byte[] data)
  {
    write(data, 0, data.length);
  }

  /**
   * Writes the data to the buffer.
   *
   * @param data The data.
   * @param offset The start of bytes to write.
   * @param len The count of bytes to write.
   */
  public void write(byte[] data, int offset, int len)
  {
    writeLock.writeLock().lock();
    try
    {
      ensureAdditionalCapacity(len);
      int elementsAtEnd = buffer.length - last - 1;

      if (len <= elementsAtEnd)
      {
        System.arraycopy(data, offset, buffer, last + 1, len);
        last = last + len;
      }
      else
      {
        System.arraycopy(data, offset, buffer, last + 1, elementsAtEnd);
        System.arraycopy(data, offset + elementsAtEnd, buffer, 0, len - elementsAtEnd);
        last = (last + len) % buffer.length;
      }
      atomicLength.addAndGet(len);
    }
    finally
    {
      writeLock.writeLock().unlock();
    }

  }

  /**
   * Writes one byte to the buffer.
   *
   * @param singleByte The byte
   */
  public void write(byte singleByte)
  {
    writeLock.writeLock().lock();
    try
    {
      ensureAdditionalCapacity(1);
      last = (last + 1) % buffer.length;
      buffer[last] = singleByte;
      atomicLength.getAndIncrement();
    }
    finally
    {
      writeLock.writeLock().unlock();
    }
  }

  /**
   * Writes one byte to the buffer.
   *
   * @param singleByte The byte
   */
  public void write(int singleByte)
  {
    write((byte)singleByte);
  }

  /**
   * Reads one byte from the buffer
   *
   * @return The byte as integer (0-255)
   */
  public int read()
  {
    readLock.writeLock().lock();
    try
    {
      int result = peek();
      first = (first + 1) % buffer.length;
      atomicLength.getAndDecrement();
      return result;
    }
    finally
    {
      readLock.writeLock().unlock();
    }
  }

  /**
   * Gets one byte from the buffer, without deleting it.
   *
   * @return The byte
   */
  public int peek()
  {
    readLock.readLock().lock();
    try
    {
      if (atomicLength.get() == 0)
      {
        throw new IllegalStateException("no data available");
      }

      int result = 0xFF & buffer[first];

      return result;
    }
    finally
    {
      readLock.readLock().unlock();
    }
  }

  /**
   * Copies {@code len} bytes to {@code b} starting from {@code off}.<P>
   * The bytes will be deleted from the buffer.
   *
   * @param b The destination buffer.
   * @param off The offset in the destination buffer.
   * @param len The count of bytes to copy
   */
  public void read(byte[] b, int off, int len)
  {
    readLock.writeLock().lock();
    try
    {
      peek(b, off, len);
      first = (first + len) % buffer.length;
      atomicLength.getAndAdd(-len);
    }
    finally
    {
      readLock.writeLock().unlock();
    }
  }

  /**
   * Deletes {@code len} bytes from the buffer.
   * 
   * @param len
   */
  public void skip(int len)
  {
    readLock.writeLock().lock();
    try
    {
      if (len > atomicLength.get())
      {
        throw new IllegalStateException("no data available");
      }
      first = (first + len) % buffer.length;
      atomicLength.getAndAdd(-len);
    }
    finally
    {
      readLock.writeLock().unlock();
    }
  }

  /**
   * Copies {@code len} bytes to {@code b} starting from {@code off}.<P>
   * The bytes will <b>not</b> be deleted from the buffer.
   *
   * @param b The destination buffer.
   * @param off The offset in the destination buffer.
   * @param len The count of bytes to copy
   */
  public void peek(byte[] b, int off, int len)
  {
    readLock.readLock().lock();
    try
    {
      if (len > atomicLength.get())
      {
        throw new IllegalStateException("no data available");
      }

      if (first + len < buffer.length)
      {
        System.arraycopy(buffer, first, b, off, len);
      }
      else
      {
        int bytesAtEnd = buffer.length - first;
        System.arraycopy(buffer, first, b, off, bytesAtEnd);
        System.arraycopy(buffer, 0, b, off + bytesAtEnd, len - bytesAtEnd);
      }
    }
    finally
    {
      readLock.readLock().unlock();
    }

  }

  /**
   * Copies {@code b.length} bytes and deletes from the buffer to {@code b}
   *
   * @param b The destination buffer
   */
  public void read(byte[] b)
  {
    read(b, 0, b.length);
  }

  /**
   * Copies {@code b.length} bytes from the buffer to {@code b}
   *
   * @param b The destination buffer
   */
  public void peek(byte[] b)
  {
    peek(b, 0, b.length);
  }

  /**
   * Returns the size of the buffer
   *
   * @return The size of the buffer.
   */
  public int size()
  {
    return atomicLength.get();
  }

  /**
   * Returns the (current) capacity of the buffer.
   *
   * @return the capacity.
   */
  public int capacity()
  {
    readLock.readLock().lock();
    try
    {
      return buffer.length;
    }
    finally
    {
      readLock.readLock().unlock();
    }
  }

  /**
   * Deletes the complete content.<P>
   * The capacity of the internal buffer will not be changed. To free the
   * internal buffer call {@link #pack()}
   *
   */
  public void clear()
  {
    readLock.writeLock().lock();
    writeLock.writeLock().lock();
    try
    {
      first = 0;
      last = -1;
      atomicLength.set(0);
    }
    finally
    {
      readLock.writeLock().unlock();
      writeLock.writeLock().unlock();
    }
  }

}
