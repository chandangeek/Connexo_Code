/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elster.protocols.streams;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author osse
 */
public class TimeoutableInputStreamPipeTest
{
  public TimeoutableInputStreamPipeTest()
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
   * Test of readTO method, of class TimeoutableInputStreamPipe. <P>
   * Test if an TimeoutIOException will be thrown after the timeout. <br>
   * Test of normal operation after the exception.
   *
   */
  @Test
  public void testRead_int() throws IOException
  {

    System.out.println("read");

    int timeoutMillis = 1000;
    TimeoutableInputStreamPipe instance = new TimeoutableInputStreamPipe();
    try
    {

      boolean exceptionCaught = false;
      long start = System.currentTimeMillis();
      int result = -100;

      //--- Read without put must be lead to an timeout exception ---
      try
      {
        result = instance.readTO(timeoutMillis);
      }
      catch (TimeoutIOException ex)
      {
        exceptionCaught = true;
      }
      long end = System.currentTimeMillis();

      assertTrue("method returned to fast", end - start > (timeoutMillis / 10 * 8));
      assertEquals("An timeout exception must be thrown (after the timeout", true, exceptionCaught);


      //--- The TimeoutableInputStreamPipe must be working normal after an timeout occured ---
      instance.put(new byte[]
              {
                42
              });
      instance.flush();

      start = System.currentTimeMillis();
      result = instance.readTO(timeoutMillis);
      end = System.currentTimeMillis();

      assertTrue("method returned to slow", end - start < (timeoutMillis / 2));
      assertEquals(42, result);
    }
    finally
    {
      instance.close();
    }
  }

  /**
   * Test of readTO method, of class TimeoutableInputStreamPipe.<P>
   * Writing a big amount of data in the buffer.<br>
   * Reading and checking the data.
   *
   */
  @Test
  public void testRead_int_1() throws IOException
  {
    System.out.println("read");

//    PipedInputStream pipedInputStream= new PipedInputStream();
//    PipedOutputStream pipedOutputStream= new PipedOutputStream(pipedInputStream);


    //int timeoutMillis = 30000;
    final TimeoutableInputStreamPipe instance = new TimeoutableInputStreamPipe();
    try
    {
//    pipedOutputStream.write(42);
      for (int i = 0; i < 100; i++)
      {
        byte data[] = new byte[256];
        for (int j = 0; j < 256; j++)
        {
          data[j] = (byte)((i + j) % 256);
        }
        instance.put(data);
      }

      instance.flush();

//    long start = System.currentTimeMillis();


      for (int i = 0; i < 100; i++)
      {
        for (int j = 0; j < 256; j++)
        {
          assertEquals("i:" + i + ", j:" + j, ((i + j) % 256), instance.readTO(1000));
        }
      }

    }
    finally
    {
      instance.close();
    }

//    long end = System.currentTimeMillis();


//    assertTrue("method returned to slow", end - start < (timeoutMillis / 2));
  }

  /**
   * Test of readTO method, of class TimeoutableInputStreamPipe.<P>
   * Writing a big amount of data in the buffer by an <b>separate thread</b>.<br>
   * Concurrent reading and checking of the data.
   *
   */
  @Test
  public void testRead_int_2() throws IOException
  {
    System.out.println("read");

//    PipedInputStream pipedInputStream= new PipedInputStream();
//    PipedOutputStream pipedOutputStream= new PipedOutputStream(pipedInputStream);


    //int timeoutMillis = 30000;
    final TimeoutableInputStreamPipe instance = new TimeoutableInputStreamPipe();

//    pipedOutputStream.write(42);

    Thread putter = new Thread(new Runnable()
    {
      //@Override
      public void run()
      {
        for (int i = 0; i < 100; i++)
        {
          try
          {
            Thread.sleep(20);
          }
          catch (InterruptedException ex)
          {
            break;
          }
          byte data[] = new byte[256];
          for (int j = 0; j < 256; j++)
          {
            data[j] = (byte)((i + j) % 256);
          }
          instance.put(data);
          instance.flush();
        }
      }

    });

    //long start = System.currentTimeMillis();

    putter.start();

    for (int i = 0; i < 100; i++)
    {
      for (int j = 0; j < 256; j++)
      {
        int result = instance.readTO(1000);
        assertEquals("i:" + i + ", j:" + j, ((i + j) % 256), result);
      }
    }



    //long end = System.currentTimeMillis();


//    assertTrue("method returned to slow", end - start < (timeoutMillis / 2));
  }

  /**
   * Test of readTO method, of class TimeoutableInputStreamPipe.<P>
   * Several times: Put "big" array - Read part of array byte by byte<br>
   * This tests correct growing of the buffer without data loss.
   */
  @Test
  public void testRead_int_3() throws IOException
  {
    System.out.println("read");


    final TimeoutableInputStreamPipe instance = new TimeoutableInputStreamPipe();
    try
    {

      byte data[] = new byte[211];
      for (int j = 0; j < 211; j++)
      {
        data[j] = (byte)(j + 1);
      }

      int expected = 0;

      for (int i = 0; i < 256; i++)
      {
        instance.put(data);
        for (int j = 0; j < 201; j++)
        {
          int result = instance.readTO(1000);
          assertEquals(expected + 1, result);
          expected = (expected + 1) % 211;
        }
      }
    }
    finally
    {
      instance.close();
    }

//    assertTrue("method returned to slow", end - start < (timeoutMillis / 2));
  }

  /**
   * Test of readTO method, of class TimeoutableInputStreamPipe.<P>
   *
   * Several times: Put "big" array - Read part of array byte by byte<br>
   * This tests correct growing of the buffer without data loss.
   * <P>
   * Skip the rest of data.
   * <P>
   * Wait and write one byte in background.<br>
   * Check if readTO waits for this byte.
   *
   */
  @Test
  public void testRead_0args() throws Exception
  {
    System.out.println("read");
    final TimeoutableInputStreamPipe instance = new TimeoutableInputStreamPipe();
    try
    {

      byte data[] = new byte[211];
      for (int j = 0; j < 211; j++)
      {
        data[j] = (byte)(j + 1);
      }

      int expected = 0;

      for (int i = 0; i < 256; i++)
      {
        instance.put(data);
        for (int j = 0; j < 201; j++)
        {
          int result = instance.read();
          assertEquals(expected + 1, result);
          expected = (expected + 1) % 211;
        }
      }

      instance.skip(instance.available());

      //--- check if readTO blocks ---
      Thread putter = new Thread(new Runnable()
      {
        //@Override
        public void run()
        {
          try
          {
            Thread.sleep(2000);
            instance.put((byte)250);
            instance.flush();
          }
          catch (InterruptedException ex)
          {
            Logger.getLogger(TimeoutableInputStreamPipeTest.class.getName()).log(Level.SEVERE, null, ex);
          }
        }

      });

      putter.start();

      int result = instance.read();
      assertEquals(250, result);
    }
    finally
    {
      instance.close();
    }
  }

  /**
   * Test of readTO method, of class TimeoutableInputStreamPipe.
   */
  @Test
  public void testRead_3args() throws Exception
  {
    System.out.println("read 3args");
    byte[] b = null;
    //int off = 0;
    //int len = 0;
    TimeoutableInputStreamPipe instance = new TimeoutableInputStreamPipe();
    try
    {
      byte data[] = new byte[211];
      for (int j = 0; j < 211; j++)
      {
        data[j] = (byte)(j + 1);
      }

      int expected = 0;

      for (int i = 0; i < 256; i++)
      {
        instance.put(data);

        byte[] readBuffer = new byte[1000];

        int byteCount = instance.read(readBuffer, 99, 201);
        assertEquals(201, byteCount);

        for (int j = 0; j < 99; j++)
        {
          assertEquals(0, readBuffer[j]);
        }
        for (int j = 99; j < 99 + 201; j++)
        {
          int result = 0xFF & readBuffer[j];
          assertEquals(expected + 1, result);
          expected = (expected + 1) % 211;
        }
        for (int j = 99 + 201; j < 1000; j++)
        {
          assertEquals(0, readBuffer[j]);
        }

      }
    }
    finally
    {
      instance.close();
    }
  }

  /**
   * Test of available method, of class TimeoutableInputStreamPipe.
   */
  @Test
  public void testAvailable() throws Exception
  {
    System.out.println("available");
    TimeoutableInputStreamPipe instance = new TimeoutableInputStreamPipe();
    try
    {
      //int expResult = 0;

      int sum = 0;

      assertEquals(sum, instance.available());

      for (int i = 0; i < 171; i++)
      {
        byte[] data = new byte[i];
        instance.put(data);
        sum += i;
        assertEquals(sum, instance.available());
      }
    }
    finally
    {
      instance.close();
    }
  }

  /**
   * Test of put method, of class TimeoutableInputStreamPipe.
   */
  @Test
  public void testPut()
  {
    System.out.println("put is tested in the read methods");
  }

  /**
   * Test of readTO method, of class TimeoutableInputStreamPipe.
   */
  @Test
  public void testRead_5args() throws Exception
  {
    System.out.println("read");
    byte[] b = null;
    //int off = 0;
    //int len = 0;
    TimeoutableInputStreamPipe instance = new TimeoutableInputStreamPipe();
    try
    {
      byte data[] = new byte[211];
      for (int j = 0; j < 211; j++)
      {
        data[j] = (byte)(j + 1);
      }

      int expected = 0;

      for (int i = 0; i < 256; i++)
      {
        instance.put(data);

        byte[] readBuffer = new byte[1000];

        int readCount = instance.readTO(readBuffer, 99, 201, 1000, 1000);
        assertEquals(201, readCount);

        for (int j = 0; j < 99; j++)
        {
          assertEquals(0, readBuffer[j]);
        }
        for (int j = 99; j < 99 + 201; j++)
        {
          int result = 0xFF & readBuffer[j];
          assertEquals(expected + 1, result);
          expected = (expected + 1) % 211;
        }
        for (int j = 99 + 201; j < 1000; j++)
        {
          assertEquals(0, readBuffer[j]);
        }

      }
    }
    finally
    {
      instance.close();
    }
  }

  /**
   * Test of readTO method, of class TimeoutableInputStreamPipe.
   */
  @Test
  public void testRead_5args_2() throws Exception
  {
    System.out.println("read");
    byte[] b = null;
    //int off = 0;
    //int len = 0;
    final TimeoutableInputStreamPipe instance = new TimeoutableInputStreamPipe();
    try
    {

      int expected = 0;

      Thread putter = new Thread()
      {
        @Override
        public void run()
        {
          try
          {
            byte[] data = new byte[211];
            for (int j = 0; j < 211;
                 j++)
            {
              data[j] = (byte)(j + 1);
            }
            Thread.sleep(200);
            for (int i = 0; i < 256;
                 i++)
            {
              instance.put(data);
              if (interrupted())
              {
                break;
              }
              Thread.sleep(10);
            }
          }
          catch (InterruptedException ex)
          {
          }
        }

      };


      putter.start();
      try
      {
        for (int i = 0; i < 256; i++)
        {
          byte[] readBuffer = new byte[1000];

          int readCount = instance.readTO(readBuffer, 99, 201, 1000, 1000);
          assertEquals(201, readCount);

          for (int j = 0; j < 99; j++)
          {
            assertEquals(0, readBuffer[j]);
          }
          for (int j = 99; j < 99 + 201; j++)
          {
            int result = 0xFF & readBuffer[j];
            assertEquals(expected + 1, result);
            expected = (expected + 1) % 211;
          }
          for (int j = 99 + 201; j < 1000; j++)
          {
            assertEquals(0, readBuffer[j]);
          }
        }
      }
      finally
      {
        putter.interrupt();
      }
    }
    finally
    {
      instance.close();
    }
  }

  /**
   * Test of readTO method, of class TimeoutableInputStreamPipe.
   */
  @Test
  public void testRead_5args_3() throws Exception
  {
    System.out.println("read");
    byte[] b = null;
    //int off = 0;
    //int len = 0;
    TimeoutableInputStreamPipe instance = new TimeoutableInputStreamPipe();
    try
    {

      byte data[] = new byte[211];
      for (int j = 0; j < 211; j++)
      {
        data[j] = (byte)(j + 1);
      }

      instance.put(data);
      instance.finishPut();

      int expected = 0;

      byte[] readBuffer = new byte[1000];

      long start = System.currentTimeMillis();
      int readCount = instance.readTO(readBuffer, 99, 250, 100 * 2000, 2000);
      long end = System.currentTimeMillis();

      assertEquals(211, readCount);
      //assertFalse("method returned to fast", end - start < (2000 / 10 * 8));
      System.out.println("Duration: " + (end - start));
      for (int j = 0; j < 99; j++)
      {
        assertEquals(0, readBuffer[j]);
      }
      for (int j = 99; j < 99 + 211; j++)
      {
        int result = 0xFF & readBuffer[j];
        assertEquals(expected + 1, result);
        expected = (expected + 1) % 211;
      }
      for (int j = 99 + 211; j < 1000; j++)
      {
        assertEquals(0, readBuffer[j]);
      }
    }
    finally
    {
      instance.close();
    }
  }

}
