/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elster.protocols.streams;

import com.elster.coding.CodingUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author osse
 */
public class IntervallInputStreamPipeTest
{
  public IntervallInputStreamPipeTest()
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
   * Test of read method, of class IntervalInputStreamPipe.
   */
  @Test
  public void testRead_3args() throws Exception
  {
    System.out.println("read");
    byte[] b = new byte[100];
    int off = 0;
    int len = 100;
    IntervalInputStreamPipe instance = new IntervalInputStreamPipe(25);
    try
    {
      int expResult = 0;
      int result = instance.read(b, off, len);
      assertEquals(expResult, result);
    }
    finally
    {
      instance.close();
    }
  }

  /**
   * Test of read method, of class IntervalInputStreamPipe.
   */
  @Test
  public void testRead_3args_b() throws Exception
  {
    System.out.println("read");
    byte[] b = new byte[100];
    int off = 0;
    int len = 100;
    IntervalInputStreamPipe instance = new IntervalInputStreamPipe(500);
    try
    {
      int expResult = 0;

      long startTime = System.currentTimeMillis();
      int result = instance.read(b, off, len);
      long endTime = System.currentTimeMillis();
      long duration = endTime - startTime;

      assertEquals(expResult, result);
      assertTrue("Method returned to fast", duration > 499);
      assertTrue(
              "Method returned to slow (this test can fail, if the executing machine is much slower than expected)",
              duration < 1000);
    }
    finally
    {
      instance.close();
    }

  }

  /**
   * Test of read method, of class IntervalInputStreamPipe.
   */
  @Test
  public void testRead_3args_c() throws Exception
  {
    System.out.println("read");

    byte[] send = CodingUtils.string2ByteArray("AA BB CC DD EE FF 00 11 22 33 44 55 66 77 88 99");
    byte[] b = new byte[send.length + 10];
    int off = 0;
    int len = send.length + 10;
    IntervalInputStreamPipe instance = new IntervalInputStreamPipe(50);
    try
    {

      instance.put(send);
      int result = instance.read(b, off, len);
      assertEquals(send.length, result);
      assertArrayEquals(send, CodingUtils.copyOfRange(b, 0, result));
    }
    finally
    {
      instance.close();
    }

  }

}
