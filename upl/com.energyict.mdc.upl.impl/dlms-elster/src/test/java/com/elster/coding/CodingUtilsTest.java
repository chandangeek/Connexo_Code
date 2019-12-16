/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.elster.coding;

import java.io.IOException;
import java.io.InputStream;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author osse
 */
public class CodingUtilsTest {

    public CodingUtilsTest() {
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
   * Test of string2ByteArray method, of class CodingUtils.
   */
  @Test
  public void testString2ByteArray()
  {
    System.out.println("string2ByteArray");
    String hexString = "AB CD  EF11";
    byte[] expResult = new byte[] {(byte)0xAB,(byte) 0xCD,(byte) 0xEF,(byte) 0x11};
    byte[] result = CodingUtils.string2ByteArray(hexString);
    assertArrayEquals(expResult, result);
  }

  /**
   * Test of string2InputStream method, of class CodingUtils.
   */
  @Test
  public void testString2InputStream() throws IOException
  {
    System.out.println("string2InputStream");
    String hexString = "AB CD  EF11";
    InputStream result = CodingUtils.string2InputStream(hexString);
    assertEquals(0xAB, result.read());
    assertEquals(0xCD, result.read());
    assertEquals(0xEF, result.read());
    assertEquals(0x11, result.read());
    assertEquals(-1, result.read());
  }
  
  
  
  /**
   * Test of string2ByteArray method, of class CodingUtils.
   */
  @Test
  public void testIsCompleteHexString1()
  {
    System.out.println("isCompleteHexString 1");
    String hexString = " 0A AB CD ";
    boolean expResult = true;
    boolean result = CodingUtils.isCompleteHexString(hexString);
    assertEquals(expResult, result);
  }
  
    /**
   * Test of isCompleteHexString method, of class CodingUtils.
   */
  @Test
  public void testIsCompleteHexString2()
  {
    System.out.println("isCompleteHexString 2");
    String hexString = " 0A AB CDE ";
    boolean expResult = false;
    boolean result = CodingUtils.isCompleteHexString(hexString);
    assertEquals(expResult, result);
  }
  
   /**
   * Test of isCompleteHexString method, of class CodingUtils.
   */
  @Test
  public void testIsCompleteHexString3()
  {
    System.out.println("isCompleteHexString 3");
    String hexString = "1234";
    boolean expResult = true;
    boolean result = CodingUtils.isCompleteHexString(hexString);
    assertEquals(expResult, result);
  }
  
     /**
   * Test of isCompleteHexString method, of class CodingUtils.
   */
  @Test
  public void testIsCompleteHexString4()
  {
    System.out.println("isCompleteHexString 4");
    String hexString = "0F";
    boolean expResult = true;
    boolean result = CodingUtils.isCompleteHexString(hexString);
    assertEquals(expResult, result);
  }
  

  /**
   * Test of byteArrayToString method, of class CodingUtils.
   */
  @Test
  public void testByteArrayToString_byteArr()
  {
    System.out.println("byteArrayToString");
    byte[] data = new byte[] {(byte)0xAB,(byte)0x00,(byte)0xFF};
    String expResult = "AB 00 FF";
    String result = CodingUtils.byteArrayToString(data);
    assertEquals(expResult, result);
  }

  /**
   * Test of byteArrayToString method, of class CodingUtils.
   */
  @Test
  public void testByteArrayToString_byteArr_String()
  {
    System.out.println("byteArrayToString");
    byte[] data = new byte[] {(byte)0xAB,(byte)0x00,(byte)0xFF};
    String separator = "!-";
    String expResult = "AB!-00!-FF";
    String result = CodingUtils.byteArrayToString(data, separator);
    assertEquals(expResult, result);

    separator = "";
    expResult = "AB00FF";
    result = CodingUtils.byteArrayToString(data, separator);
    assertEquals(expResult, result);

  }

  /**
   * Test of intToHex method, of class CodingUtils.
   */
  @Test
  public void testIntToHex_int_int()
  {
    System.out.println("intToHex");
    int value = 0;
    int minSize = 1;
    String expResult = "0";
    String result = CodingUtils.intToHex(value, minSize);
    assertEquals(expResult, result);


    value = 0xABCDE;
    minSize = 1;
    expResult = "ABCDE";
    result = CodingUtils.intToHex(value, minSize);
    assertEquals(expResult, result);

    value = 0xABCDE;
    minSize = 10;
    expResult = "00000ABCDE";
    result = CodingUtils.intToHex(value, minSize);
    assertEquals(expResult, result);

    value = -1;
    minSize = 10;
    expResult = "00FFFFFFFF";
    result = CodingUtils.intToHex(value, minSize);
    assertEquals(expResult, result);

  }

  /**
   * Test of intToHex method, of class CodingUtils.
   */
  @Test
  public void testIntToHex_int()
  {
    System.out.println("intToHex");
    int value = 0;
    String expResult = "00";
    String result = CodingUtils.intToHex(value);
    assertEquals(expResult, result);

    value = 0x0F;
    expResult = "0F";
    result = CodingUtils.intToHex(value);
    assertEquals(expResult, result);
    
    value = 0xABCD;
    expResult = "ABCD";
    result = CodingUtils.intToHex(value);
    assertEquals(expResult, result);

    value = 0xABCDE;
    expResult = "0ABCDE";
    result = CodingUtils.intToHex(value);
    assertEquals(expResult, result);
    
    value = -1;
    expResult = "FFFFFFFF";
    result = CodingUtils.intToHex(value);
    assertEquals(expResult, result);
  }
  
  /**
   * Test of longToHex method, of class CodingUtils.
   */
  @Test
  public void testLongToHex_long_int()
  {
    System.out.println("longToHex");
    long value = 0;
    int minSize = 1;
    String expResult = "0";
    String result = CodingUtils.longToHex(value, minSize);
    assertEquals(expResult, result);


    value = 0xABCDE;
    minSize = 1;
    expResult = "ABCDE";
    result = CodingUtils.longToHex(value, minSize);
    assertEquals(expResult, result);

    value = 0xABCDE;
    minSize = 10;
    expResult = "00000ABCDE";
    result = CodingUtils.longToHex(value, minSize);
    assertEquals(expResult, result);

    value = -1;
    minSize = 18;
    expResult = "00FFFFFFFFFFFFFFFF";
    result = CodingUtils.longToHex(value, minSize);
    assertEquals(expResult, result);

  }

  /**
   * Test of longToHex method, of class CodingUtils.
   */
  @Test
  public void testLongToHex_long()
  {
    System.out.println("longToHex");
    long value = 0;
    String expResult = "00";
    String result = CodingUtils.longToHex(value);
    assertEquals(expResult, result);

    value = 0x0F;
    expResult = "0F";
    result = CodingUtils.longToHex(value);
    assertEquals(expResult, result);
    
    value = 0xABCD;
    expResult = "ABCD";
    result = CodingUtils.longToHex(value);
    assertEquals(expResult, result);

    value = 0xABCDE;
    expResult = "0ABCDE";
    result = CodingUtils.longToHex(value);
    assertEquals(expResult, result);
    
    value = -1;
    expResult = "FFFFFFFFFFFFFFFF";
    result = CodingUtils.longToHex(value);
    assertEquals(expResult, result);
  }

}