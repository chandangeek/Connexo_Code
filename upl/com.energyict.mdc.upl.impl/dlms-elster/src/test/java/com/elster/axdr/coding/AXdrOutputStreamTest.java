/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elster.axdr.coding;

import com.elster.coding.CodingUtils;
import com.elster.dlms.types.basic.BitString;
import java.io.ByteArrayOutputStream;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author osse
 */
public class AXdrOutputStreamTest
{
  public AXdrOutputStreamTest()
  {
  }

  @BeforeClass
  public static void setUpClass() throws Exception
  {
  }

  @AfterClass
  public static void tearDownClass() throws Exception
  {
  }

  /**
   * Test of writeLength method, of class AXdrOutputStream.
   */
  @Test
  public void testWriteLength() throws Exception
  {
    System.out.println("writeLength");
    int length1 = 0;
    int length2 = 1;
    int length3 = 0x7F;
    int length4 = 0xFF;
    int length5 = 0xFFFF;
    int length6 = 0x1351497;

    byte[] expecteds = CodingUtils.string2ByteArray("00 01 7F 81FF 82FFFF 8401351497");
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    AXdrOutputStream instance = new AXdrOutputStream(out);
    instance.writeLength(length1);
    instance.writeLength(length2);
    instance.writeLength(length3);
    instance.writeLength(length4);
    instance.writeLength(length5);
    instance.writeLength(length6);
    assertArrayEquals(expecteds, out.toByteArray());
  }

  /**
   * Test of writeBitString method, of class AXdrOutputStream.
   */
  @Test
  public void testWriteBitString() throws Exception
  {
    System.out.println("writeBitString");
    BitString bitString = new BitString(19, new byte[]
            {
              (byte)0x01, (byte)0x67, (byte)0x87
            });

    byte[] expecteds = CodingUtils.string2ByteArray("13 01 67 87");
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    AXdrOutputStream instance = new AXdrOutputStream(out);
    instance.writeBitString(bitString);

    assertArrayEquals(expecteds, out.toByteArray());
  }

  /**
   * Test of writeBoolean method, of class AXdrOutputStream.
   */
  @Test
  public void testWriteBoolean() throws Exception
  {
    System.out.println("writeBoolean");
    byte[] expecteds = CodingUtils.string2ByteArray("01 01 00 01");
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    AXdrOutputStream instance = new AXdrOutputStream(out);
    instance.writeBoolean(true);
    instance.writeBoolean(true);
    instance.writeBoolean(false);
    instance.writeBoolean(true);

    assertArrayEquals(expecteds, out.toByteArray());
  }

  /**
   * Test of writeInteger method, of class AXdrOutputStream.
   */
  @Test
  public void testWriteInteger() throws Exception
  {
    System.out.println("writeInteger");
    int value1 = 0x7B;
    int value2 = 0x00;
    int value3 = -1;
    int value4 = 128;
    int value5 = -128;


    byte[] expecteds = CodingUtils.string2ByteArray("7B 00 81FF 820080 8180");//lezter Wert laut A-XDR Spec.: 82FF80
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    AXdrOutputStream instance = new AXdrOutputStream(out);

    instance.writeInteger(value1);
    instance.writeInteger(value2);
    instance.writeInteger(value3);
    instance.writeInteger(value4);
    instance.writeInteger(value5);

    byte[] result= out.toByteArray();


    assertArrayEquals(expecteds, result);
  }

  /**
   * Test of writeInteger8 method, of class AXdrOutputStream.
   */
  @Test
  public void testWriteInteger8() throws Exception
  {
    System.out.println("writeInteger8");
    int value1 = 0;
    int value2 = -85;
    int value3 = -70;
    int value4 = -1;

    byte[] expecteds = CodingUtils.string2ByteArray("00 AB BA FF");
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    AXdrOutputStream instance = new AXdrOutputStream(out);

    instance.writeInteger8(value1);
    instance.writeInteger8(value2);
    instance.writeInteger8(value3);
    instance.writeInteger8(value4);

    assertArrayEquals(expecteds, out.toByteArray());
  }

  /**
   * Test of writeInteger16 method, of class AXdrOutputStream.
   */
  @Test
  public void testWriteInteger16() throws Exception
  {
    System.out.println("writeInteger16");

    int value1 = 0;
    int value2 = 0xAB;
    int value3 = 0x7ABC;
    int value4 = 0xFFFFFABC;
    int value5 = 0xFFFFFFFF;

    byte[] expecteds = CodingUtils.string2ByteArray("0000 00AB 7ABC FABC FFFF");
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    AXdrOutputStream instance = new AXdrOutputStream(out);

    instance.writeInteger16(value1);
    instance.writeInteger16(value2);
    instance.writeInteger16(value3);
    instance.writeInteger16(value4);
    instance.writeInteger16(value5);

    assertArrayEquals(expecteds, out.toByteArray());
  }

  /**
   * Test of writeInteger32 method, of class AXdrOutputStream.
   */
  @Test
  public void testWriteInteger32() throws Exception
  {
    System.out.println("writeInteger32");

    int value1 = 0;
    int value2 = 0xAB;
    int value3 = 0xFAFFAABB;
    int value4 = 0xFFFFFFFF;

    byte[] expecteds = CodingUtils.string2ByteArray("00000000 000000AB FAFFAABB FFFFFFFF");
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    AXdrOutputStream instance = new AXdrOutputStream(out);

    instance.writeInteger32(value1);
    instance.writeInteger32(value2);
    instance.writeInteger32(value3);
    instance.writeInteger32(value4);

    assertArrayEquals(expecteds, out.toByteArray());
  }

  /**
   * Test of writeInteger64 method, of class AXdrOutputStream.
   */
  @Test
  public void testWriteInteger64() throws Exception
  {
    System.out.println("writeInteger64");
    long value1 = 0;
    long value2 = 0xABL;
    long value3 = 0xFAFFAABBL;
    long value4 = 0xFFFFFFFFL;
    long value5 = 0xABCDEF1234L;
    long value6 = 0xFFFFFFFFFFFFFFFFL;


    byte[] expecteds = CodingUtils.string2ByteArray("0000000000000000 00000000000000AB 00000000FAFFAABB 00000000FFFFFFFF 000000ABCDEF1234 FFFFFFFFFFFFFFFF");
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    AXdrOutputStream instance = new AXdrOutputStream(out);

    instance.writeInteger64(value1);
    instance.writeInteger64(value2);
    instance.writeInteger64(value3);
    instance.writeInteger64(value4);
    instance.writeInteger64(value5);
    instance.writeInteger64(value6);

    assertArrayEquals(expecteds, out.toByteArray());
  }

  /**
   * Test of writeOctetStringFixLength method, of class AXdrOutputStream.
   */
  @Test
  public void testWriteOctetStringFixLength() throws Exception
  {
    System.out.println("writeOctetStringFixLength");
    byte[] data =      CodingUtils.string2ByteArray("00 AB CC DE FF");
    byte[] expecteds = CodingUtils.string2ByteArray("00 AB CC DE FF");

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    AXdrOutputStream instance = new AXdrOutputStream(out);
    instance.writeOctetStringFixLength(data);

    assertArrayEquals(expecteds, out.toByteArray());
  }

  /**
   * Test of writeOctetStringVariableLength method, of class AXdrOutputStream.
   */
  @Test
  public void testWriteOctetStringVariableLength() throws Exception
  {
    System.out.println("writeOctetStringVariableLength");
    byte[] data =      CodingUtils.string2ByteArray("00 AB CC DE FF");
    byte[] expecteds = CodingUtils.string2ByteArray("05 00 AB CC DE FF");

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    AXdrOutputStream instance = new AXdrOutputStream(out);
    instance.writeOctetStringVariableLength(data);

    assertArrayEquals(expecteds, out.toByteArray());
  }

  /**
   * Test of writeUnsigned8 method, of class AXdrOutputStream.
   */
  @Test
  public void testWriteUnsigned8() throws Exception
  {
    int value1 = 0;
    int value2 = 0xAB;
    int value3 = 0xBA;
    int value4 = 0xFF;

    byte[] expecteds = CodingUtils.string2ByteArray("00 AB BA FF");
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    AXdrOutputStream instance = new AXdrOutputStream(out);

    instance.writeUnsigned8(value1);
    instance.writeUnsigned8(value2);
    instance.writeUnsigned8(value3);
    instance.writeUnsigned8(value4);

    assertArrayEquals(expecteds, out.toByteArray());
  }

  /**
   * Test of writeUnsigned16 method, of class AXdrOutputStream.
   */
  @Test
  public void testWriteUnsigned16() throws Exception
  {
    System.out.println("writeUnsigned16");

    int value1 = 0;
    int value2 = 0xAB;
    int value3 = 0x7ABC;
    int value4 = 0xFABC;
    int value5 = 0xFFFF;

    byte[] expecteds = CodingUtils.string2ByteArray("0000 00AB 7ABC FABC FFFF");
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    AXdrOutputStream instance = new AXdrOutputStream(out);

    instance.writeUnsigned16(value1);
    instance.writeUnsigned16(value2);
    instance.writeUnsigned16(value3);
    instance.writeUnsigned16(value4);
    instance.writeUnsigned16(value5);

    assertArrayEquals(expecteds, out.toByteArray());
  }

  /**
   * Test of writeUnsigned32 method, of class AXdrOutputStream.
   */
  @Test
  public void testWriteUnsigned32() throws Exception
  {
    System.out.println("writeUnsigned32");
    long value1 = 0;
    long value2 = 0xAB;
    long value3 = 0xFAFFAABBL;
    long value4 = 0xFFFFFFFFL;

    byte[] expecteds = CodingUtils.string2ByteArray("00000000 000000AB FAFFAABB FFFFFFFF");
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    AXdrOutputStream instance = new AXdrOutputStream(out);

    instance.writeUnsigned32(value1);
    instance.writeUnsigned32(value2);
    instance.writeUnsigned32(value3);
    instance.writeUnsigned32(value4);

    assertArrayEquals(expecteds, out.toByteArray());
  }

  /**
   * Test of writeVisibleString method, of class AXdrOutputStream.
   */
  @Test
  public void testWriteVisibleString() throws Exception
  {
//    SortedMap<String, Charset> availableCharsets = Charset.availableCharsets();
//    for (Charset cs: availableCharsets.values())
//    {
//          System.out.println(cs.toString());
//    }

    System.out.println("writeVisibleString");
    String value = "Hallo A-XDR";
    byte[] expecteds = new byte[value.length()+1];
    System.arraycopy( value.getBytes(), 0, expecteds, 1, value.length());
    expecteds[0]=(byte) value.length();

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    AXdrOutputStream instance = new AXdrOutputStream(out);

    instance.writeVisibleString(value);
    assertArrayEquals(expecteds, out.toByteArray());
  }

}
