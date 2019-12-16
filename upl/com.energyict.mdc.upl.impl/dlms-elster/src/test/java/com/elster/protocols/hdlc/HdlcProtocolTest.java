/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elster.protocols.hdlc;

import com.elster.protocols.IStreamProtocol;
import com.elster.coding.CodingUtils;
import com.elster.protocols.ProtocolBridge;
import com.elster.protocols.hdlc.secondary.HdlcSecStationTestProtocol;
import java.io.IOException;
import java.util.Random;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author osse
 */
public class HdlcProtocolTest
{
  public HdlcProtocolTest()
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

  @Test(timeout=30000)
  public void testRnr() throws IOException
  {
    System.out.println("RNR test");
    ProtocolBridge bridge = new ProtocolBridge();
    HdlcSecStationTestProtocol secHdlc =
            new HdlcSecStationTestProtocol(bridge.getSideA(), new HdlcAddress(10));
    secHdlc.start();


//    LoggingProtocol loggingProtocol = new LoggingProtocol(bridge.getSideB(), new AnalyseTraceLogHandler(
//            "d:/temp/rnrTest.txt"));
//    loggingProtocol.open();

    IStreamProtocol loggingProtocol = bridge.getSideB();

    HdlcProtocol instance = new HdlcProtocol(loggingProtocol);
    instance.open();

    HdlcChannel channel = instance.openChannel(new HdlcAddress(11), new HdlcAddress(10), true);

    channel.sendInformation(CodingUtils.string2ByteArray("02 0A 30 40 50"));
    HdlcInformationBlockIn receiveInformationBlock = channel.receiveInformationBlock(true, 30 * 1000);

    byte expects[] = CodingUtils.string2ByteArray("30 40 50");

    channel.close();
    instance.close();
    secHdlc.stop();

    assertArrayEquals(expects, receiveInformationBlock.getBytes());
    assertTrue(secHdlc.getRnrCount() > 2); //Assert that some RNR frames were sent.
  }

  @Test
  public void testInformationTransfer() throws IOException
  {
    System.out.println("I-Frame test");
    ProtocolBridge bridge = new ProtocolBridge();
    HdlcSecStationTestProtocol secHdlc =
            new HdlcSecStationTestProtocol(bridge.getSideA(), new HdlcAddress(10));
    secHdlc.start();

//    LoggingProtocol loggingProtocol = new LoggingProtocol(bridge.getSideB(), new AnalyseTraceLogHandler(
//            "d:/temp/rnrTest.txt"));
//    loggingProtocol.open();

    IStreamProtocol loggingProtocol = bridge.getSideB();

    HdlcProtocol instance = new HdlcProtocol(loggingProtocol);
    instance.open();

    HdlcChannel channel = instance.openChannel(new HdlcAddress(11), new HdlcAddress(10), true);

    Random random = new Random(234116L);

    for (int i = 0; i < 1000; i++)
    {

      byte[] info = new byte[random.nextInt(200) + 1];
      info[0] = 0x01;
      byte expects[] = CodingUtils.copyOfRange(info, 1, info.length);

      channel.sendInformation(info);
      HdlcInformationBlockIn receiveInformationBlock = channel.receiveInformationBlock(true, 3000);

      assertArrayEquals(expects, receiveInformationBlock.getBytes());
    }

    channel.close();
    instance.close();
    secHdlc.stop();
  }

}
