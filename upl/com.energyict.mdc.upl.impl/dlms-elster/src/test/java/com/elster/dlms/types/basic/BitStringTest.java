/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elster.dlms.types.basic;

import java.util.Random;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author osse
 */
public class BitStringTest
{
  public BitStringTest()
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
   * Test of getBitCount method, of class BitString.
   */
  @Test
  public void testGetBitCount()
  {
    System.out.println("getBitCount");
    BitString instance = new BitString(12, new byte[]
            {
              (byte)0x01, (byte)0xBB
            });
    int expResult = 12;
    int result = instance.getBitCount();
    assertEquals(expResult, result);
  }

  /**
   * Test of getData method, of class BitString.
   */
  @Test
  public void testGetData()
  {
    System.out.println("getData");
    BitString instance = new BitString(12, new byte[]
            {
              (byte)0x01, (byte)0xBB
            });
    byte[] expResult = new byte[]
    {
      (byte)0x01, (byte)0xBB
    };
    byte[] result = instance.getData();
    assertArrayEquals(expResult, result);
  }

  /**
   * Test of isBitSet method, of class BitString.
   */
  @Test
  public void testIsBitSet()
  {
    System.out.println("isBitSet");
    BitString instance = new BitString(12, new byte[]
            {
              (byte)0xAB, (byte)0x80
            });

    assertEquals(true, instance.isBitSet(0));
    assertEquals(false, instance.isBitSet(1));
    assertEquals(true, instance.isBitSet(2));
    assertEquals(false, instance.isBitSet(3));

    assertEquals(true, instance.isBitSet(4));
    assertEquals(false, instance.isBitSet(5));
    assertEquals(true, instance.isBitSet(6));
    assertEquals(true, instance.isBitSet(7));

    assertEquals(true, instance.isBitSet(8));
    assertEquals(false, instance.isBitSet(9));
    assertEquals(false, instance.isBitSet(10));
    assertEquals(false, instance.isBitSet(11));

    //1010 1011 1000 0000
    //0123 4567 8901

  }

  /**
   * Test of getPopulationCount method, of class BitString.
   */
  @Test
  public void testGetPopulationCount()
  {
    System.out.println("getPopulationCount");
    BitStringBuilder bb = new BitStringBuilder(100);

    bb.setBit(0, true);
    bb.setBit(1, true);
    bb.setBit(2, true);
    bb.setBit(3, true);
    bb.setBit(4, true);
    bb.setBit(5, true);
    bb.setBit(6, true);
    bb.setBit(7, true);
    bb.setBit(15, true);
    bb.setBit(99, true);

    BitString instance = bb.toBitString();

    int expResult = 10;
    int result = instance.getPopulationCount();
    assertEquals(expResult, result);
  }
  
    /**
   * Test of getActiveBits method, of class BitString.
   */
  @Test
  public void testGetActiveBits()
  {
    System.out.println("getActiveBits");
    BitStringBuilder bb = new BitStringBuilder(99);

    bb.setBit(0, true);
    bb.setBit(1, true);
    bb.setBit(2, true);
    bb.setBit(3, true);
    bb.setBit(4, true);
    bb.setBit(5, true);
    bb.setBit(6, true);
    bb.setBit(7, true);
    bb.setBit(15, true);
    bb.setBit(98, true);

    BitString instance = bb.toBitString();

    int[] expResult = new int[]{0,1,2,3,4,5,6,7,15,98};
    int[] result= instance.getActiveBits();
    assertArrayEquals(expResult, result);
  }  

   /**
   * Test of getActiveBits method, of class BitString (using random Bitstrings)
   */  
  @Test
  public void testGetActiveBits_Random()
  {
    final long seed= System.nanoTime();
    System.out.println("getActiveBits (Random test. Seed:"+seed+")");    
    final Random rand = new Random(seed);

    for (int r = 0; r < 10000; r++)
    {
      final byte[] bytes = new byte[rand.nextInt(100)];
      rand.nextBytes(bytes);
      int bitCount;

      if (bytes.length > 0) //bitCount im erlaubten Bereich variieren.
      {
        bitCount = ((bytes.length - 1) * 8) + 1 + rand.nextInt(7);
      }
      else
      {
        bitCount = 0;
      }

      final BitString instance = new BitString(bitCount, bytes);
      assertArrayEquals(simpleGetActiveBits(instance), instance.getActiveBits());
    }
  }
  
  


  /**
   * Simple version of getActiveBits without optimizations 
   * 
   * @param bitString
   * @return 
   */
  private int[] simpleGetActiveBits(final BitString bitString)
  {

    final int populationCount = bitString.getPopulationCount();
    final int[] result = new int[populationCount];
    final int bitCount = bitString.getBitCount();
    int pos = 0;
    for (int i = 0; i < bitCount; i++)
    {
      if (bitString.isBitSet(i))
      {
        result[pos] = i;
        pos++;
      }
    }
    return result;
  }

}
