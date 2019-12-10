/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/axdr/coding/AXdrOutputStream.java $
 * Version:     
 * $Id: AXdrOutputStream.java 3665 2011-10-04 17:34:41Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  09.08.2010 18:53:10
 */
package com.elster.axdr.coding;

import com.elster.dlms.types.basic.BitString;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Filter output stream to write A-XDR data to an output stream.
 *
 * @author osse
 */
public class AXdrOutputStream extends FilterOutputStream
{
  public AXdrOutputStream(OutputStream out)
  {
    super(out);
  }

  /**
   * Writes an length information to the stream.<P>
   * This method is used by other methods of this class to
   * write the length of dynamic data (such as "VisibleString" or "OctetString") to the stream.
   *
   * @param length The length to encode.
   * @throws IOException
   */
  public void writeLength(int length) throws IOException
  {
    if (length < 0x80)
    {
      out.write(length);
    }
    else
    {
      int s = 0;
      int temp = length;

      while (temp > 0)
      {
        s++;
        temp = temp >> 8;
      }

      out.write(s | 0x80);

      for (int i = s - 1; i >= 0; i--)
      {
        out.write(0xFF & (length >> (8 * i)));
      }
    }

  }

  /**
   * Writes an {@link BitString } to the stream.
   *
   * @param bitString The {@code BitString} to encode.
   * @throws IOException
   */
  public void writeBitString(BitString bitString) throws IOException
  {
    writeLength(bitString.getBitCount());
    out.write(bitString.getData());
  }

  /**
   * Writes an boolean to the stream.
   * 
   * @param bool The boolean to encode.
   * @throws IOException
   */
  public void writeBoolean(boolean bool) throws IOException
  {
    out.write(bool ? 1 : 0);
  }

  /**
   * Writes an integer with dynamic length to the stream.
   *
   * @param value The integer to encode.
   * @throws IOException
   */
  public void writeInteger(int value) throws IOException
  {
    if (0 <= value && value < 128)
    {
      out.write(value);
    }
    else
    {
      if (value > 0)
      {
        if (value < 0x00008000)
        {
          write(0x82);
          write(0xFF & (value >> 8));
          write(0xFF & (value));
        }
        else if (value < 0x0080000)
        {
          write(0x83);
          write(0xFF & (value >> 16));
          write(0xFF & (value >> 8));
          write(0xFF & (value));
        }
        else
        {
          write(0x84);
          write(0xFF & (value >> 24));
          write(0xFF & (value >> 16));
          write(0xFF & (value >> 8));
          write(0xFF & (value));
        }
      }
      else
      {
        if (value >= 0xFFFFFF80)
        {
          write(0x81);
          write(0xFF & value);
        }
        else if (value >= 0xFFFF8000)
        {
          write(0x82);
          write(0xFF & (value >> 8));
          write(0xFF & (value));
        }
        else if (value >= 0xFF800000)
        {
          write(0x83);
          write(0xFF & (value >> 16));
          write(0xFF & (value >> 8));
          write(0xFF & (value));
        }
        else
        {
          write(0x84);
          write(0xFF & (value >> 24));
          write(0xFF & (value >> 16));
          write(0xFF & (value >> 8));
          write(0xFF & (value));
        }
      }
    }



  }

  /**
   * Writes an integer with 8 bits to the stream.
   *
   * @param value The integer to encode.
   * @throws IOException
   */
  public void writeInteger8(int value) throws IOException
  {
    out.write(value & 0xFF);
  }

  /**
   * Writes an integer with 16 bits to the stream.
   *
   * @param value The integer to encode.
   * @throws IOException
   */
  public void writeInteger16(int value) throws IOException
  {
    out.write((value >> 8) & 0xFF);
    out.write(value & 0xFF);
  }

  /**
   * Writes an integer with 32 bits to the stream.
   *
   * @param value The integer to encode.
   * @throws IOException
   */
  public void writeInteger32(int value) throws IOException
  {
    out.write((value >> 24) & 0xFF);
    out.write((value >> 16) & 0xFF);
    out.write((value >> 8) & 0xFF);
    out.write(value & 0xFF);
  }

  /**
   * Writes an integer with 64 bits to the stream.
   *
   * @param value The integer to encode.
   * @throws IOException
   */
  public void writeInteger64(long value) throws IOException
  {
    out.write((int)(value >> 56) & 0xFF);
    out.write((int)(value >> 48) & 0xFF);
    out.write((int)(value >> 40) & 0xFF);
    out.write((int)(value >> 32) & 0xFF);
    out.write((int)(value >> 24) & 0xFF);
    out.write((int)(value >> 16) & 0xFF);
    out.write((int)(value >> 8) & 0xFF);
    out.write((int)value & 0xFF);
  }

  /**
   * Writes an octet string with fix length to the stream.<P>
   * No length information will be encoded.<br>
   * All data of the array will be written.
   *
   * @param data The octet string to encode.
   * @throws IOException
   */
  public void writeOctetStringFixLength(byte[] data) throws IOException
  {
    out.write(data);
  }

  /**
   * Writes an octet string with dynamic length to the stream.<P>
   * The length information will be encoded.<br>
   * All data of the array will be written.
   *
   * @param data The octet string to encode.
   * @throws IOException
   */
  public void writeOctetStringVariableLength(byte[] data) throws IOException
  {
    writeLength(data.length);
    out.write(data);
  }

  /**
   * Writes an unsigned integer with 8 bits to the stream.
   *
   * @param value The integer to encode.
   * @throws IOException
   */
  public void writeUnsigned8(int value) throws IOException
  {
    out.write(value & 0xFF);
  }

  /**
   * Writes an unsigned integer with 16 bits to the stream.
   *
   * @param value The integer to encode.
   * @throws IOException
   */
  public void writeUnsigned16(int value) throws IOException
  {
    out.write((value >> 8) & 0xFF);
    out.write(value & 0xFF);
  }

  /**
   * Writes an unsigned integer with 32 bits to the stream.
   *
   * @param value The integer to encode.
   * @throws IOException
   */
  public void writeUnsigned32(long value) throws IOException
  {
    out.write((int)((value >> 24) & 0xFF));
    out.write((int)((value >> 16) & 0xFF));
    out.write((int)((value >> 8) & 0xFF));
    out.write((int)(value & 0xFF));
  }

  private static final String VISIBLE_STRING_CHARSET_NAME="US-ASCII"; //VisibleString== ISO/IEC 646 == US-ASCII
  //private static final Charset VISIBLE_STRING_CHARSET= Charset.forName("US-ASCII"); //VisibleString== ISO/IEC 646 == US-ASCII

  /**
   * Writes an "visible string" to the stream.<P>
   * Currently non ASCII chars will be probably encoded in a wrong way.
   *
   * @param value The string to encode.
   * @throws IOException
   */
  public void writeVisibleString(String value) throws IOException
  {
    writeOctetStringVariableLength(value.getBytes(VISIBLE_STRING_CHARSET_NAME));
  }


  public void writeTag(int tag) throws IOException
  {
    out.write(tag);
  }

}
