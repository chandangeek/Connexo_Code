/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/types/basic/BitString.java $
 * Version:     
 * $Id: BitString.java 3767 2011-11-15 10:53:23Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  19.05.2010 15:57:55
 */
package com.elster.dlms.types.basic;

import java.util.Arrays;

/**
 * This class holds an bit string.<P>
 * Objects of this class are immutable. For easy creation BitStringBuilder can be used.
 *
 * @author osse
 */
public class BitString
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
  public BitString(final int bitCount, final byte[] data)
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
   * Creates the bit string using all bytes of the {@code data} parameter .<P>
   *
   * @param bytes The bytes
   */
  public BitString(final byte[] bytes)
  {
    this(bytes.length * 8, bytes);
  }

  /**
   * Creates an BitString which holds {@code bitCount} Bits.
   *
   * @param bitCount The bit count
   */
//  public BitString(int bitCount)
//  {
//    this.bitCount = bitCount;
//    this.data = new byte[bitCount / 8 + (bitCount % 8 == 0 ? 0 : 1)];
//  }
  /**
   * Returns the count of bits.
   *
   * @return The count of bits.
   */
  public int getBitCount()
  {
    return bitCount;
  }
  
  /**
   * Returns the number of bits set in the BitString
   * 
   * @return Returns the number of bits set in the BitString
   */

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
   * Returns a byte array which holds the bits.
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
  public boolean isBitSet(final int bitNo)
  {
    return 0 != ((0x80 >> (bitNo % 8)) & data[bitNo / 8]);
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
    final StringBuilder stringBuilder = new StringBuilder();


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

  /**
   * String presentation
   *
   * @return The string
   */
  public String toString(final boolean group, final int maxBits)
  {
    final StringBuilder stringBuilder = new StringBuilder();

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

      if (group && (i % 8 == 7))
      {
        stringBuilder.append(" ");
      }

      if (maxBits > 0 && i > maxBits)
      {
        stringBuilder.append("...");
        break;
      }
    }
    return stringBuilder.toString();
  }

  /**
   * Returns an array with the bit numbers of the active (set) bits. (zero based)<P>
   * 
   * ({@link #isBitSet(int)} will return {@code true} for every entry of the returned array and {@code false} for all other integers.)
   * 
   * @return An array with the bit numbers of the active (set) bits.
   */
  public int[] getActiveBits()
  {
    final int populationCount = getPopulationCount();
    int[] result = new int[getPopulationCount()];
    int pos = 0;

    for (int i = 0; i < bitCount; i++)
    {
      if (pos >= populationCount)
      {
        break;
      }

      if (i % 8 == 0 && data[i / 8] == 0)
      {
        i += 7;
      }
      else if (isBitSet(i))
      {
        result[pos] = i;
        pos++;
      }
    }
    return result;
  }

  @Override
  public boolean equals(final Object obj)
  {
    if (obj == null)
    {
      return false;
    }
    if (getClass() != obj.getClass())
    {
      return false;
    }
    final BitString other = (BitString)obj;
    if (this.bitCount != other.bitCount)
    {
      return false;
    }
    if (!Arrays.equals(this.data, other.data))
    {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode()
  {
    int hash = 5;
    hash = 79 * hash + this.bitCount;
    hash = 79 * hash + Arrays.hashCode(this.data);
    return hash;
  }

}
