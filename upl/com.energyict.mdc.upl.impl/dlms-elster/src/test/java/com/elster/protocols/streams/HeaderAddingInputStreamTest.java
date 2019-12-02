/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elster.protocols.streams;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author osse
 */
public class HeaderAddingInputStreamTest
{
  static final byte[] testHeader =
  {
    (byte)0xFF, (byte)0x11, (byte)0x12
  };
  static final byte[] testData =
  {
    (byte)0xF3, (byte)0x14, (byte)0xF5, (byte)0x16
  };
  static final byte[] testDataComb =
  {
     (byte)0xFF, (byte)0x11, (byte)0x12,(byte)0xF3, (byte)0x14, (byte)0xF5, (byte)0x16
  };

  public HeaderAddingInputStreamTest()
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
   * Test of available method, of class HeaderAddingInputStream.
   */
  @Test
  public void testAvailable() throws Exception
  {
    System.out.println("available");

    InputStream dataInputStream = new ByteArrayInputStream(testData);

    HeaderAddingInputStream instance = new HeaderAddingInputStream(dataInputStream, testHeader);
    int expResult = testData.length + testHeader.length;
    int result;

    while (expResult >= 0)
    {
      result = instance.available();
      assertEquals(expResult, result);
      instance.read();
      expResult--;
    }

  }

  /**
   * Test of read method, of class HeaderAddingInputStream.
   */
  @Test
  public void testRead_0args() throws Exception
  {
    System.out.println("read");


    InputStream dataInputStream = new ByteArrayInputStream(testData);
    HeaderAddingInputStream instance = new HeaderAddingInputStream(dataInputStream, testHeader);


    for (int i = 0; i < testDataComb.length; i++)
    {
      int result = instance.read();
      int expResult = 0xFF & testDataComb[i];
      assertEquals(expResult, result);
    }

    int result = instance.read();
    int expResult = -1;
    assertEquals(expResult, result);
  }

  /**
   * Test of read method, of class HeaderAddingInputStream. <P>
   * Read all.
   */
  @Test
  public void testRead_3args() throws Exception
  {
    System.out.println("read");
    byte[] b = new byte[testDataComb.length];
    InputStream dataInputStream = new ByteArrayInputStream(testData);
    HeaderAddingInputStream instance = new HeaderAddingInputStream(dataInputStream, testHeader);
    int expResult = testDataComb.length;
    int result = instance.read(b, 0, testDataComb.length);

    assertEquals(expResult, result);
    assertArrayEquals(testDataComb, b);


    result = instance.read(b, 0, testDataComb.length);
    assertEquals(-1, result);
  }

  /**
   * Test of read method, of class HeaderAddingInputStream.<P>
   * Read blocks of 2.
   */
  @Test
  public void testRead_3args_2() throws Exception
  {
    System.out.println("read");
    byte[] b = new byte[testDataComb.length];
    InputStream dataInputStream = new ByteArrayInputStream(testData);
    HeaderAddingInputStream instance = new HeaderAddingInputStream(dataInputStream, testHeader);


    for (int i = 0; i < testDataComb.length; i += 2)
    {
      int expResult = Math.min(2, testDataComb.length - i);
      int result = instance.read(b, i, expResult);
      assertEquals(expResult, result);
    }

    assertArrayEquals(testDataComb, b);
    assertEquals(-1, instance.read(b, 0, testDataComb.length)); //EOF
  }

  /**
   * Test of skip method, of class HeaderAddingInputStream.<P>
   * skip 0->n
   */
  @Test
  public void testSkip() throws Exception
  {
    System.out.println("skip");

    for (long i = 0L; i < testDataComb.length; i++)
    {
      InputStream dataInputStream = new ByteArrayInputStream(testData);
      HeaderAddingInputStream instance = new HeaderAddingInputStream(dataInputStream, testHeader);
      long n = i;
      long expResult = i;
      long result = instance.skip(n);

      assertEquals(expResult, result);

      assertEquals(testDataComb[(int)i], (byte)instance.read());
    }
  }

  /**
   * Test of skip method, of class HeaderAddingInputStream. <P>
   * skip 2, read 1 ...
   */
  @Test
  public void testSkip_2() throws Exception
  {
    System.out.println("skip");

    InputStream dataInputStream = new ByteArrayInputStream(testData);
    HeaderAddingInputStream instance = new HeaderAddingInputStream(dataInputStream, testHeader);

    for (int i = 0; i < testDataComb.length;)
    {
      int expResult = Math.min(2, testDataComb.length - i);
      long result = instance.skip(2);

      assertEquals(expResult, result);

      i+=2;

      if (i<testDataComb.length)
      {
        assertEquals(testDataComb[(int)i], (byte)instance.read());
        i++;
      }
      else
      {
        assertEquals(-1, (byte)instance.read());
        i++;
      }
    }
  }

}
