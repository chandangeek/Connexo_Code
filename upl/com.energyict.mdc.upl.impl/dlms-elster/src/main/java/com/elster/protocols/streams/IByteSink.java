/* File:
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/protocols/streams/IByteSink.java $
 * Version:
 * $Id: IByteSink.java 2649 2011-02-08 13:52:13Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  Feb 8, 2011 10:53:13 AM
 */

package com.elster.protocols.streams;

/**
 * This interface ...
 *
 * @author osse
 */
public interface IByteSink
{

  /**
   * Call this method to indicate that no more data will be written to the buffer.
   * <P>
   * After calling this method the put methods must not be called anymore.
   *
   */
  void finishPut();

  /**
   * Notifies waiting threads.
   */
  void flush();

  /**
   * Puts the data in the buffer.
   * <P>
   * Call {@link #flush()} to notify waiting threads.
   *
   * @param data The complete array will be put in the buffer.
   */
  void put(byte[] data);

  /**
   * Puts the data in the buffer.
   * <P>
   * Call {@link #flush()} to notify waiting threads.
   *
   * @param data  The data.
   * @param offset An offset in the data array.
   * @param len The amount of data to put.
   */
  void put(byte[] data, int offset, int len);

  /**
   * Puts one byte into the buffer.
   * <P>
   * Call {@link #flush()} to notify waiting threads.*
   *
   * @param singleByte One byte to put in the buffer.
   */
  void put(byte singleByte);

}
