/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/protocols/streams/FcsChecksumInputStream.java $
 * Version:     
 * $Id: FcsChecksumInputStream.java 3665 2011-10-04 17:34:41Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  30.04.2010 09:16:39
 */
package com.elster.protocols.streams;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * This class can check an FCS checksum on the fly.
 *
 * @author osse
 */
public class FcsChecksumInputStream extends FilterInputStream
{
  int currentChecksum = Fcs16Builder.INITIAL_VALUE;

  /**
   * Resets the current checksum. <P>
   * This method must be called directly before the first byte of range
   * to check was read.
   *
   */
  public void resetChecksum()
  {
    currentChecksum = Fcs16Builder.INITIAL_VALUE;
  }

  /**
   * Returns the current checksum.
   *
   * @return The current checksum.
   */
  public int getChecksum()
  {
    return currentChecksum;
  }

  /**
   * Return true if the current is ok.<P>
   * This method must be called <b>after</b> the checksum was read.
   *
   * @return {@code true} if checksum is correct, {@code false} if checksum is not correct.
   */
  public boolean isChecksumOk()
  {
    return currentChecksum == Fcs16Builder.GOOD_CHECKSUM;
  }

  /**
   * Throws an exception if the checksum is not correct.
   * <P>
   * This method must be called <b>after</b> the checksum was read.
   *
   *
   * @throws ChecksumIOException
   */
  public void checkChecksum() throws ChecksumIOException
  {
    if (!isChecksumOk())
    {
      throw new ChecksumIOException();
    }
  }

  public FcsChecksumInputStream(InputStream in)
  {
    super(in);
  }

  @Override
  public int read() throws IOException
  {
    int b = in.read();
    if (b >= 0)
    {
      currentChecksum = Fcs16Builder.updateFcs16(currentChecksum, b);
    }
    return b;
  }

  @Override
  public int read(byte[] b, int off, int len) throws IOException
  {
    int bytesRead = in.read(b, off, len);
    currentChecksum = Fcs16Builder.updateFcs16(currentChecksum, b, off, bytesRead);
    return bytesRead;
  }

}
