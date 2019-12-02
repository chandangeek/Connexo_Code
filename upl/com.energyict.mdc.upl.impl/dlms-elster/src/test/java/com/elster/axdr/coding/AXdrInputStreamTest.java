/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elster.axdr.coding;

import com.elster.coding.CodingUtils;
import com.elster.dlms.types.basic.BitString;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Random;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author osse
 */
public class AXdrInputStreamTest
{
  public AXdrInputStreamTest()
  {
  }

  /**
   * Test of readBitString method, of class AXdrInputStream.
   */
  @Test
  public void testReadBitString() throws Exception
  {
    System.out.println("readBitString");
    AXdrInputStream instance = new AXdrInputStream(CodingUtils.string2InputStream("13 01 67 87"));
    try
    {
      BitString expResult = new BitString(19, new byte[]
              {
                (byte)0x01, (byte)0x67, (byte)0x87
              });
      BitString result = instance.readBitString();
      assertEquals(expResult, result);
    }
    finally
    {
      instance.close();
    }

  }

  /**
   * Test of readLength method, of class AXdrInputStream.
   */
  @Test
  public void testReadLength() throws Exception
  {
    System.out.println("readLength");
    AXdrInputStream instance = new AXdrInputStream(CodingUtils.string2InputStream(
            "00 01 7F 81FF 82FFFF 8401351497"));
    try
    {
      int length1 = 0;
      int length2 = 1;
      int length3 = 0x7F;
      int length4 = 0xFF;
      int length5 = 0xFFFF;
      int length6 = 0x1351497;

      int result1 = instance.readLength();
      int result2 = instance.readLength();
      int result3 = instance.readLength();
      int result4 = instance.readLength();
      int result5 = instance.readLength();
      int result6 = instance.readLength();
      assertEquals(length1, result1);
      assertEquals(length2, result2);
      assertEquals(length3, result3);
      assertEquals(length4, result4);
      assertEquals(length5, result5);
      assertEquals(length6, result6);
    }
    finally
    {
      instance.close();
    }
  }

  /**
   * Test of readLength method, of class AXdrInputStream.
   */
  @Test(expected = IOException.class)
  public void testReadInvalidLength1() throws Exception
  {
    System.out.println("readLength");
    AXdrInputStream instance = new AXdrInputStream(CodingUtils.string2InputStream(
            "84 FF FF FF FF"));
    try
    {
      final int result = instance.readLength();
      System.out.println("Should not be possible to decode. Result: " + result);
    }
    finally
    {
      instance.close();
    }
  }

  /**
   * Test of readLength method, of class AXdrInputStream.
   */
  @Test(expected = IOException.class)
  public void testReadInvalidLength2() throws Exception
  {
    System.out.println("readLength");
    AXdrInputStream instance = new AXdrInputStream(CodingUtils.string2InputStream(
            "85 01 02 03 04 05"));
    try
    {
      final int result = instance.readLength();
      System.out.println("Should not be possible to decode. Result: " + result);
    }
    finally
    {
      instance.close();
    }
  }

  /**
   * Test of readBoolean method, of class AXdrInputStream.
   */
  @Test
  public void testReadBoolean() throws Exception
  {
    System.out.println("readBoolean");
    AXdrInputStream instance = new AXdrInputStream(CodingUtils.string2InputStream("01 01 00 01"));
    try
    {
      boolean result1 = instance.readBoolean();
      boolean result2 = instance.readBoolean();
      boolean result3 = instance.readBoolean();
      boolean result4 = instance.readBoolean();
      assertEquals(true, result1);
      assertEquals(true, result2);
      assertEquals(false, result3);
      assertEquals(true, result4);
    }
    finally
    {
      instance.close();
    }
  }

  /**
   * Test of readInteger method, of class AXdrInputStream.
   */
  @Test
  public void testReadInteger() throws Exception
  {
    System.out.println("readInteger");
    AXdrInputStream instance = new AXdrInputStream(CodingUtils.string2InputStream("7B 00 81FF 820080 8180"));
    try
    {

      int value1 = 0x7B;
      int value2 = 0x00;
      int value3 = -1;
      int value4 = 128;
      int value5 = -128;

      int result1 = instance.readInteger();
      int result2 = instance.readInteger();
      int result3 = instance.readInteger();
      int result4 = instance.readInteger();
      int result5 = instance.readInteger();

      assertEquals(value1, result1);
      assertEquals(value2, result2);
      assertEquals(value3, result3);
      assertEquals(value4, result4);
      assertEquals(value5, result5);
    }
    finally
    {
      instance.close();
    }
  }

  /**
   * Test of readInteger8 method, of class AXdrInputStream.
   */
  @Test
  public void testReadInteger8() throws Exception
  {
    System.out.println("readInteger8");
    AXdrInputStream instance = new AXdrInputStream(CodingUtils.string2InputStream("00 AB BA FF"));
    try
    {

      int value1 = 0;
      int value2 = -85;
      int value3 = -70;
      int value4 = -1;

      int result1 = instance.readInteger8();
      int result2 = instance.readInteger8();
      int result3 = instance.readInteger8();
      int result4 = instance.readInteger8();

      assertEquals(value1, result1);
      assertEquals(value2, result2);
      assertEquals(value3, result3);
      assertEquals(value4, result4);
    }
    finally
    {
      instance.close();
    }
  }

  /**
   * Test of readInteger16 method, of class AXdrInputStream.
   */
  @Test
  public void testReadInteger16() throws Exception
  {
    System.out.println("readInteger16");
    AXdrInputStream instance = new AXdrInputStream(CodingUtils.string2InputStream("0000 00AB 7ABC FABC FFFF"));
    try
    {
      int value1 = 0;
      int value2 = 0xAB;
      int value3 = 0x7ABC;
      int value4 = 0xFFFFFABC;
      int value5 = 0xFFFFFFFF;

      int result1 = instance.readInteger16();
      int result2 = instance.readInteger16();
      int result3 = instance.readInteger16();
      int result4 = instance.readInteger16();
      int result5 = instance.readInteger16();

      assertEquals(value1, result1);
      assertEquals(value2, result2);
      assertEquals(value3, result3);
      assertEquals(value4, result4);
      assertEquals(value5, result5);
    }
    finally
    {
      instance.close();
    }
  }

  /**
   * Test of readInteger32 method, of class AXdrInputStream.
   */
  @Test
  public void testReadInteger32() throws Exception
  {
    System.out.println("readInteger32");
    AXdrInputStream instance = new AXdrInputStream(CodingUtils.string2InputStream(
            "00000000 000000AB FAFFAABB FFFFFFFF"));
    try
    {

      int value1 = 0;
      int value2 = 0xAB;
      int value3 = 0xFAFFAABB;
      int value4 = 0xFFFFFFFF;

      int result1 = instance.readInteger32();
      int result2 = instance.readInteger32();
      int result3 = instance.readInteger32();
      int result4 = instance.readInteger32();

      assertEquals(value1, result1);
      assertEquals(value2, result2);
      assertEquals(value3, result3);
      assertEquals(value4, result4);
    }
    finally
    {
      instance.close();
    }
  }

  /**
   * Test of readInteger64 method, of class AXdrInputStream.
   */
  @Test
  public void testReadInteger64() throws Exception
  {
    System.out.println("readInteger64");
    AXdrInputStream instance =
            new AXdrInputStream(
            CodingUtils.string2InputStream(
            "0000000000000000 00000000000000AB 00000000FAFFAABB 00000000FFFFFFFF 000000ABCDEF1234 FFFFFFFFFFFFFFFF"));
    try
    {

      long value1 = 0;
      long value2 = 0xABL;
      long value3 = 0xFAFFAABBL;
      long value4 = 0xFFFFFFFFL;
      long value5 = 0xABCDEF1234L;
      long value6 = 0xFFFFFFFFFFFFFFFFL;

      long result1 = instance.readInteger64();
      long result2 = instance.readInteger64();
      long result3 = instance.readInteger64();
      long result4 = instance.readInteger64();
      long result5 = instance.readInteger64();
      long result6 = instance.readInteger64();


      assertEquals(value1, result1);
      assertEquals(value2, result2);
      assertEquals(value3, result3);
      assertEquals(value4, result4);
      assertEquals(value5, result5);
      assertEquals(value6, result6);
    }
    finally
    {
      instance.close();
    }
  }

  /**
   * Test of readOctetString method, of class AXdrInputStream.
   */
  @Test
  public void testReadOctetString_int() throws Exception
  {
    System.out.println("readOctetString");
    int length = 5;
    AXdrInputStream instance = new AXdrInputStream(CodingUtils.string2InputStream("00 AB CC DE FF"));
    try
    {
      byte[] expResult = CodingUtils.string2ByteArray("00 AB CC DE FF");
      byte[] result = instance.readOctetString(length);
      assertArrayEquals(expResult, result);
    }
    finally
    {
      instance.close();
    }
  }

  /**
   * Test of readOctetString method, of class AXdrInputStream.
   */
  @Test
  public void testReadOctetString_int_huge() throws Exception
  {
    System.out.println("readOctetString huge");
    int length = AXdrInputStream.SUSPICIOUS_LENGTH * 2 + 343347;
    byte data[] = new byte[length];
    Random r = new Random(902379056L);
    r.nextBytes(data);

    final AXdrInputStream instance = new AXdrInputStream(new ByteArrayInputStream(data));
    try
    {
      final byte[] expResult = data;
      final byte[] result = instance.readOctetString(length);
      assertTrue(Arrays.equals(expResult, result)); //This call is much slower: assertArrayEquals(expResult, result);
    }
    finally
    {
      instance.close();
    }
  }

  /**
   * Test of readOctetString method, of class AXdrInputStream.
   */
  @Test
  public void testReadOctetString_0args() throws Exception
  {
    System.out.println("readOctetString");
    AXdrInputStream instance = new AXdrInputStream(CodingUtils.string2InputStream("05 00 AB CC DE FF"));
    try
    {
      byte[] expResult = CodingUtils.string2ByteArray("00 AB CC DE FF");
      byte[] result = instance.readOctetString();
      assertArrayEquals(expResult, result);
    }
    finally
    {
      instance.close();
    }
  }

  /**
   * Test of readUnsigned8 method, of class AXdrInputStream.
   */
  @Test
  public void testReadUnsigned8() throws Exception
  {
    System.out.println("readUnsigned8");
    AXdrInputStream instance = new AXdrInputStream(CodingUtils.string2InputStream("00 AB BA FF"));
    try
    {

      int value1 = 0;
      int value2 = 0xAB;
      int value3 = 0xBA;
      int value4 = 0xFF;

      int result1 = instance.readUnsigned8();
      int result2 = instance.readUnsigned8();
      int result3 = instance.readUnsigned8();
      int result4 = instance.readUnsigned8();

      assertEquals(value1, result1);
      assertEquals(value2, result2);
      assertEquals(value3, result3);
      assertEquals(value4, result4);
    }
    finally
    {
      instance.close();
    }
  }

  /**
   * Test of readTag method, of class AXdrInputStream.
   */
  @Test
  public void testReadTag() throws Exception
  {
    System.out.println("readTag");
    AXdrInputStream instance = new AXdrInputStream(CodingUtils.string2InputStream("00 AB BA FF"));
    try
    {

      int value1 = 0;
      int value2 = 0xAB;
      int value3 = 0xBA;
      int value4 = 0xFF;

      int result1 = instance.readTag();
      int result2 = instance.readTag();
      int result3 = instance.readTag();
      int result4 = instance.readTag();

      assertEquals(value1, result1);
      assertEquals(value2, result2);
      assertEquals(value3, result3);
      assertEquals(value4, result4);

    }
    finally
    {
      instance.close();
    }

  }

  /**
   * Test of readUnsigned16 method, of class AXdrInputStream.
   */
  @Test
  public void testReadUnsigned16() throws Exception
  {
    System.out.println("readUnsigned8");
    AXdrInputStream instance = new AXdrInputStream(CodingUtils.string2InputStream("0000 00AB 7ABC FABC FFFF"));
    try
    {

      int value1 = 0;
      int value2 = 0xAB;
      int value3 = 0x7ABC;
      int value4 = 0xFABC;
      int value5 = 0xFFFF;

      int result1 = instance.readUnsigned16();
      int result2 = instance.readUnsigned16();
      int result3 = instance.readUnsigned16();
      int result4 = instance.readUnsigned16();
      int result5 = instance.readUnsigned16();

      assertEquals(value1, result1);
      assertEquals(value2, result2);
      assertEquals(value3, result3);
      assertEquals(value4, result4);
      assertEquals(value5, result5);
    }
    finally
    {
      instance.close();
    }
  }

  /**
   * Test of readUnsigned32 method, of class AXdrInputStream.
   */
  @Test
  public void testReadUnsigned32() throws Exception
  {
    System.out.println("readUnsigned32");
    AXdrInputStream instance = new AXdrInputStream(CodingUtils.string2InputStream(
            "00000000 000000AB FAFFAABB FFFFFFFF"));

    try
    {

      long value1 = 0;
      long value2 = 0xAB;
      long value3 = 0xFAFFAABBL;
      long value4 = 0xFFFFFFFFL;

      long result1 = instance.readUnsigned32();
      long result2 = instance.readUnsigned32();
      long result3 = instance.readUnsigned32();
      long result4 = instance.readUnsigned32();

      assertEquals(value1, result1);
      assertEquals(value2, result2);
      assertEquals(value3, result3);
      assertEquals(value4, result4);
    }
    finally
    {
      instance.close();
    }
  }

  /**
   * Test of readUnsigned32 method, of class AXdrInputStream.
   */
  @Test
  public void testReadUnsigned64() throws Exception
  {
    System.out.println("readUnsigned64");
    AXdrInputStream instance =
            new AXdrInputStream(
            CodingUtils.string2InputStream(
            "0000000000000000 00000000000000AB 00000000FAFFAABB 00000000FFFFFFFF 000000ABCDEF1234 FFFFFFFFFFFFFABC  FFFFFFFFFFFFFFFF"));

    try
    {

      BigInteger value1 = BigInteger.valueOf(0);
      BigInteger value2 = BigInteger.valueOf(0xABL);
      BigInteger value3 = BigInteger.valueOf(0xFAFFAABBL);
      BigInteger value4 = BigInteger.valueOf(0xFFFFFFFFL);
      BigInteger value5 = BigInteger.valueOf(0xABCDEF1234L);
      BigInteger value6 = new BigInteger("18446744073709550268");
      BigInteger value7 = new BigInteger("18446744073709551615");

      BigInteger result1 = instance.readUnsigned64();
      BigInteger result2 = instance.readUnsigned64();
      BigInteger result3 = instance.readUnsigned64();
      BigInteger result4 = instance.readUnsigned64();
      BigInteger result5 = instance.readUnsigned64();
      BigInteger result6 = instance.readUnsigned64();
      BigInteger result7 = instance.readUnsigned64();

      assertEquals(value1, result1);
      assertEquals(value2, result2);
      assertEquals(value3, result3);
      assertEquals(value4, result4);
      assertEquals(value5, result5);
      assertEquals(value6, result6);
      assertEquals(value7, result7);
    }
    finally
    {
      instance.close();
    }
  }

  /**
   * Test of readVisibleString method, of class AXdrInputStream.
   */
  @Test
  public void testReadVisibleString() throws Exception
  {
    System.out.println("readVisibleString");

    String expResult = "Hallo A-XDR";

    byte[] bytes = new byte[expResult.length() + 1];
    System.arraycopy(expResult.getBytes(), 0, bytes, 1, expResult.length());
    bytes[0] = (byte)expResult.length();

    AXdrInputStream instance = new AXdrInputStream(new ByteArrayInputStream(bytes));
    try
    {
      String result = instance.readVisibleString();
      assertEquals(expResult, result);
    }
    finally
    {
      instance.close();
    }
  }

}
