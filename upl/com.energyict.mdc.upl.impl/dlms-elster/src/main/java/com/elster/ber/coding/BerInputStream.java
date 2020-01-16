/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/ber/coding/BerInputStream.java $
 * Version:     
 * $Id: BerInputStream.java 6722 2013-06-11 10:17:36Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  28.07.2010 10:09:40
 */
package com.elster.ber.coding;

import com.elster.ber.types.BerId;
import com.elster.dlms.types.basic.BitString;
import com.elster.dlms.types.basic.ObjectIdentifier;
import com.elster.protocols.streams.CountingInputStream;
import com.elster.protocols.streams.SafeReadInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Filter input stream for reading BER data.<P> Adds methods for decoding the tag and the length of an TLV
 * structure and the "universal" types from an input stream.<P> It also provides an "position" attribute which
 * counts the bytes read through this stream.
 *
 * @author osse
 */
public class BerInputStream extends FilterInputStream
{
  /**
   * Constructor with {@code in} as sub input stream.
   *
   * @param in The underlying input stream.
   */
  public BerInputStream(InputStream in)
  {
    super(new CountingInputStream(new SafeReadInputStream(in)));
  }

  /**
   * Returns the position (the number of bytes read through this stream.
   *
   * @return The position.
   */
  public int getPosition()
  {
    return ((CountingInputStream)in).getCount();
  }

  /**
   * Read an {@link BerId} from the stream.<P> This is the T of the TLV structure.
   *
   * @return The {@code BerIndetifier}
   * @throws IOException
   */
  public BerId readIdentifier() throws IOException
  {
    int firstByte = read();

    int tagPart = (firstByte >> 6) & 0x03;

    BerId.Tag tag = BerId.Tag.values()[tagPart]; //2 bits = 4 values

    boolean constructed = 0 != (firstByte & 0x20);

    long tagNumber = 0;

    if (0x1F != (firstByte & 0x1F))
    {
      tagNumber = firstByte & 0x1F;
    }
    else
    {
      tagNumber = 0;
      int subsequentByte = 0;
      do
      {
        subsequentByte = read();
        if (0 != (tagNumber >> 56)) //Es dürfen weder relevante Bits entfernt werden, noch darf das 1. Bit gesetzt sein (negative Zahl).
        {
          throw new IOException("tag numbers with more than 63 bits are not supported");
        }

        tagNumber = (tagNumber << 7) | (subsequentByte & 0x7F);
      }
      while (0 != (subsequentByte & 0x80));
    }

    return new BerId(tag, constructed, tagNumber);
  }

  /**
   * Read an length information from the stream.<P> This is the L of the TLV structure.
   *
   * @return The length (Number of bytes for the value V of the TLV structure).
   * @throws IOException
   */
  public int readLength() throws IOException
  {
    int result = read();

    if (0 != (result & 0x80))
    {
      if (result == 0x80)
      {
        throw new IOException("The indefinite form is not suported");
      }


      int lengthByteCount = result & 0x7F;
      result = 0;

      for (int i = 0; i < lengthByteCount; i++)
      {
        result = result << 8;
        result = result | read();
      }
    }
    return result;
  }

  public int readUniversialInteger() throws IOException
  {
    BerId id = readIdentifier();
    checkTag(BerIds.ID_INT, id);
    return readInt();
  }

  /**
   * Reads an int starting by the length byte(s).
   *
   * @return The read integer.
   * @throws IOException
   */
  public int readInt() throws IOException
  {
    return readInt(readLength());
  }

  /**
   * Reads an int with the specified length.
   *
   * @param contentLength The length in bytes.
   * @return The read integer.
   * @throws IOException
   */
  public int readInt(int contentLength) throws IOException
  {
    int result = 0;

    if (contentLength > 0)
    {
      int firstByte = read();

      if (0 != (firstByte & 0x80)) //negativ
      {
        result = ~result;
      }

      result = (result << 8) | firstByte;
    }

    for (int i = 1; i < contentLength; i++)
    {
      result = (result << 8) | read();
    }

    return result;
  }
  
  public BitString readUniversalBitString() throws IOException
  {
    BerId id= readIdentifier();
    checkTag(BerIds.ID_BITSTRING, id);
    return readBitString();
  }

  /**
   * Reads an {@link BitString } starting by the length byte(s).
   *
   * @return The read {@link BitString }.
   * @throws IOException
   */
  public BitString readBitString() throws IOException
  {
    return readBitString(readLength());
  }

  /**
   * Reads an {@link BitString } with the specified length.
   *
   * @param contentLength The length in bytes (at least 1).
   * @return The read {@link BitString}.
   * @throws IOException
   */
  public BitString readBitString(int contentLength) throws IOException
  {
    if (contentLength < 1) //see ISO/IEC 8825-1:2008 (E)  - 8.6.2
    {
      throw new IOException("The content length of a bit string must be at least 1: Was:" + contentLength);
    }

    int bitsLeft = read();

    if (bitsLeft < 0 || bitsLeft > 7) //see ISO/IEC 8825-1:2008 (E)  - 8.6.2.2
    {
      throw new IOException("The number of 'bits left' in the bit string must be between 0 and 7");
    }

    if (contentLength == 1 && bitsLeft != 0)//see ISO/IEC 8825-1:2008 (E)  - 8.6.2.3
    {
      throw new IOException("The number of 'bits left' for an empty bit-string shall be 0");
    }


    int bitCount = 8 * (contentLength - 1) - bitsLeft;

    byte[] data = new byte[contentLength - 1];

    for (int i = 1; i < contentLength; i++)
    {
      data[i - 1] = (byte)read();
    }

    return new BitString(bitCount, data);
  }

  /**
   * Reads an "graphic string" starting by the length byte(s).
   *
   * @return The read "graphic string".
   * @throws IOException
   */
  public String readGraphicString() throws IOException
  {
    return readGraphicString(readLength());
  }

  /**
   * Reads an "graphic string" with the specified length.
   *
   * @param contentLength The length in bytes.
   * @return The read "graphic string".
   * @throws IOException
   */
  public String readGraphicString(int contentLength) throws IOException
  {
    String value;

    byte[] data = new byte[contentLength];

    read(data); //Full amount of data is ensured by the underlying SafeInputStream
    value = new String(data,"ASCII");

    return value;
  }

  public String readUniversialVisibleString() throws IOException
  {
    final BerId id = readIdentifier();
    checkTag(BerIds.ID_VISIBLE_STRING, id);
    return readVisibleString();
  }

  /**
   * Reads an "visible string" starting by the length byte(s).
   *
   * @return The read "graphic string".
   * @throws IOException
   */
  public String readVisibleString() throws IOException
  {
    return readGraphicString(readLength());
  }

  /**
   * Reads an "visible string" with the specified length.
   *
   * @param contentLength The length in bytes.
   * @return The read "graphic string".
   * @throws IOException
   */
  public String readVisibleString(int contentLength) throws IOException
  {
    String value;

    byte[] data = new byte[contentLength];

    read(data); //Full amount of data is ensured by the underlying SafeInputStream
    value = new String(data);

    return value;
  }

  /**
   * Reads an "octet string" starting by the length byte(s).
   *
   * @return The read "octet string".
   * @throws IOException
   */
  public byte[] readOctetString() throws IOException
  {
    return readOctetString(readLength());
  }

  public boolean readUniversialBoolean() throws IOException
  {
    final BerId id = readIdentifier();
    checkTag(BerIds.ID_BOOLEAN, id);
    return readBoolean();
  }

  public boolean readBoolean() throws IOException
  {
    return readBoolean(readLength());
  }

  public boolean readBoolean(final int contentLength) throws IOException
  {
    if (contentLength != 1)
    {
      throw new IOException("For booleans the content length must be 1. Was: " + contentLength);
    }
    return 0 != readInt(1);
  }

  /**
   * Reads an "octet string" with the specified length.
   *
   * @param contentLength The length in bytes.
   * @return The read "octet string".
   * @throws IOException
   */
  public byte[] readOctetString(int contentLength) throws IOException
  {
    byte[] data = new byte[contentLength];
    read(data, 0, contentLength); //Full amount of data is ensured by the underlying SafeInputStream
    return data;
  }

  /**
   * Reads an {@link ObjectIdentifier } starting by the length byte(s).
   *
   * @return The read {@link ObjectIdentifier }.
   * @throws IOException
   */
  public ObjectIdentifier readObjectIdentifer() throws IOException
  {
    return readObjectIdentifer(readLength());
  }

  /**
   * Reads an {@link ObjectIdentifier } with the specified length.
   *
   * @param contentLength The length in bytes.
   * @return The read {@link ObjectIdentifier }.
   * @throws IOException
   */
  public ObjectIdentifier readObjectIdentifer(int contentLength) throws IOException
  {

    List<Integer> elements = new ArrayList<Integer>(contentLength + 1);

    elements.add(0); //place holder for unpacking the packed element.

    for (int i = 0; i < contentLength; i++)
    {
      int octet = read();
      int element = octet & 0x7F;

      while ((octet & 0x80) != 0)
      {
        octet = read();
        i++;
        element = (element << 7) | (octet & 0x7F);
      }
      elements.add(element);
    }

    if (elements.size() < 2)
    {
      throw new IOException("The object identifier must contain 2 elements at least. Was: " + elements.size());
    }


    //unpack
    int element0 = 0;
    int element1 = elements.get(1);

    // Special handling for the first packed OID element
    if (element1 >= 80)
    {
      element0 = 2;
      element1 = element1 - 80;
    }
    else
    {
      element0 = element1 / 40;
      element1 = element1 % 40;
    }

    elements.set(0, element0);
    elements.set(1, element1);

    return new ObjectIdentifier(elements);
  }
  
  public void skipTLV() throws IOException
  {
    readIdentifier();
    skip(readLength());
  }
  
  
  public void skipLV() throws IOException
  {
    skip(readLength());
  }

  public static void checkTag(final BerId expected, final BerId actual) throws IOException
  {
    if (!expected.equals(actual))
    {
      throw new IOException("Unexpected ID. Expected:" + expected + " Actual:" + actual);
    }
  }

}
