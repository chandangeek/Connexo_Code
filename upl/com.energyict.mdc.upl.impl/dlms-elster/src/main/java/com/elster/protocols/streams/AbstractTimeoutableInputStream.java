/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/protocols/streams/AbstractTimeoutableInputStream.java $
 * Version:     
 * $Id: AbstractTimeoutableInputStream.java 2649 2011-02-08 13:52:13Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  29.04.2010 10:13:47
 */
package com.elster.protocols.streams;

import java.io.IOException;
import java.io.InputStream;

/**
 * This class is base class for stream which provides read methods with timeouts.
 *
 * @author osse
 */
public abstract class AbstractTimeoutableInputStream extends InputStream implements ITimeoutControl
{
  /**
   * Reads {@code len} bytes to {@code  b} starting from {@code off} respecting the given timeouts.<P>
   * If an timeout occurs an {@link TimeoutIOException} is thrown.
   *
   * @param b The destination buffer.
   * @param off An offset for the destination buffer.
   * @param len The amount of bytes to readTO.
   * @param totalTimeoutMillis The total time allowed for this operation in milliseconds. If this value is set to 0 the total timeout is ignored.
   * @param intervalTimeoutMillis The time allowed between to bytes.  If this value is set to 0 the interval timeout is ignored.
   * @return The bytes readTO.
   * @throws IOException
   */
  public abstract int readTO(byte[] b, int off, int len, int totalTimeoutMillis, int intervalTimeoutMillis)
          throws IOException;

  /**
   * Reads one byte.
   *
   * @param timeoutMillis The time allowed for reading the byte.
   * @return The byte
   * @throws IOException
   */
  public abstract int readTO(int timeoutMillis) throws IOException;

  /**
   * Reads {@code b.length} bytes respecting the given timeouts.
   * <P>
   * The implementation simply calls <br>
   * {@code return readTO(b, 0, b.length, totalTimeoutMillis, intervalTimeoutMillis);}
   *
   *
   * @param b The destination buffer.
   * @param totalTimeoutMillis he total time allowed for this operation in milliseconds. If this value is set to 0 the total timeout is ignored.
   * @param intervalTimeoutMillis The time allowed between to bytes.  If this value is set to 0 the intervall timeout is ignored.
   * @return The bytes readTO.
   * @throws IOException
   */
  public int readTO(byte[] b, int totalTimeoutMillis, int intervalTimeoutMillis)
          throws IOException
  {
    return readTO(b, 0, b.length, totalTimeoutMillis, intervalTimeoutMillis);
  }
}


