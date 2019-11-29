/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elster.dlms.apdu.coding;

import com.elster.dlms.cosem.application.services.open.DlmsConformance;
import com.elster.coding.CodingUtils;
import com.elster.dlms.cosem.application.services.open.ProposedXDlmsContext;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.EnumSet;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author osse
 */
public class CoderInitiateRequestTest
{
  public CoderInitiateRequestTest()
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
   * Test of encodeObject method, of class CoderInitiateRequest.
   */
  @Test
  public void testEncodeObject() throws Exception
  {
    System.out.println("encodeObject");
    ProposedXDlmsContext object = new ProposedXDlmsContext();
    object.setProposedDlmsVersionNumber(6);
    object.setProposedDlmsConformance(EnumSet.of(
            DlmsConformance.PRIORITY_MGMT_SUPPORTED,
            DlmsConformance.ATTRIBUTE0_SUPPORTED_WITH_GET,
            DlmsConformance.BLOCK_TRANSFER_WITH_GET_OR_READ,
            DlmsConformance.BLOCK_TRANSFER_WITH_SET_OR_WRITE,
            DlmsConformance.BLOCK_TRANSFER_WITH_ACTION,
            DlmsConformance.MULTIPLE_REFERENCES,
            DlmsConformance.GET,
            DlmsConformance.SET,
            DlmsConformance.SELECTIVE_ACCESS,
            DlmsConformance.EVENT_NOTIFICATION,
            DlmsConformance.ACTION));
    object.setClientMaxReceivePduSize(1200);

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    CoderInitiateRequest instance = new CoderInitiateRequest(true);
    instance.encodeObject(object, out);

    byte[] expects= CodingUtils.string2ByteArray("01 00 00 00 06 5F 1F 04 00 00 7E 1F 04 B0");

    assertArrayEquals(expects, out.toByteArray());
  }

   /**
   * Test of encodeObject method, of class CoderInitiateRequest.<P>
   * With dedicated key. <P>
   * Example from GB ed.7 p.264
   */
  @Test
  public void testEncodeObject2() throws Exception
  {
    System.out.println("encodeObject");
    ProposedXDlmsContext object = new ProposedXDlmsContext();

    object.setDedicatedKey(CodingUtils.string2ByteArray("00112233445566778899AABBCCDDEEFF"));
    object.setProposedDlmsVersionNumber(6);
    object.setProposedDlmsConformance(EnumSet.of(
            DlmsConformance.PRIORITY_MGMT_SUPPORTED,
            DlmsConformance.ATTRIBUTE0_SUPPORTED_WITH_GET,
            DlmsConformance.BLOCK_TRANSFER_WITH_GET_OR_READ,
            DlmsConformance.BLOCK_TRANSFER_WITH_SET_OR_WRITE,
            DlmsConformance.BLOCK_TRANSFER_WITH_ACTION,
            DlmsConformance.MULTIPLE_REFERENCES,
            DlmsConformance.GET,
            DlmsConformance.SET,
            DlmsConformance.SELECTIVE_ACCESS,
            DlmsConformance.EVENT_NOTIFICATION,
            DlmsConformance.ACTION));
    object.setClientMaxReceivePduSize(1200);

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    CoderInitiateRequest instance = new CoderInitiateRequest(true);
    instance.encodeObject(object, out);

    byte[] result = out.toByteArray();

    System.out.println(CodingUtils.byteArrayToString(result));
    byte[] expects= CodingUtils.string2ByteArray("0101100011223344 5566778899AABBCC DDEEFF0000065F1F 0400007E1F04B0");

    assertArrayEquals(expects,result);
  }


  /**
   * Test of decodeObject method, of class CoderInitiateRequest.
   */
  @Test
  public void testDecodeObject() throws Exception
  {
    System.out.println("decodeObject");
    InputStream in = CodingUtils.string2InputStream("01 00 00 00 06 5F 1F 04 00 00 7E 1F 04 B0");
    CoderInitiateRequest instance = new CoderInitiateRequest(true);
    ProposedXDlmsContext result = instance.decodeObject(in);

    assertEquals(null, result.getDedicatedKey());
    assertEquals(6, result.getProposedDlmsVersionNumber());
    EnumSet<DlmsConformance> proposedDlmsConformance = result.getProposedDlmsConformance();
    EnumSet<DlmsConformance> expecetedConformance = EnumSet.of(
            DlmsConformance.PRIORITY_MGMT_SUPPORTED,
            DlmsConformance.ATTRIBUTE0_SUPPORTED_WITH_GET,
            DlmsConformance.BLOCK_TRANSFER_WITH_GET_OR_READ,
            DlmsConformance.BLOCK_TRANSFER_WITH_SET_OR_WRITE,
            DlmsConformance.BLOCK_TRANSFER_WITH_ACTION,
            DlmsConformance.MULTIPLE_REFERENCES,
            DlmsConformance.GET,
            DlmsConformance.SET,
            DlmsConformance.SELECTIVE_ACCESS,
            DlmsConformance.EVENT_NOTIFICATION,
            DlmsConformance.ACTION);

    assertEquals(expecetedConformance, proposedDlmsConformance);
    assertEquals(1200, result.getClientMaxReceivePduSize());
  }

}
