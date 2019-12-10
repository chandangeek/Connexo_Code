/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/protocols/streams/Fcs16Builder.java $
 * Version:     
 * $Id: Fcs16Builder.java 3665 2011-10-04 17:34:41Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  30.04.2010 08:45:47
 */
package com.elster.protocols.streams;

/**
 * This class builds FCS16 checksums.
 * <P>
 * To check if an checksum is correct, the checksum can be updated with
 * checksum bytes itself. The resulting checksum must be equal to the GOOD_CHECKSUM constant.
 * <P>
 * To write the checksum it must be inverted. This can be done by {@link #invertFcs16(int)}
 *
 * @author osse
 */
public final class Fcs16Builder
{

  private Fcs16Builder()
  {
    //no instances allowed
  }
  
  
  private final static short[] FCS_TABLE;

  /**
   * Initial value for the FCS.
   * 
   */
  public final static int INITIAL_VALUE = 0xFFFF;

  
  /**
   * After the correct check sum was read the fcs has this value.
   */
  public final static int GOOD_CHECKSUM = 0xF0B8;


  
  /**
   * Updates the checksum.
   * 
   * @param fcs The old checksum.
   * @param data The data.
   * @param offset The first byte in the data array.
   * @param len The length in the data array.
   * @return The updated checksum.
   */
  public static int updateFcs16(int fcs, byte[] data, int offset, int len)
  {
    for (int i = 0; i < len; i++)
    {
      fcs = 0xFFFF & ((fcs >> 8) ^ FCS_TABLE[(fcs ^ (0xFF & data[i + offset])) & 0xFF]);
    }
    return fcs;
  }

  /**
   * Updates the check sum by an single byte.
   *
   * @param fcs The old checksum.
   * @param date One byte (0-255) for update. (The range will not be verified)
   * @return The new checksum
   */
  public static int updateFcs16(int fcs, int date)
  {
    fcs = 0xFFFF & ((fcs >> 8) ^ FCS_TABLE[(fcs ^ (0xFF & date)) & 0xFF]);
    return fcs;
  }

  /**
   * Inverts the checksum.
   *
   * @param fcs The checksum.
   * @return The inverted checksum.
   */
  public static int invertFcs16(int fcs)
  {
    return 0xFFFF & (0xFFFF ^ fcs);
  }


  
  /**
   * Returns one entry of (internall) checksum table.
   * <P>
   * (used for unit tests)
   * 
   * @param index The index (0-255)
   * @return The entry.
   */
  static short getTableEntry(int index)
  {
    return FCS_TABLE[index];
  }

  private final static int P = 0x8408;

  static
  {
    FCS_TABLE = new short[256];

    int v;
    int i;

    for (int b = 0; b < 256; b++)
    {
      v = b;
      for (i = 8; i > 0; i--)
      {
        v = 0 != (v & 1) ? (v >> 1) ^ P : v >> 1;
      }
      FCS_TABLE[b] = (short)(0xFFFF & v);
    }
  }

}
