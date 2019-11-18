/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elster.protocols.streams;

import java.util.Random;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Osse
 */
public class ConcurentCircularByteBufferSpeedTest
{
  
  private static final int TEST_SIZE= 10000;

  private static class FillInfo
  {
    int current=0;
    byte next()
    {
      current +=271 %256;
      return (byte) current;

    }
  }




  public ConcurentCircularByteBufferSpeedTest()
  {
  }

  private static void fillRandomly(byte[] array, FillInfo random)
  {
    for (int i = 0; i < array.length; i++)
    {
      array[i] = random.next();
    }

  }



 /**
   */
  @Test
  public void testSpeed2()
  {
    System.out.println("read 2");
    final ConcurrentCircularByteBuffer instance = new ConcurrentCircularByteBuffer();

    final FillInfo readRandom =new FillInfo();
    final FillInfo writeRandom =new FillInfo();

    Thread writerThread = new Thread(new Runnable()
    {
      public void run()
      {
        int c = 0;
        final Random sizeRandom = new Random(364664353L);


        while (c < TEST_SIZE)
        {

          byte[] someBytes = new byte[sizeRandom.nextInt(1024)];

          fillRandomly(someBytes, writeRandom);
          c += someBytes.length;
          instance.write(someBytes);
        }
      }

    });
    writerThread.start();


    int c = 0;

    long startTime = System.currentTimeMillis();
    //final Random sizeRandom = new Random(32313353L);

    while (c < TEST_SIZE-2000)
    {
      int size=instance.size();
      byte[] someBytes = new byte[size];
      byte[] expectedBytes = new byte[size];

      if (instance.size() >= size)
      {
        fillRandomly(expectedBytes, readRandom);
        instance.read(someBytes);
        c += someBytes.length;
        assertArrayEquals(expectedBytes, someBytes);
      }
    }

    long endTime = System.currentTimeMillis();
    System.out.println("duration: " + (endTime - startTime));
  }
  /**
   */
  @Test
  public void testSpeed()
  {
    System.out.println("read");
    final FastConcurrentCircularByteBuffer instance = new FastConcurrentCircularByteBuffer();

    final FillInfo readRandom =new FillInfo();
    final FillInfo writeRandom =new FillInfo();

    Thread writerThread = new Thread(new Runnable()
    {
      public void run()
      {
        int c = 0;
        final Random sizeRandom = new Random(364664353L);


        while (c < TEST_SIZE)
        {

          byte[] someBytes = new byte[sizeRandom.nextInt(1024)];

          fillRandomly(someBytes, writeRandom);
          c += someBytes.length;
          instance.write(someBytes);
        }
      }

    });
    writerThread.start();


    int c = 0;

    long startTime = System.currentTimeMillis();
    //final Random sizeRandom = new Random(32313353L);

    while (c < TEST_SIZE-2000)
    {
      int size=instance.size();
      byte[] someBytes = new byte[size];
      byte[] expectedBytes = new byte[size];

      if (instance.size() >= size)
      {
        fillRandomly(expectedBytes, readRandom);
        instance.read(someBytes);
        c += someBytes.length;
        assertArrayEquals(expectedBytes, someBytes);
      }
    }

    long endTime = System.currentTimeMillis();
    System.out.println("duration: " + (endTime - startTime));
  }



    /**
   */
  @Test
  public void testRepeat()  {
    testSpeed2();
    testSpeed();
    testSpeed2();
    testSpeed();
    testSpeed2();
    testSpeed();
  }

}
