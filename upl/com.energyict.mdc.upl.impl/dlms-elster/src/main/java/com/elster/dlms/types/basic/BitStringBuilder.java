/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/types/basic/BitStringBuilder.java $
 * Version:     
 * $Id: BitStringBuilder.java 3665 2011-10-04 17:34:41Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  19.05.2010 15:57:55
 */
package com.elster.dlms.types.basic;

/**
 * Helper class to build BitStrings
 *
 * @author osse
 */
public class BitStringBuilder
{
  private final int bitCount;
  private final byte[] data;

  /**
   * Constructor. <P>
   * The data must have exactly the size to hold {@code bitCount} bits.<P>
   * The need length can be calculated by {@code bitCount / 8 + (bitCount % 8 == 0 ? 0 : 1)}
   *
   * @param bitCount The bit count.
   * @param data The data.
   */
  public BitStringBuilder(final int bitCount,final byte[] data)
  {

    if (data.length != bitCount / 8 + (bitCount % 8 == 0 ? 0 : 1))
    {
      throw new IllegalArgumentException("Wrong length of the data array. Expected length:"
                                         + bitCount / 8 + (bitCount % 8 == 0
              ? 0 : 1) + " Actual length:" + data.length);
    }


    this.bitCount = bitCount;
    this.data = data.clone();
  }

  /**
   * Creates an BitString which holds {@code bitCount} Bits.
   *
   * @param bitCount The bit count
   */
  public BitStringBuilder(int bitCount)
  {
    this.bitCount = bitCount;
    this.data = new byte[bitCount / 8 + (bitCount % 8 == 0 ? 0 : 1)];
  }

  /**
   * Returns the count of bits.
   *
   * @return The count of bits.
   */
  public int getBitCount()
  {
    return bitCount;
  }

  public int getPopulationCount()
  {
    int result = 0;
    for (byte b : data)
    {
      result += Integer.bitCount(b & 0xFF);
    }
    return result;
  }

  /**
   * Returns the byte array which holds the bits.
   *
   * @return The data.
   */
  public byte[] getData()
  {
    return data.clone();
  }

  /**
   * Check if be specified bit is set.
   *
   * @param bitNo The bit to check (zero based).
   * @return {@code true} if the bit is set.
   */
  public boolean isBitSet(int bitNo)
  {
    return 0 != ((0x80 >> (bitNo % 8)) & data[bitNo / 8]);
  }

  /**
   * Sets the or deletes the specified bit.
   *
   * @param bitNo The bit to set or to delete (zero based).
   * @param set {@code true} to set, {@code false} to delete.
   */
  public void setBit(int bitNo, boolean set)
  {
    if (set)
    {
      data[bitNo / 8] |= (0x80 >> (bitNo % 8));
    }
    else
    {
      data[bitNo / 8] &= 0xFF ^ (0x80 >> (bitNo % 8));
    }
  }

  /**
   * Clears all bits.
   *
   */
  public void clearBits()
  {
    //Arrays.fill(data, (byte) 0); // this method does the same.
    for (int i = 0; i < data.length; i++)
    {
      data[i] = 0;
    }
  }

  /**
   * String for debugging purposes<P>
   * Truncated after 800 bits, grouped in 8 bit groups.
   *
   * @return The string
   */
  @Override
  public String toString()
  {
    StringBuilder stringBuilder = new StringBuilder();


    stringBuilder.append("Bit count: ").append(bitCount).append(", ");
    stringBuilder.append("Bits: ");

    for (int i = 0; i < bitCount; i++)
    {
      if (isBitSet(i))
      {
        stringBuilder.append("1");
      }
      else
      {
        stringBuilder.append("0");
      }

      if (i % 8 == 7)
      {
        stringBuilder.append(" ");
      }

      if (i > 800)
      {
        stringBuilder.append("...");
        break;
      }
    }
    return stringBuilder.toString();
  }

  public BitString toBitString()
  {
    return new BitString(bitCount,data);
  }
}
