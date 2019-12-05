/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elster.dlms.types.data;

import com.elster.coding.CodingUtils;
import com.elster.dlms.types.data.DlmsData.DataType;
import java.math.BigInteger;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author osse
 */
public class DlmsDataLong64UnsignedTest
{

  
   /**
   * Test of getMaxValue method, of class DlmsDataLong64Unsigned.
   */
  public void testLongConstructor()
  {
    DlmsDataLong64Unsigned instance =
            new DlmsDataLong64Unsigned(123);
    assertEquals("123",instance.getValue().toString());
  }


  /**
   * Test of getType method, of class DlmsDataLong64Unsigned.
   */
  @Test
  public void testGetType()
  {
    System.out.println("getType");
    DlmsDataLong64Unsigned instance = new DlmsDataLong64Unsigned(0);
    DataType expResult = DataType.LONG64_UNSIGNED;
    DataType result = instance.getType();
    assertEquals(expResult, result);
  }

  /**
   * Test of getMinValue method, of class DlmsDataLong64Unsigned.
   */
  @Test
  public void testGetMinValue()
  {
    System.out.println("getMinValue");
    DlmsDataLong64Unsigned instance = new DlmsDataLong64Unsigned(10);
    long expResult = 0;
    BigInteger result = instance.getMinValue();

    assertEquals(expResult, result.longValue());
  }

  /**
   * Test of getMaxValue method, of class DlmsDataLong64Unsigned.
   */
  @Test
  public void testGetMaxValue()
  {
    System.out.println("getMaxValue");
    DlmsDataLong64Unsigned instance = new DlmsDataLong64Unsigned(new BigInteger(1,CodingUtils.string2ByteArray("FFFFFFFFFFFFFFFF")));
    long expResult = 0xFFFFFFFFFFFFFFFFL;
    BigInteger result = instance.getMaxValue();
    assertEquals(expResult, result.longValue());
  }

  /**
   * Test of getMaxValue method, of class DlmsDataLong64Unsigned.
   */
  @Test
  public void testMinTest()
  {
    DlmsDataLong64Unsigned instance = new DlmsDataLong64Unsigned(BigInteger.ZERO);
    assertEquals(DlmsDataLong64Unsigned.MIN_VALUE, instance.getValue());
  }

  /**
   * Test of getMaxValue method, of class DlmsDataLong64Unsigned.
   */
  @Test
  public void testMaxTest()
  {
    DlmsDataLong64Unsigned instance = new DlmsDataLong64Unsigned(new BigInteger("18446744073709551615"));
    assertEquals(DlmsDataLong64Unsigned.MAX_VALUE, instance.getValue());
  }

  /**
   * Test of getMaxValue method, of class DlmsDataLong64Unsigned.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testMinFailTest()
  {
    DlmsDataLong64Unsigned instance =
            new DlmsDataLong64Unsigned(new BigInteger("-1"));
    instance.toString();
  }
  
    /**
   * Test of getMaxValue method, of class DlmsDataLong64Unsigned.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testMinFailTest2()
  {
    DlmsDataLong64Unsigned instance =
            new DlmsDataLong64Unsigned(-1);
    instance.toString();
  }

  /**
   * Test of getMaxValue method, of class DlmsDataLong64Unsigned.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testMaxFailTest()
  {
    DlmsDataLong64Unsigned instance =
            new DlmsDataLong64Unsigned(new BigInteger("18446744073709551616"));
    instance.toString();
  }

  /**
   * Test of toBytes method, of class DlmsDataLong64Unsigned.
   */
  @Test
  public void testToBytes()
  {
    System.out.println("toBytes");
    DlmsDataLong64Unsigned instance = new DlmsDataLong64Unsigned(0);
    byte[] expResult = CodingUtils.string2ByteArray("00 00 00 00 00 00 00 00");
    byte[] result = instance.toBytes();
    assertArrayEquals(expResult, result);
  }

  /**
   * Test of toBytes method, of class DlmsDataLong64Unsigned.
   */
  @Test
  public void testToBytes2()
  {
    System.out.println("toBytes 2");
    DlmsDataLong64Unsigned instance = new DlmsDataLong64Unsigned(0xFFFF);
    byte[] expResult = CodingUtils.string2ByteArray("00 00 00 00 00 00 FF FF");
    byte[] result = instance.toBytes();
    assertArrayEquals(expResult, result);
  }

  /**
   * Test of toBytes method, of class DlmsDataLong64Unsigned.
   */
  @Test
  public void testToBytes2b()
  {
    System.out.println("toBytes 2b");
    DlmsDataLong64Unsigned instance = new DlmsDataLong64Unsigned(0xAB00FFFFL);
    byte[] expResult = CodingUtils.string2ByteArray("00 00 00 00 AB 00 FF FF");
    byte[] result = instance.toBytes();
    assertArrayEquals(expResult, result);
  }

  /**
   * Test of toBytes method, of class DlmsDataLong64Unsigned.
   */
  @Test
  public void testToBytes3()
  {
    System.out.println("toBytes 3");
    DlmsDataLong64Unsigned instance = new DlmsDataLong64Unsigned(DlmsDataLong64Unsigned.MAX_VALUE);
    byte[] expResult = CodingUtils.string2ByteArray("FF FF FF FF FF FF FF FF");
    byte[] result = instance.toBytes();
    assertArrayEquals(expResult, result);
  }

  /**
   * Test of toBytes method, of class DlmsDataLong64Unsigned.
   */
  @Test
  public void testToBytes4()
  {
    System.out.println("toBytes 4");
    DlmsDataLong64Unsigned instance = new DlmsDataLong64Unsigned(new BigInteger(CodingUtils.string2ByteArray(
            "00 00 00 00 FF FF FF FF FF FF FF FF")));
    byte[] expResult = CodingUtils.string2ByteArray("FF FF FF FF FF FF FF FF");
    byte[] result = instance.toBytes();
    assertArrayEquals(expResult, result);
  }


}