/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/protocols/streams/FcsChecksumOutputStream.java $
 * Version:     
 * $Id: FcsChecksumOutputStream.java 2430 2010-12-06 13:56:06Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  30.04.2010 11:08:47
 */
package com.elster.protocols.streams;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * This class calculates the FCS16 checksum on the fly.
 * <P>
 * The checksum can be written by the {@link #writeChecksum()} method.
 *
 * @author osse
 */
public class FcsChecksumOutputStream extends FilterOutputStream
{
  int currentChecksum = Fcs16Builder.INITIAL_VALUE;

  public FcsChecksumOutputStream(OutputStream out)
  {
    super(out);
  }

  /**
   * Resets the checksum.
   *
   */
  public void resetChecksum()
  {
    currentChecksum = Fcs16Builder.INITIAL_VALUE;
  }

  /**
   * Returns the current checksum.
   *
   * @return The checksum
   */
  public int getChecksum()
  {
    return currentChecksum;
  }

  /**
   * Writes the (inverted) checksum to the stream.<P>
   * The current checksum will be updated by the checksum bytes itself. So 
   * it can be used to calculate the header checksum and the frame checksum.
   * 
   * @return the count (2) of bytes written.
   * @throws IOException
   */
  public int writeChecksum() throws IOException
  {
    int checksumToWrite = Fcs16Builder.invertFcs16(currentChecksum);
    write(checksumToWrite & 0xFF);
    write((checksumToWrite >> 8) & 0xFF);
    return 2;
  }

  @Override
  public void write(int b) throws IOException
  {
    currentChecksum = Fcs16Builder.updateFcs16(currentChecksum, b);
    out.write(b);
  }

  @Override
  public void write(byte[] b, int off, int len) throws IOException
  {
    currentChecksum = Fcs16Builder.updateFcs16(currentChecksum, b, off, len);
    out.write(b, off, len);
  }

}
