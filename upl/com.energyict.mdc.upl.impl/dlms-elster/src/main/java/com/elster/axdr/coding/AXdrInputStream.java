/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/axdr/coding/AXdrInputStream.java $
 * Version:     
 * $Id: AXdrInputStream.java 4793 2012-07-06 10:12:37Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  09.08.2010 18:31:03
 */
package com.elster.axdr.coding;

import com.elster.dlms.types.basic.BitString;
import com.elster.protocols.streams.SafeReadInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;

/**
 * Filter input stream to read AXdr data from an stream.
 *
 * @author osse
 */
public class AXdrInputStream extends FilterInputStream
{
  //package private for unit tests.
  static final int SUSPICIOUS_LENGTH = 1000000;

  public AXdrInputStream(final InputStream in)
  {
    super(new SafeReadInputStream(in));
  }

  /**
   * Reads an {@link BitString } from the stream.
   *
   * @return The decoded {@code BitString }
   * @throws IOException
   */
  public BitString readBitString() throws IOException
  {
    final int bitCount = readLength();
    final int byteCount = bitCount / 8 + (bitCount % 8 > 0 ? 1 : 0);
    final byte[] bytes =  readBytesCarefully(byteCount);
    return new BitString(bitCount, bytes);
  }

  /**
   * Reads an length information from the stream.<P>
   * This method is used by other methods of this class to
   * determinate the length of dynamic data (such as "VisibleString" or "OctetString").
   *
   * @return The decoded length.
   * @throws IOException
   */
  public int readLength() throws IOException
  {
    int result = in.read();

    if (0 != (result & 0x80))
    {
      final int length = result & 0x7F;

      if (length > 4)
      {
        throw new IOException("Maximum supported length exceeded.");
      }

      result = 0;

      for (int i = 0; i < length; i++)
      {
        result = result << 8;
        result = result | in.read();
      }

      if (result < 0)
      {
        throw new IOException("Maximum supported length exceeded.");
      }
    }

    return result;
  }

  /**
   * Reads an boolean value from the stream.<P>
   * This method can also be used to determinate the presence of optional data
   * or default data.
   *
   * @return The decoded boolean value.
   * @throws IOException
   */
  public boolean readBoolean() throws IOException
  {
    return 0 != in.read();
  }

  /**
   * Reads an {@code int} with dynamic length from the stream.<P>
   * If the integer value to decode is to big to fit in an {@code int} an IOException will be thrown.
   *
   * @return The decoded {@code int}.
   * @throws IOException
   */
  public int readInteger() throws IOException
  {
    final int first = read();

    if (0 == (first & 0x80))
    {
      return first;
    }
    else
    {
      int length = (first & 0x7F);

      if (length > 4)
      {
        throw new IOException("Integer to read is to big");
      }

      int result = 0;

      if (length > 0)
      {
        result = in.read();
        length--;

        if (0 != (result & 0x80))
        {
          result = result | 0xFFFFFF00; //negative Erweiterung
        }

        while (length > 0)
        {
          result = (result << 8) | in.read();
          length--;
        }
      }
      return result;
    }
  }

  /**
   * Reads an {@code int} with 8 Bits from the stream.<P>
   *
   * @return The decoded {@code int}.
   * @throws IOException
   */
  public int readInteger8() throws IOException
  {
    int value;
    value = in.read();
    if ((value & 0x80) != 0)
    {
      value = value | 0xFFFFFF00; //negative Erweiterung
    }
    return value;
  }

  /**
   * Reads an {@code int} with 16 Bits from the stream.<P>
   *
   * @return The decoded {@code int}.
   * @throws IOException
   */
  public int readInteger16() throws IOException
  {
    int value;
    value = in.read() << 8;
    value |= in.read();

    if ((value & 0x8000) != 0)
    {
      value = value | 0xFFFF0000; //negative Erweiterung
    }
    return value;
  }

  /**
   * Reads an {@code int} with 32 Bits from the stream.<P>
   *
   * @return The decoded {@code int}.
   * @throws IOException
   */
  public int readInteger32() throws IOException
  {
    int value;
    value = in.read() << 24;
    value |= in.read() << 16;
    value |= in.read() << 8;
    value |= in.read();
    return value;
  }

  /**
   * Reads an integer as {@code long} with 64 Bits from the stream.<P>
   *
   * @return The decoded {@code long}.
   * @throws IOException
   */
  public long readInteger64() throws IOException
  {
    long value;
    value = (long)in.read() << 56;
    value |= (long)in.read() << 48;
    value |= (long)in.read() << 40;
    value |= (long)in.read() << 32;
    value |= (long)in.read() << 24;
    value |= (long)in.read() << 16;
    value |= (long)in.read() << 8;
    value |= (long)in.read();
    return value;
  }

  /**
   * Reads an octet string with fix length from the stream.
   *
   * @param length The length.
   * @return Returns the octet string.
   * @throws IOException
   */
  public byte[] readOctetString(final int length) throws IOException
  {
    final byte[] data = readBytesCarefully(length);
    return data;
  }

  /**
   * Reads an octet string with variable length from the stream.
   *
   * @return The octet string.
   * @throws IOException
   */
  public byte[] readOctetString() throws IOException
  {
    final int length = readLength();
    return readOctetString(length);
  }

  /**
   * Reads an unsigned integer with 8 Bits from the stream.
   *
   * @return The decoded integer.
   * @throws IOException
   */
  public int readUnsigned8() throws IOException
  {
    return in.read();
  }

  /**
   * Reads an Tag (which must be encoded as an unsigned integer with 8 Bits) from the stream.
   *
   * @return The decoded tag.
   * @throws IOException
   */
  public int readTag() throws IOException
  {
    return in.read();
  }

  /**
   * Reads an unsigned integer with 16 Bits from the stream.
   *
   * @return The decoded integer.
   * @throws IOException
   */
  public int readUnsigned16() throws IOException
  {
    int value;
    value = in.read() << 8;
    value |= in.read();
    return value;
  }

  /**
   * Reads an unsigned integer with 32 Bits from the stream.<P>
   * The integer will be returned as long to keep the sign.
   *
   * @return The decoded integer.
   * @throws IOException
   */
  public long readUnsigned32() throws IOException
  {
    long value;
    value = ((long)in.read()) << 24;
    value |= ((long)in.read()) << 16;
    value |= ((long)in.read()) << 8;
    value |= ((long)in.read());
    return value;
  }

  /**
   * Reads an unsigned integer with 32 Bits from the stream.<P>
   * The integer will be returned as long to keep the sign.
   *
   * @return The decoded integer.
   * @throws IOException
   */
  public BigInteger readUnsigned64() throws IOException
  {
    byte[] data = new byte[9];

    data[0] = 0; //prevent negative numbers.
    in.read(data, 1, 8);
    return new BigInteger(data);
  }

  private static final String VISIBLE_STRING_CHARSET_NAME = "US-ASCII"; //VisibleString== ISO/IEC 646 == US-ASCII

  /**
   * Reads an "visible string" from the stream.<P>
   * Currently non ASCII chars will be probably decoded in a wrong way.
   *
   * @return The decoded string.
   * @throws IOException
   */
  public String readVisibleString() throws IOException
  {
    final byte data[] = readOctetString();
    return new String(data, VISIBLE_STRING_CHARSET_NAME);
  }

  private static final int CHUNK_SIZE= 10 * 1024;
  
  private byte[] readBytesCarefully(final int length) throws IOException
  {
    byte[] result;
    if (length > SUSPICIOUS_LENGTH) //If requestet length is very large, read the data in chunks and see if the input stream is really able to provide these amount of data.
    {
      final ByteArrayOutputStream tempOut = new ByteArrayOutputStream(CHUNK_SIZE);
      final byte buffer[]= new byte[CHUNK_SIZE];
      int bytesLeft = length;

      while (bytesLeft > 0)
      {
        final int chunk = Math.min(CHUNK_SIZE, bytesLeft);
        in.read(buffer, 0, chunk);
        tempOut.write(buffer, 0, chunk);
        bytesLeft-=chunk;
      }

      result=tempOut.toByteArray();
    }
    else
    {
      result= new byte[length];
      in.read(result);
    }
    return result;
  }

}
