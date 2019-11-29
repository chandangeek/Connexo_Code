/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elster.protocols.comportdriver;

import java.util.Arrays;
import com.elster.protocols.streams.SafeReadInputStream;
import com.elster.protocols.streams.TimeoutInputStream;
import java.io.IOException;
import java.util.Random;
import static org.junit.Assert.*;

/**
 * This test can only be run if com port {@code port1} and com port {@code port2}
 * are connected with a null modem cable.
 *
 * @author osse
 */
public class ComPortDriverTestManual
{
  private static final int BAUDRATE = 115200;
  private static final int PORT1 = 7;
  private static final int PORT2 = 9;
  private static final long SEED1 = 36498;
  private static final long SEED2 = 162416;
  private static final int SIZE1 = 564312;
  private static final int SIZE2 = 546313;
  private static final int MAX_BLOCK_SIZE = 424;

  public ComPortDriverTestManual()
  {
  }

  // @Test
  public void testSend() throws Exception
  {
    System.out.println("test send");
    ComPortDriver instance1 = new ComPortDriver(100);
    instance1.setBaudrate(BAUDRATE);
    instance1.setComPort(PORT1);
    instance1.open();
    try
    {
      instance1.getOutputStream().write(new byte[]
              {
                (byte)31, (byte)32, (byte)33, (byte)34
              });
    }
    finally
    {
      instance1.close();
    }
  }

  /**
   * Test of open method, of class ComPortDriver.
   */
  //@Test
  public void testDataTransfer() throws Exception
  {
    System.out.println("test data transfer");
    ComPortDriver instance1 = new ComPortDriver(100);
    instance1.setBaudrate(BAUDRATE);
    instance1.setComPort(PORT1);
    instance1.open();

    ComPortDriver instance2 = new ComPortDriver(100);
    instance2.setBaudrate(BAUDRATE);
    instance2.setComPort(PORT2);
    instance2.open();

    DataReceiver dataReceiver2 = new DataReceiver(SEED1, instance2, SIZE1);
    DataSender dataSender1 = new DataSender(SEED1, instance1, SIZE1, dataReceiver2);
    DataReceiver dataReceiver1 = new DataReceiver(SEED2, instance1, SIZE2);
    DataSender dataSender2 = new DataSender(SEED2, instance2, SIZE2, dataReceiver1);

    // dataSender1.extraWork=5*1000000;
    // dataReceiver1.extraWork=5*1000000;
    // dataSender2.extraWork=100000;
    // dataReceiver2.extraWork=100000;

    Thread[] threads = new Thread[4];

    threads[0] = new Thread(dataSender1);
    threads[0].setName("dataSender1");
    threads[1] = new Thread(dataReceiver2);
    threads[1].setName("dataReceiver2");
    threads[2] = new Thread(dataSender2);
    threads[2].setName("dataSender2");
    threads[3] = new Thread(dataReceiver1);
    threads[3].setName("dataReceiver1");

    for (Thread t : threads)
    {
      t.start();
    }

    for (Thread t : threads)
    {
      t.join();
    }

    assertEquals(true, dataSender1.isResult());
    assertEquals(true, dataReceiver2.isResult());
    assertEquals(true, dataSender2.isResult());
    assertEquals(true, dataReceiver1.isResult());
  }

  private static class DataSender implements Runnable
  {
    long extraWork = 0;
    private final long randomSeed;
    private final ComPortDriver comPortDriver;
    private int bytesToSend;
    private int bytesSend = 0;
    private boolean result = true;
    private final DataReceiver dataReceiver;

    public DataSender(long randomSeed, ComPortDriver comPortDriver, int bytesToSend, DataReceiver dataReceiver)
    {
      this.randomSeed = randomSeed;
      this.comPortDriver = comPortDriver;
      this.bytesToSend = bytesToSend;
      this.dataReceiver = dataReceiver;
    }

    public void run()
    {
      try
      {

        Random random = new Random(randomSeed);

        while (bytesToSend > 0)
        {
          int block = random.nextInt(MAX_BLOCK_SIZE) + 1;
          if (block > bytesToSend)
          {
            block = bytesToSend;
          }
          bytesToSend -= block;

          if (bytesToSend == 0)
          {
            break;
          }

          byte[] data = new byte[block];
          random.nextBytes(data);

          //while (dataReceiver.isResult() && bytesSend - dataReceiver.getBytesRead() > 12048) //einfache sync. um überlaufen des send buffers zu verhindern.
          while (dataReceiver.isResult() && comPortDriver.getConnection().getBytesInTxBuffer() > 1000) //einfache sync. um überlaufen des send buffers zu verhindern.
          {
            Thread.sleep(10);
          }

          if (!dataReceiver.isResult())
          {
            throw new IOException("Receiver failed");
          }

          if (extraWork > 0)
          {
            long d = 1346;
            for (long i = 0; i < extraWork; i++)
            {
              d = (d + 3341654L) * 324 % 22412133L;
            }
          }

          comPortDriver.getOutputStream().write(data);
          bytesSend += data.length;


        }
      }
      catch (Exception ex)
      {
        System.out.print(ex.toString());
        result = false;
      }
    }

    public boolean isResult()
    {
      return result;
    }

  }

  private static class DataReceiver implements Runnable
  {
    private final long randomSeed;
    private final ComPortDriver comPortDriver;
    private int bytesToRead;
    private int bytesRead = 0;
    long extraWork = 0;
    private int nextNotification = 10000;
    private boolean result = true;

    public DataReceiver(long randomSeed, ComPortDriver comPortDriver, int bytesToRead)
    {
      this.randomSeed = randomSeed;
      this.comPortDriver = comPortDriver;
      this.bytesToRead = bytesToRead;
    }

    public void run()
    {
      try
      {
        SafeReadInputStream in = new SafeReadInputStream(new TimeoutInputStream(comPortDriver.getInputStream(),
                                                                                5000));
        try
        {

          Random random = new Random(randomSeed);

          while (bytesToRead > 0)
          {
            int block = random.nextInt(MAX_BLOCK_SIZE) + 1;
            if (block > bytesToRead)
            {
              block = bytesToRead;
            }
            bytesToRead -= block;

            if (bytesToRead == 0)
            {
              break;
            }

            byte[] expected = new byte[block];
            random.nextBytes(expected);

            byte[] actualls = new byte[block];

            if (extraWork > 0)
            {
              long d = 1346;
              for (int i = 0; i < extraWork; i++)
              {
                d = (d + 3341654L) * 324 % 22412133L;
              }
            }


            bytesRead += in.read(actualls);

            if (nextNotification < bytesRead)
            {

              System.out.print(Thread.currentThread().getName() + ": " + (100 * bytesRead / (bytesToRead
                                                                                             + bytesRead))
                               + "% " + bytesRead + "/" + (bytesToRead + bytesRead) + " bytes received.\r\n");
              nextNotification = bytesRead + 10000;
            }


            if (!Arrays.equals(expected, actualls))
            {
              throw new IOException("Wrong data");
            }
          }
        }
        finally
        {
          try
          {
            in.close();
          }
          catch (final IOException ignore)
          {
            //ignore the close exception
          }
        }
      }
      catch (IOException ex)
      {
        System.out.print(ex.toString());
        result = false;
      }
      catch (RuntimeException ex)
      {
        System.out.print(ex.toString());
        result = false;
      }
    }

    public boolean isResult()
    {
      return result;
    }

    public int getBytesRead()
    {
      return bytesRead;
    }

  }

}
