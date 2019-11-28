/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elster.protocols.hdlc;

import com.elster.coding.CodingUtils;
import com.elster.protocols.hdlc.secondary.HdlcSecStationEchoProtocol;
import com.elster.protocols.IStreamProtocol;
import com.elster.protocols.ProtocolBridge;
import com.elster.protocols.StreamProtocol;
import com.elster.protocols.streams.SimpleInputStreamMultiplexer;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;
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
public class HdlcStressTest
{
  public HdlcStressTest()
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
   * Test of start method, of class HdlcSecStationEchoProtocol.
   */
  @Test
  public void testStart() throws InterruptedException, IOException
  {
    System.out.println("start");
    for (int round = 1; round <= 3826; round++)
    {

      ProtocolBridge bridge = new ProtocolBridge();

      HdlcSecStationEchoProtocol secHdlc = new HdlcSecStationEchoProtocol(bridge.getSideA(), new HdlcAddress(10));
      secHdlc.start();

      HdlcProtocol primHdlc = new HdlcProtocol(bridge.getSideB());
      primHdlc.setResponseTimeout(1000);
      primHdlc.open();

      HdlcChannel channel = primHdlc.openChannel(new HdlcAddress(2), new HdlcAddress(10), true);

      Random random = new Random(135124);

      byte[] sent = new byte[round];
      random.nextBytes(sent);

      channel.sendInformation(sent);

      byte[] exp = new byte[sent.length];

      for (int i = 0; i < sent.length; i++)
      {
        exp[i] = (byte)(((0xFF & sent[i]) + 1) % 255);
      }

      HdlcInformationBlockIn block = channel.receiveInformationBlock(true,200);
      channel.close();

      secHdlc.stop();

      byte[] received = block.getBytes();

      assertArrayEquals("Round " + round, exp, received);

      //Auführung beschleunigen:
      if (round > 10)
      {
        round += 3;
      }
      if (round > 100)
      {
        round += 8;
      }
    }
  }
  
  private static final int SECONDARY_COUNT = 40;

  /**
   * Test of start method, of class HdlcSecStationEchoProtocol.
   */
  @Test(timeout= 60000)
  public void testStart2() throws InterruptedException, IOException
  {
    System.out.println("HDLC stress test (this may take a while)");

    ProtocolBridge bridge = new ProtocolBridge();



    SimpleInputStreamMultiplexer multiplexer = new SimpleInputStreamMultiplexer(bridge.getSideA().
            getInputStream(), SECONDARY_COUNT);
//    InputStreamMultiplexer multiplexer = new InputStreamMultiplexer(bridge.getSideA().
//            getInputStream(), secCount);
    multiplexer.open();

    HdlcSecStationEchoProtocol[] secHdlc = new HdlcSecStationEchoProtocol[SECONDARY_COUNT];
    IStreamProtocol[] secSublayersProtocols = new StreamProtocol[SECONDARY_COUNT];
    ChannelTester[] tester = new ChannelTester[SECONDARY_COUNT];
    Thread threads[] = new Thread[SECONDARY_COUNT];

    for (int i = 0; i < SECONDARY_COUNT; i++)
    {
      secSublayersProtocols[i] = new StreamProtocol(multiplexer.getInputStream(i), bridge.getSideA().
              getOutputStream(), true);
      secHdlc[i] = new HdlcSecStationEchoProtocol(secSublayersProtocols[i], new HdlcAddress(1, i, 4));
      secHdlc[i].start();
    }


    final HdlcProtocol primHdlc = new HdlcProtocol(bridge.getSideB());
    primHdlc.setResponseTimeout(1000);
    primHdlc.open();

    ThreadGroup tg = new ThreadGroup("tester");
    for (int i = 0; i < SECONDARY_COUNT; i++)
    {
      tester[i] = new ChannelTester(i, new HdlcAddress(1, i, 4), primHdlc);
      threads[i] = new Thread(tg, tester[i]);
      threads[i].start();
    }

    for (int i = 0; i < SECONDARY_COUNT; i++)
    {
      threads[i].join();
      tester[i].testResult();
    }

    for (HdlcSecStationEchoProtocol secS : secHdlc)
    {
      secS.stop();
    }
  }

  private static class ChannelTester implements Runnable
  {
    HdlcAddress dest;
    HdlcProtocol hdlcProtocol;
    boolean ok = true;
    byte[] errorReceivedBytes;
    byte[] errorExpectedBytes;
    boolean finished = false;
    int index;

    public ChannelTester(int index, HdlcAddress dest, HdlcProtocol hdlcProtocol)
    {
      this.dest = dest;
      this.hdlcProtocol = hdlcProtocol;
      this.index = index;
    }

    //@Override
    public void run()
    {
      try
      {
        HdlcChannel channel =
                hdlcProtocol.openChannel(new HdlcAddress(10), dest, true);
        Random random = new Random(dest.hashCode());

        int rounds = 60;
        for (int round = 0; round < rounds; round++)
        {
          Thread.sleep(random.nextInt(250) + 1);

          byte[] sent = new byte[random.nextInt(5000)+1];
          random.nextBytes(sent);

          channel.sendInformation(sent);

          HdlcInformationBlockIn block = channel.receiveInformationBlock(true,2000);
          byte[] received = block.getBytes();
          
          
          byte[] exp = new byte[sent.length];

          for (int i = 0; i < sent.length; i++)
          {
            exp[i] = (byte)(((0xFF & sent[i]) + 1) % 255);
          }
          
          if (ok)
          {
            ok=Arrays.equals(exp, received);
            if (!ok)
            {
              errorReceivedBytes= CodingUtils.copyOf(received,received.length);
              errorExpectedBytes= CodingUtils.copyOf(exp,exp.length);
            }

          }
        }

        channel.close();
        finished = true;
      }

      catch (InterruptedException ex)
      {
        Logger.getLogger(HdlcStressTest.class.getName()).log(Level.SEVERE, null, ex);
      }      catch (IOException ex)
      {
        Logger.getLogger(HdlcStressTest.class.getName()).log(Level.SEVERE, null, ex);
      }
    }

    public void testResult()
    {
      assertTrue("Index " + index, finished);

      if (!ok)
      {
        assertArrayEquals("Index " + index, errorExpectedBytes, errorReceivedBytes);
      }
      assertTrue("Index " + index,ok);
    }

  }


}
