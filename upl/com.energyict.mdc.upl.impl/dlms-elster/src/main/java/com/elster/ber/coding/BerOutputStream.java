/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/ber/coding/BerOutputStream.java $
 * Version:     
 * $Id: BerOutputStream.java 2739 2011-03-03 17:30:21Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  05.08.2010 12:59:42
 */
package com.elster.ber.coding;

import com.elster.ber.types.BerId;
import com.elster.dlms.types.basic.BitString;
import com.elster.dlms.types.basic.ObjectIdentifier;
import java.io.ByteArrayOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Filter output stream for writing BER data.<P>
 * Adds methods for encoding the tag and the length of an TLV structure and
 * the "universal" types to an output stream.<P>
 *
 * @author osse
 */
public class BerOutputStream extends FilterOutputStream
{
  public BerOutputStream(OutputStream out)
  {
    super(out);
  }

  /**
   * Writes the specified identifier to the output stream.<P>
   * (The T of the TLV structure)
   *
   * @param identifier The identifier.
   * @throws IOException
   */
  public void writeIdentifier(BerId identifier) throws IOException
  {
    int firstByte = 0;

    firstByte |= identifier.getTag().ordinal() << 6;


    if (identifier.isConstructed())
    {
      firstByte |= 0x20;
    }

    if (identifier.getTagNumber() <= 30)
    {
      firstByte |= identifier.getTagNumber();
      write(firstByte);
    }
    else
    {

      firstByte |= 0x1F;
      write(firstByte);

      long temp = identifier.getTagNumber();
      int c = 0;

      while (temp > 0)
      {
        c++;
        temp = temp >> 7;
      }


      int subsequentByte = 0;

      for (int i = c - 1; i >= 0; i--)
      {
        subsequentByte = (int)(0x7F & (identifier.getTagNumber() >> (7 * i)));
        if (i > 0)
        {
          subsequentByte |= 0x80;
        }
        write(subsequentByte);
      }
    }
  }

  /**
   * Writes the specified length to the output stream.<P>
   * (The L of the TLV structure)
   *
   * @param length The length.
   * @throws IOException
   */
  public void writeLength(int length) throws IOException
  {
    if (length >= 0x80)
    {
      int temp = length;

      int c = 0;

      while (temp != 0)
      {
        temp = temp >> 8;
        c++;
      }
      write(0x80 | c);
      for (int i = c - 1; i >= 0; i--)
      {
        write(0xFF & (length >> (i * 8)));
      }
    }
    else
    {
      write(length);
    }
  }

  /**
   * Writes an integer to the output stream.<P>
   * The length will will be written before the value.<br>
   * (This method writes the LV of the TLV structure).
   *
   *
   * @param value The integer.
   * @throws IOException
   */
  public void writeInt(int value) throws IOException
  {
    if (value == 0)
    {
      writeLength(1);
      write(0); //ISO/IEC 8825-1:2008 (E) 8.3.1: "The contents octets shall consist of one or more octets."
    }
    else if (value > 0)
    {
      if (value < 0x00000080)
      {
        writeLength(1);
        write(value);
      }
      else if (value < 0x00008000)
      {
        writeLength(2);
        write(0xFF & (value >> 8));
        write(0xFF & (value));
      }
      else if (value < 0x0080000)
      {
        writeLength(3);
        write(0xFF & (value >> 16));
        write(0xFF & (value >> 8));
        write(0xFF & (value));
      }
      else
      {
        writeLength(4);
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
        writeLength(1);
        write(0xFF & value);
      }
      else if (value >= 0xFFFF8000)
      {
        writeLength(2);
        write(0xFF & (value >> 8));
        write(0xFF & (value));
      }
      else if (value >= 0xFF800000)
      {
        writeLength(3);
        write(0xFF & (value >> 16));
        write(0xFF & (value >> 8));
        write(0xFF & (value));
      }
      else
      {
        writeLength(4);
        write(0xFF & (value >> 24));
        write(0xFF & (value >> 16));
        write(0xFF & (value >> 8));
        write(0xFF & (value));
      }
    }
  }

  // <editor-fold defaultstate="collapsed" desc="alt. for write int">
//  public void writeInt(int value) throws IOException
//  {
//
//    if (value > 0)
//    {
//      int i = 3;
//
//      while (i > 0)
//      {
//        int leading9;
//
//        leading9 = 0x1FF & (value >> ((i * 8) - 1));
//
//        if (leading9 != 0x000)
//        {
//          break;
//        }
//        i--;
//      }
//      writeLength(i + 1);
//      while (i >= 0)
//      {
//        write(0xFF & (value >> ((i * 8))));
//        i--;
//      }
//    }
//    else if (value < 0)
//    {
//      int i = 3;
//
//      while (i > 0)
//      {
//        int leading9;
//
//        leading9 = 0x1FF & (value >> ((i * 8) - 1));
//
//        if (leading9 != 0x1FF)
//        {
//          break;
//        }
//        i--;
//      }
//      writeLength(i + 1);
//      while (i >= 0)
//      {
//        write(0xFF & (value >> ((i * 8))));
//        i--;
//      }
//    }
//    else //value==0
//    {
//      writeLength(1);
//      write(0); //ISO/IEC 8825-1:2008 (E) 8.3.1: "The contents octets shall consist of one or more octets."
//    }
//  }
// </editor-fold>
  /**
   * Writes an bit string to the output stream.<P>
   * The length will will be written before the value.<br>
   * (This method writes the LV of the TLV structure).
   *
   * @param bitString The bit string.
   * @throws IOException
   */
  public void writeBitString(BitString bitString) throws IOException
  {

    writeLength(bitString.getData().length + 1);
    write(8 * bitString.getData().length - bitString.getBitCount());
    write(bitString.getData());
  }

  /**
   * Writes an "graphic string" to the output stream.<P>
   * The length will will be written before the value.<br>
   * (This method writes the LV of the TLV structure).
   *
   * @param graphicString The "graphic string"
   * @throws IOException
   */
  public void writeGraphicString(String graphicString) throws IOException
  {
    byte[] bytes = graphicString.getBytes();

    writeLength(bytes.length);
    write(bytes);
  }


  /**
   * Writes an "octet string" to the output stream.<P>
   * The length will will be written before the value.<br>
   * (This method writes the LV of the TLV structure).
   *
   * @param bytes The "octet string"
   * @throws IOException
   */
  public void writeOctetString(byte[] bytes) throws IOException
  {
    writeLength(bytes.length);
    write(bytes);
  }

  /**
   * Writes an object identifier to the output stream.<P>
   * The length will will be written before the value.<br>
   * (This method writes the LV of the TLV structure).
   * @param oid
   * @throws IOException
   */
  public void writeObjectIdentifier(ObjectIdentifier oid) throws IOException
  {

    int[] elements = oid.getElements();

    int packedElement1 = elements[1];

    packedElement1 = packedElement1 + elements[0] * 40;


    ByteArrayOutputStream tempOut = new ByteArrayOutputStream();

    writeUInt7BitBytes(packedElement1, tempOut);

    for (int i = 2; i < elements.length; i++)
    {
      writeUInt7BitBytes(elements[i], tempOut);
    }

    byte[] bytes = tempOut.toByteArray();

    writeOctetString(bytes);
  }

  protected static int writeUInt7BitBytes(int integer, OutputStream out) throws IOException
  {
    //shortcut
    if (integer < 0x80)
    {
      out.write(integer);
      return 1;
    }

    int c = 0;
    int temp = integer;

    while (temp != 0)
    {
      c++;
      temp = temp >>> 7;
    }

    int outByte = 0;

    for (int i = c - 1; i >= 0; i--)
    {
      outByte = (0x7F & (integer >> (7 * i)));
      if (i > 0)
      {
        outByte |= 0x80;
      }
      out.write(outByte);
    }
    return c;
  }

}
