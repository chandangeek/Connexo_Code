/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elster.protocols.errortest;

import java.io.ByteArrayOutputStream;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author osse
 */
public class ErrorTestDelayOutputStreamTest
{
  public ErrorTestDelayOutputStreamTest()
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
   * Test of write method, of class ErrorTestDelayOutputStream.
   */
  @Test(timeout=3000)
  public void testWrite_int() throws Exception
  {
    System.out.println("write");
    final ByteArrayOutputStream byteArrayOutputStream= new ByteArrayOutputStream();
    final ErrorTestDelayOutputStream instance = new ErrorTestDelayOutputStream(byteArrayOutputStream, 200);
    instance.write(0);
    instance.write(255);
    assertTrue("Test may fail in very slow enviroments",byteArrayOutputStream.size()<2);
    instance.close();
    assertEquals(2,byteArrayOutputStream.size());
    
    final byte[] expected= new byte[]{(byte) 0x00,(byte) 0xFF};
    final byte[] toByteArray = byteArrayOutputStream.toByteArray();

    assertArrayEquals(expected, toByteArray);
    
  }

//  /**
//   * Test of write method, of class ErrorTestDelayOutputStream.
//   */
//  @Test
//  public void testWrite_3args() throws Exception
//  {
//    System.out.println("write");
//    byte[] b = null;
//    int off = 0;
//    int len = 0;
//    ErrorTestDelayOutputStream instance = null;
//    instance.write(b, off, len);
//    // TODO review the generated test code and remove the default call to fail.
//    fail("The test case is a prototype.");
//  }
//
//  /**
//   * Test of write method, of class ErrorTestDelayOutputStream.
//   */
//  @Test
//  public void testWrite_byteArr() throws Exception
//  {
//    System.out.println("write");
//    byte[] b = null;
//    ErrorTestDelayOutputStream instance = null;
//    instance.write(b);
//    // TODO review the generated test code and remove the default call to fail.
//    fail("The test case is a prototype.");
//  }
//
//  /**
//   * Test of close method, of class ErrorTestDelayOutputStream.
//   */
//  @Test
//  public void testClose() throws Exception
//  {
//    System.out.println("close");
//    ErrorTestDelayOutputStream instance = null;
//    instance.close();
//    // TODO review the generated test code and remove the default call to fail.
//    fail("The test case is a prototype.");
//  }
  
}
