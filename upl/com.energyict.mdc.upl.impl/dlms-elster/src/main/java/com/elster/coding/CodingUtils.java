/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/coding/CodingUtils.java $
 * Version:     
 * $Id: CodingUtils.java 6186 2013-03-01 14:21:47Z leise $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  20.04.2010 13:07:45
 */
package com.elster.coding;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utilities for encoding and decoding hex strings
 *
 * @author osse
 */
public final class CodingUtils
{
  private CodingUtils()
  {
    //No instance for Utility class possible
  }

  private final static Pattern PATTERN = Pattern.compile(
          "\\s*([abcdefABCDEF0123456789][abcdefABCDEF0123456789])");
  public final static Pattern HEX_PATTERN = Pattern.compile(
          "(\\s*([abcdefABCDEF0123456789][abcdefABCDEF0123456789])\\s*)*");
  private static final char[] BASE_4_DIGIGTS =
  {
    '0', '1', '2', '3', '4', '5', '6', '7',
    '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
  };

  /**
   * Converts a hex string into a byte array.<P> The hex string must contain 2-digit pairs of hexadecimal
   * characters. The pair may be separated by whitespace.<P> Examples:<br> 01 AA BB CC ABCDAF 01AA BBCC 03 03
   * AABB <P> There is no error checking.
   *
   * @param hexString The string to convert.
   * @return The created byte array.
   */
  public static byte[] string2ByteArray(String hexString)
  {
    ByteArrayOutputStream stream = new ByteArrayOutputStream();

    Matcher matcher = PATTERN.matcher(hexString);

    while (matcher.find())
    {
      if (matcher.groupCount() >= 1)
      {
        stream.write(Integer.parseInt(matcher.group(1), 16));
      }
    }

    return stream.toByteArray();
  }

  /**
   * Checks if complete specified String can be parsed to a byte array by
   * {@link #string2ByteArray(java.lang.String)} and by {@link #string2InputStream(java.lang.String) }
   *
   * @param hexString The string to check
   * @return {@code true} if complete string can be parsed.
   */
  static public boolean isCompleteHexString(final String hexString)
  {
    return HEX_PATTERN.matcher(hexString).matches();
  }

  /**
   * Create an input stream for the specified hex string.<P> Exactly like {@link #string2ByteArray(java.lang.String)
   * }, but wraps the result in a {@code ByteArrayInputStream}.
   *
   * @param hexString The hex string.
   * @return The created byte array.
   */
  public static InputStream string2InputStream(String hexString)
  {
    return new ByteArrayInputStream(string2ByteArray(hexString));
  }

  /**
   * Converts the byte array into a string.<P> Pairs of hex digits will be separated by spaces.
   *
   * @param data The data to convert.
   * @return The hex string.
   */
  public static String byteArrayToString(final byte[] data)
  {
    return byteArrayToString(data, " ");
  }

  /**
   * Converts the byte array into a string.<P> Pairs of hex digits will be separated by the specified
   * separator.
   *
   * @param data The data to convert.
   * @param separator The separator. The separator can be empty.
   * @return The hex string.
   */
  public static String byteArrayToString(final byte[] data, final String separator)
  {

    if (data == null)
    {
      return "null";
    }

    if (data.length == 0)
    {
      return "";
    }

    StringBuilder sb = new StringBuilder(data.length * 2 * separator.length());

    sb.append(BASE_4_DIGIGTS[(0xFF & data[0]) >> 4]);
    sb.append(BASE_4_DIGIGTS[(0x0F & data[0])]);

    for (int i = 1; i < data.length; i++)
    {
      sb.append(separator);
      sb.append(BASE_4_DIGIGTS[(0xF0 & data[i]) >> 4]);
      sb.append(BASE_4_DIGIGTS[(0x0F & data[i])]);
    }

    return sb.toString();
  }

  /**
   * Converts the value into a string. The number of digits will at least the specified size.
   *
   * @param value The value to convert.
   * @param minSize The minimum size.
   * @return The string.
   */
  public static String intToHex(int value, int minSize)
  {
    StringBuilder sb = new StringBuilder(minSize);

    while (value != 0 || minSize > 0)
    {
      sb.append(BASE_4_DIGIGTS[(0x0F & value)]);
      value >>>= 4;
      minSize--;
    }
    return sb.reverse().toString();
  }

  /**
   * Converts the value into a string. The number of digits will be even, at least two and at most eight.
   *
   * @param value The value to convert.
   * @return The string.
   */
  public static String intToHex(int value)
  {
    StringBuilder sb = new StringBuilder();

    int s = 0;

    while (value != 0 || s % 2 != 0 || s < 2)
    {
      sb.append(BASE_4_DIGIGTS[(0x0F & value)]);
      value >>>= 4;
      s++;
    }
    return sb.reverse().toString();
  }
  
  /**
   * Converts the value into a string. The number of digits will at least the specified size.
   *
   * @param value The value to convert.
   * @param minSize The minimum size.
   * @return The string.
   */
  public static String longToHex(long value, int minSize)
  {
    StringBuilder sb = new StringBuilder(minSize);

    while (value != 0 || minSize > 0)
    {
      sb.append(BASE_4_DIGIGTS[(int)(0x0FL & value)]);
      value >>>= 4;
      minSize--;
    }
    return sb.reverse().toString();
  }

  /**
   * Converts the value into a string. The number of digits will be even, at least two and at most 16.
   *
   * @param value The value to convert.
   * @return The string.
   */
  public static String longToHex(long value)
  {
    StringBuilder sb = new StringBuilder();

    int s = 0;

    while (value != 0 || s % 2 != 0 || s < 2)
    {
      sb.append(BASE_4_DIGIGTS[(int)(0x0FL & value)]);
      value >>>= 4;
      s++;
    }
    return sb.reverse().toString();
  }

  /**
   * For compatibility with jdk 1.5
   */
  public static byte[] copyOfRange(byte[] original, int from, int to)
  {
    int newLength = to - from;
    if (newLength < 0)
    {
      throw new IllegalArgumentException(from + " > " + to);
    }
    byte[] copy = new byte[newLength];
    System.arraycopy(original, from, copy, 0,
                     Math.min(original.length - from, newLength));
    return copy;
  }

  /**
   * For compatibility with jdk 1.5
   */
  public static byte[] copyOf(byte[] original, int newLength)
  {
    byte[] copy = new byte[newLength];
    System.arraycopy(original, 0, copy, 0,
                     Math.min(original.length, newLength));
    return copy;
  }

  /**
   *
   */
//  public static byte[] copyOf(byte[] original)
//  {
//    byte[] copy = new byte[original.length];
//    System.arraycopy(original, 0, copy, 0,original.length);
//    return copy;
//  }
  /**
   * For compatibility with jdk 1.5
   */
//  public static <T> T[] copyOf(T[] original, int newLength)
//  {
//    @SuppressWarnings("unchecked") //Array.newInstance assures the correct type
//    T[] copy = (T[]) Array.newInstance(original.getClass().getComponentType(), newLength);
//    System.arraycopy(original, 0, copy, 0,
//                     Math.min(original.length, newLength));
//   
//    return copy;
//  }
  /**
   * For compatibility with jdk 1.5
   */
//  public static <T> T[] copyOf(T[] original)
//  {
//    @SuppressWarnings("unchecked") //Array.newInstance assures the correct type
//    T[] copy = (T[]) Array.newInstance(original.getClass().getComponentType(), original.length);
//    System.arraycopy(original, 0, copy, 0, original.length);
//    return copy;
//  }
  /**
   * For compatibility with jdk 1.5
   */
//  public static int[] copyOf(int[] original, int newLength)
//  {
//    int[] copy = new int[newLength];
//    System.arraycopy(original, 0, copy, 0,
//                     Math.min(original.length, newLength));
//    return copy;
//  }
  /**
   */
//  public static int[] copyOf(int[] original)
//  {
//    int[] copy = new int[original.length];
//    System.arraycopy(original, 0, copy, 0,original.length);
//    return copy;
//  }
}
