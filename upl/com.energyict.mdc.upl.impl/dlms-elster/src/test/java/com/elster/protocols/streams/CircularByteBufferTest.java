/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elster.protocols.streams;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author osse
 */
public class CircularByteBufferTest
{
  public CircularByteBufferTest()
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
   * Test of pack method, of class CircularByteBuffer.
   */
  @Test
  public void testPack1()
  {
    int p = 0;
    int r = 0;

    System.out.println("pack");
    CircularByteBuffer instance = new CircularByteBuffer();
    instance.write((byte)p++);
    instance.write((byte)p++);
    instance.write((byte)p++);
    instance.write((byte)p++);
    assertEquals(r++, instance.read());
    assertEquals(r++, instance.read());

    for (int i = 0; i < instance.capacity() - 2; i++)
    {
      instance.write((byte)p++);
    }
    assertEquals(r++, instance.read());

    while (instance.size() > 0)
    {
      assertEquals(r++, instance.read());
    }
    instance.pack();

    assertEquals(0, instance.size());
  }

  /**
   * Test of pack method, of class CircularByteBuffer.
   */
  @Test
  public void testPack2()
  {
    int p = 0;
    int r = 0;

    System.out.println("pack");
    CircularByteBuffer instance = new CircularByteBuffer();
    instance.write((byte)p++);
    instance.write((byte)p++);
    instance.write((byte)p++);
    instance.write((byte)p++);
    assertEquals(r++, instance.read());
    assertEquals(r++, instance.read());
    instance.pack();
    for (int i = 0; i < instance.capacity() - 2; i++)
    {
      instance.write((byte)p++);
    }
    assertEquals(r++, instance.read());

    while (instance.size() > 0)
    {
      assertEquals(r++, instance.read());
    }
    instance.pack();

    assertEquals(0, instance.size());
  }

}
