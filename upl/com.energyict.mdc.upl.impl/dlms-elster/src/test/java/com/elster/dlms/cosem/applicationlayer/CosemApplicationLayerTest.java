/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elster.dlms.cosem.applicationlayer;

import com.elster.dlms.apdu.coding.CoderXDlmsApdu;
import com.elster.coding.CodingUtils;
import com.elster.dlms.security.DlmsSecurityProviderGcm;
import com.elster.dlms.cosem.application.services.common.SecurityControlField;
import com.elster.dlms.cosem.application.services.open.DlmsConformance;
import com.elster.dlms.cosem.application.services.open.ProposedXDlmsContext;
import java.util.EnumSet;
import com.elster.protocols.streams.TimeoutIOException;
import com.elster.protocols.hdlc.HdlcProtocol;
import com.elster.protocols.hdlc.HdlcAddress;
import com.elster.protocols.IStreamProtocol;
import com.elster.protocols.ProtocolBridge;
import com.elster.dlms.cosem.profiles.hdlc.DlmsLlcHdlc;
import com.elster.protocols.hdlc.secondary.HdlcSecStationRRProtocol;
import java.io.IOException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author osse
 */
public class CosemApplicationLayerTest
{
  public CosemApplicationLayerTest()
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

  @Test()
  public void testEncodeProposedXDlmsContext() throws IOException
  {
    CoderXDlmsApdu coderXDlmsApdu = new CoderXDlmsApdu();
    DlmsSecurityProviderGcm securityProvider = new DlmsSecurityProviderGcm(CodingUtils.string2ByteArray(
            "00 01 02 03 04 05 06 07 08 09 0A 0B 0C 0D 0E 0F"), CodingUtils.string2ByteArray(
            "D0 D1 D2 D3 D4 D5 D6 D7 D8 D9 DA DB DC DD DE DF"),
                                                                           CodingUtils.string2ByteArray(
            "BC 61 4E 2E B8 B9 82 77"));
    securityProvider.setRespondingApTitle(CodingUtils.string2ByteArray("BC 61 4E 2E B8 B9 82 77"));

    coderXDlmsApdu.setSecurityProvider(securityProvider);

    ProposedXDlmsContext proposedXDlmsContext = new ProposedXDlmsContext();

    proposedXDlmsContext.setProposedDlmsConformance(
            EnumSet.of(
            DlmsConformance.ACTION,
            DlmsConformance.SET,
            DlmsConformance.GET));

    proposedXDlmsContext.setDedicatedKey(securityProvider.getDedicatedKey());
    proposedXDlmsContext.setResponseAllowed(true);

    proposedXDlmsContext.setSecurityControlField(
            new SecurityControlField(0, true, true, SecurityControlField.CipheringMethod.GLOBAL_UNICAST));

    byte[] encodeObjectToBytes = coderXDlmsApdu.encodeObjectToBytes(proposedXDlmsContext);

    System.out.println("encoded bytes: " + CodingUtils.byteArrayToString(encodeObjectToBytes));
  }

  /**
   * Tests that continually received RR-Frames lead to an timeout.
   */
  @Test(timeout = 10000)//10000
  public void testOpen_RR_Answers() throws Exception
  {

    System.out.println("open RR");
    int logicalDeviceID = 0x10;

    ProtocolBridge bridge = new ProtocolBridge();
    HdlcSecStationRRProtocol secHdlc =
            new HdlcSecStationRRProtocol(bridge.getSideA(), new HdlcAddress(0x10, 0x11, 4));
   
    secHdlc.start();


//    LoggingProtocol loggingProtocol = new LoggingProtocol(bridge.getSideB(), new AnalyseTraceLogHandler(
//            "d:/temp/rnrTest.txt"));
//    loggingProtocol.open();

    IStreamProtocol loggingProtocol = bridge.getSideB();

    HdlcProtocol hdlcProtocol = new HdlcProtocol(loggingProtocol);
    hdlcProtocol.setPollingIntervall(250);
    hdlcProtocol.setInternalIntervall(50);
    hdlcProtocol.open();

    IDlmsLlc dlmsLlc = new DlmsLlcHdlc(hdlcProtocol, 0x11);

    CosemApplicationLayer instance = new CosemApplicationLayer(dlmsLlc);
    instance.setPduRxTimeout(5000);//5000
    try
    {
      instance.open(logicalDeviceID, "121345");

      fail("TimeoutIOException expected");
    }
    catch (TimeoutIOException ex)
    {
    }

    secHdlc.stop();
    assertTrue("More than 5 RR Frames expected. Received: " + secHdlc.getReceivedRRCounter(), secHdlc.
            getReceivedRRCounter() > 5); //Ensure that something happened
    hdlcProtocol.close();
  }

}
