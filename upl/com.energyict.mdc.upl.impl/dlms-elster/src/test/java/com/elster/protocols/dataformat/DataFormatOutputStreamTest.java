/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elster.protocols.dataformat;

import java.io.ByteArrayOutputStream;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import com.elster.coding.CodingUtils;
import static org.junit.Assert.*;

/**
 *
 * @author osse
 */
public class DataFormatOutputStreamTest
{
  public DataFormatOutputStreamTest()
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
   * Test of write method, of class DataFormatOutputStream.
   */
  @Test
  public void testWrite_int() throws Exception
  {
    System.out.println("write");

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

    DataFormatOutputStream instance = new DataFormatOutputStream(outputStream);

    instance.write(0x00);
    instance.write(0x01);
    instance.write(0x02);
    instance.write(0x50);
    instance.write(0x7E);
    instance.write(0x7F);


    byte[] result = outputStream.toByteArray();

    assertEquals(6, result.length);

    assertEquals(0x00, 0xFF & result[0]);
    assertEquals(0x81, 0xFF & result[1]);
    assertEquals(0x82, 0xFF & result[2]);
    assertEquals(0x50, 0xFF & result[3]);
    assertEquals(0x7E, 0xFF & result[4]);
    assertEquals(0xFF, 0xFF & result[5]);
  }

  /**
   * Test of write method, of class DataFormatOutputStream.
   */
  @Test
  public void testWrite_byteArr() throws Exception
  {
    System.out.println("write");
    byte[] b =  CodingUtils.string2ByteArray("41 42 43 44 45 46 47 48 49 4A 4B 4C 4D 4E 4F 50 61 62 63 64 65 66 67 68 69 6A 6B 6C 6D 6F 70");
    byte[] e =  CodingUtils.string2ByteArray("41 42 C3 44 C5 C6 47 48 C9 CA 4B CC 4D 4E CF 50 E1 E2 63 E4 65 66 E7 E8 69 6A EB 6C ED 6F F0");

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();



    DataFormatOutputStream instance =  new DataFormatOutputStream(outputStream);
    instance.write(b);

    assertArrayEquals(e, outputStream.toByteArray());
  }

  /**
   * Test of write method, of class DataFormatOutputStream.
   */
  @Test
  public void testWrite_3args() throws Exception
  {
    System.out.println("write");
    byte[] b =  CodingUtils.string2ByteArray("01 01 41 42 43 44 45 46 47 48 49 4A 4B 4C 4D 4E 4F 50 61 62 63 64 65 66 67 68 69 6A 6B 6C 6D 6F 70 01 01");
    byte[] e =  CodingUtils.string2ByteArray("41 42 C3 44 C5 C6 47 48 C9 CA 4B CC 4D 4E CF 50 E1 E2 63 E4 65 66 E7 E8 69 6A EB 6C ED 6F F0");

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

    int off = 2;
    int len = b.length-4;

    DataFormatOutputStream instance =  new DataFormatOutputStream(outputStream);
    instance.write(b, off, len);

    assertArrayEquals(e, outputStream.toByteArray());
  }

}
