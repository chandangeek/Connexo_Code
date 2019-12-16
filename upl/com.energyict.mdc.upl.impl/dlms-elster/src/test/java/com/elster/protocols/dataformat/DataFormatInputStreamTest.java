/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elster.protocols.dataformat;

import java.io.InputStream;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import com.elster.coding.CodingUtils;
import static org.junit.Assert.*;

/**
 *
 * @author osse
 */
public class DataFormatInputStreamTest
{
  public DataFormatInputStreamTest()
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
   * Test of read method, of class DataFormatInputStream.
   */
  @Test
  public void testRead() throws Exception
  {
    System.out.println("read");

    InputStream inputStream = CodingUtils.string2InputStream(
            "41 42 C3 44 C5 C6 47 48 C9 CA 4B CC 4D 4E CF 50 E1 E2 63 E4 65 66 E7 E8 69 6A EB 6C ED 6F F0");

    DataFormatInputStream instance = new DataFormatInputStream(inputStream);
    try
    {

      byte[] expResult = CodingUtils.string2ByteArray(
              "41 42 43 44 45 46 47 48 49 4A 4B 4C 4D 4E 4F 50 61 62 63 64 65 66 67 68 69 6A 6B 6C 6D 6F 70");

      assertEquals(expResult.length, instance.available());

      for (int i = 0; i < expResult.length; i++)
      {
        int result = instance.read();
        assertEquals(expResult[i], result);
        assertEquals(expResult.length - i - 1, instance.available());
      }
    }
    finally
    {
      instance.close();
    }
  }

  /**
   * Test of read method, of class DataFormatInputStream.
   */
  @Test(expected = DataFormatIOException.class)
  public void testRead_exception() throws Exception
  {
    System.out.println("read");

    InputStream inputStream = CodingUtils.string2InputStream(
            "41 42 C3 44 C5 C6 47 48 01 CA 4B CC 4D 4E CF 50 E1 E2 63 E4 65 66 E7 E8 69 6A EB 6C ED 6F F0");

    DataFormatInputStream instance = new DataFormatInputStream(inputStream);
    try
    {

      byte[] expResult = CodingUtils.string2ByteArray(
              "41 42 43 44 45 46 47 48 49 4A 4B 4C 4D 4E 4F 50 61 62 63 64 65 66 67 68 69 6A 6B 6C 6D 6F 70");

      assertEquals(expResult.length, instance.available());

      for (int i = 0; i < expResult.length; i++)
      {
        int result = instance.read();
        assertEquals(expResult[i], result);
        assertEquals(expResult.length - i - 1, instance.available());
      }
    }
    finally
    {
      instance.close();
    }
  }

  /**
   * Test of read method, of class DataFormatInputStream.
   */
  @Test
  public void testRead__3args() throws Exception
  {
    System.out.println("read");

    InputStream inputStream = CodingUtils.string2InputStream(
            "41 42 C3 44 C5 C6 47 48 C9 CA 4B CC 4D 4E CF 50 E1 E2 63 E4 65 66 E7 E8 69 6A EB 6C ED 6F F0");

    DataFormatInputStream instance = new DataFormatInputStream(inputStream);
    try
    {

      byte[] expResult = CodingUtils.string2ByteArray(
              "41 42 43 44 45 46 47 48 49 4A 4B 4C 4D 4E 4F 50 61 62 63 64 65 66 67 68 69 6A 6B 6C 6D 6F 70");

      byte[] result = new byte[expResult.length + 200];
      byte[] expectedResultUntouched = new byte[expResult.length + 200];

      for (int i = 0; i < result.length; i++)
      {
        result[i] = (byte)((i % 76) + 11);
        expectedResultUntouched[i] = result[i];
      }


      int count = instance.read(result, 24, expResult.length);
      assertEquals(expResult.length, count);

      for (int i = 0; i < 24; i++)
      {
        assertEquals(expectedResultUntouched[i], result[i]);
      }

      for (int i = 24; i < 24 + expResult.length; i++)
      {
        assertEquals(expResult[i - 24], result[i]);
      }

      for (int i = 24 + expResult.length; i < expectedResultUntouched.length; i++)
      {
        assertEquals(expectedResultUntouched[i], result[i]);
      }
    }
    finally
    {
      instance.close();
    }
  }

  /**
   * Test of read method, of class DataFormatInputStream.
   */
  @Test(expected = DataFormatIOException.class)
  public void testRead_3args_Exception() throws Exception
  {
    System.out.println("read");

    InputStream inputStream = CodingUtils.string2InputStream(
            "41 42 C3 44 C5 C6 47 48 C9 CA 4B CC 4D 4E CF 50 E1 E2 63 E4 65 66 E7 E8 69 6A 3B 6C ED 6F F0");

    DataFormatInputStream instance = new DataFormatInputStream(inputStream);
    try
    {

      byte[] expResult = CodingUtils.string2ByteArray(
              "41 42 43 44 45 46 47 48 49 4A 4B 4C 4D 4E 4F 50 61 62 63 64 65 66 67 68 69 6A 6B 6C 6D 6F 70");

      byte[] result = new byte[expResult.length + 200];
      byte[] expectedResultUntouched = new byte[expResult.length + 200];

      for (int i = 0; i < result.length; i++)
      {
        result[i] = (byte)((i % 76) + 11);
        expectedResultUntouched[i] = result[i];
      }


      int count = instance.read(result, 24, expResult.length);
      assertEquals(24, count);


      for (int i = 0; i < 24; i++)
      {
        assertEquals(expectedResultUntouched[i], result[i]);
      }

      for (int i = 24; i < 24 + expResult.length; i++)
      {
        assertEquals(expResult[i - 24], result[i]);
      }

      for (int i = 24 + expResult.length; i < expectedResultUntouched.length; i++)
      {
        assertEquals(expectedResultUntouched[i], result[i]);
      }
    }
    finally
    {
      instance.close();
    }
  }

}