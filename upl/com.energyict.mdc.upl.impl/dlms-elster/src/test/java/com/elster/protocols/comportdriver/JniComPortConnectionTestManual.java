/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elster.protocols.comportdriver;

import java.io.IOException;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.junit.Assert.*;

/**
 * This test can only be run if com port {@code port1} and com port {@code port2} 
 * are connected with a null modem cable
 *
 * @author osse
 */
public class JniComPortConnectionTestManual
{
  private static final int BAUDRATE = 115200;
  private static final int PORT1 = 9;
  private static final int PORT2 = 7;
  private static final long SIZE1 = 564312000L;
  private static final long SIZE2 = 546313000L;
  private static final int MAX_BLOCK_SIZE = 2024;

  public JniComPortConnectionTestManual() throws IOException
  {
  }

 // @Test
  public void testDataTransfer() throws InterruptedException, IOException
  {
    DataReceiver receiveHandler1 = new DataReceiver("Receiver 1");
 //   receiveHandler1.extraWork = 50000;

    DataReceiver receiveHandler2 = new DataReceiver("Receiver 2");

    final JniComPortConnection connection1 = new JniComPortConnection("COM"+PORT1);
    connection1.setOnReceivedHandler(receiveHandler1);
    connection1.open(PORT1, BAUDRATE, JniComPortConnection.RTS_CONTROL_ENABLE, true);

    new Thread(new Runnable()
    {
      public void run()
      {
        try
        {
          connection1.startReceiving();
        }
        catch (IOException ex)
        {
          Logger.getLogger(JniComPortConnectionTestManual.class.getName()).log(Level.SEVERE, null, ex);
        }
      }

    }).start();



    final JniComPortConnection connection2 = new JniComPortConnection("COM"+PORT2);
    connection2.setOnReceivedHandler(receiveHandler2);
    connection2.open(PORT2, BAUDRATE, JniComPortConnection.RTS_CONTROL_ENABLE, true);

    new Thread(new Runnable()
    {
      public void run()
      {
        try
        {
          connection2.startReceiving();
        }
        catch (IOException ex)
        {
          Logger.getLogger(JniComPortConnectionTestManual.class.getName()).log(Level.SEVERE, null, ex);
        }
      }

    }).start();


    DataSender dataSender1 = new DataSender(connection1, SIZE1);
   // dataSender1.extraWork = 100000;
    DataSender dataSender2 = new DataSender(connection2, SIZE2);


    Thread[] threads = new Thread[2];

    threads[0] = new Thread(dataSender1);
    threads[0].setName("dataSender1");
    threads[1] = new Thread(dataSender2);
    threads[1].setName("dataSender2");

    for (Thread t : threads)
    {
      t.start();
    }

    for (Thread t : threads)
    {
      t.join();
    }

    connection1.cancelReceiving();
    connection2.cancelReceiving();

    assertEquals(0, receiveHandler1.getErrorCount());
    assertEquals(0, receiveHandler2.getErrorCount());

  }

  private static class DataSender implements Runnable
  {
    long extraWork = 0;
    private final JniComPortConnection connection;
    private long bytesToSend;
    private boolean result = true;

    public DataSender(JniComPortConnection connection, long bytesToSend)
    {
      this.connection = connection;
      this.bytesToSend = bytesToSend;
    }

    int currentNo = 0;

    private byte[] buildBlock(int size)
    {
      byte[] data = new byte[size];

      for (int i = 0; i < size; i++)
      {
        data[i] = (byte)currentNo++;
        if (currentNo > 255)
        {
          currentNo = 0;
        }
//        if (currentNo == 0x7E)
//        {
//          currentNo++;
//        }
      }
      return data;
    }

    public void run()
    {
      try
      {
        Random random = new Random(16368);

        while (bytesToSend > 0)
        {
          long block = random.nextInt(MAX_BLOCK_SIZE) + 1;
          if (block > bytesToSend)
          {
            block = bytesToSend;
          }
          bytesToSend -= block;

          if (block == 0)
          {
            break;
          }

          byte[] data = buildBlock((int) block);

          if (extraWork > 0)
          {
            long d = 1346;
            for (long i = 0; i < extraWork; i++)
            {
              d = (d + 3341654L) * 324 % 22412133L;
            }
          }

          while (connection.getBytesInTxBuffer() > 1000)
          {
            if (extraWork > 0)
            {
              long d = 0;
              for (int i = 0; i < extraWork; i++)
              {
                d = (d + 334123L) * 13354L % 3431535L;
              }
            }
            else
            {
              Thread.sleep(10);
            }
          }
          connection.send(data);
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

  private static class DataReceiver implements JniComPortConnection.OnReceivedHandler
  {
    private final String name;
    long extraWork = 0;
    int currentNo = 0;
    int errorCount = 0;
    long bytesReceived = 0;
    private long nextNotification = 1000;

    public DataReceiver(String name)
    {
      this.name = name;
    }

    public void onDataReceived(byte[] data)
    {
      boolean lastWasError = false;

      for (int i = 0; i < data.length; i++)
      {
        bytesReceived++;

        if ((data[i] & 0xFF) != currentNo)
        {
          currentNo = data[i] & 0xFF;
          if (!lastWasError)
          {
            errorCount++;
            System.out.println(name + ": ErrorCount:" + errorCount);
          }
          lastWasError = true;
        }
        else
        {
          lastWasError = false;
        }
        currentNo++;
        if (currentNo > 255)
        {
          currentNo = 0;
        }
        if (nextNotification < bytesReceived)
        {
          System.out.println(name + ": " + bytesReceived + " bytes received."+ ": ErrorCount:" + errorCount);
          nextNotification = bytesReceived *11 /10;
        }

//        if (currentNo == 0x7E)
//        {
//          currentNo++;
//        }
      }
      if (extraWork > 0 && data.length<50)
      {
        long d = 0;
        for (int j = 0; j < extraWork; j++)
        {
          d = (d + 334123L) * 13354L % 3431535L;
        }
      }

    }

    public int getErrorCount()
    {
      return errorCount;
    }

  }

}
