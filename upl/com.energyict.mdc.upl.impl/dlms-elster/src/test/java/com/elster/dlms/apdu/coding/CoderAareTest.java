/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elster.dlms.apdu.coding;

import com.elster.dlms.cosem.application.services.common.AbstractCosemServiceInvocation;
import com.elster.dlms.security.DlmsSecurityProviderGcm;
import java.io.ByteArrayInputStream;
import java.util.EnumSet;
import com.elster.dlms.cosem.application.services.open.AuthenticationValue;
import com.elster.dlms.cosem.application.services.open.DlmsConformance;
import com.elster.dlms.types.basic.BitString;
import com.elster.dlms.cosem.application.services.open.FailureType;
import com.elster.dlms.cosem.application.services.open.NegotiatedXDlmsContext;
import com.elster.coding.CodingUtils;
import com.elster.dlms.cosem.application.services.open.OpenResponse;
import com.elster.dlms.definitions.DlmsOids;
import com.elster.protocols.streams.SafeReadInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @author osse
 */
public class CoderAareTest
{
  /**
   * Test of encodeObject method, of class CoderAare. <P>
   * Not implemented. Encoding is tested by the "Decode_Encode" tests.
   *
   */
  public void testEncodeObject() throws Exception
  {
    fail("not implemented");
    System.out.println("encodeObject");
    OpenResponse openResponse = null;
    OutputStream out = null;
    CoderAare instance = new CoderAare(true, true);
    instance.encodeObject(openResponse, out);
  }

  /**
   * Test of decodeObject method, of class CoderAare.<P>
   *
   * With original EK240 answer
   */
  @Test
  public void testDecodeObject() throws Exception
  {
    System.out.println("decodeObject");

    InputStream in = CodingUtils.string2InputStream(
            "61 41 A1 09 06 07 60 85 74 05 08 01 01 A2 03 02 01 00 A3 05 A1 03 02 01 00 88 02 07 80 89 07"
            + "60 85 74 05 08 02 01 AA 02 80 08 35 35 35 35 35 35 35 35 BE 0F 04 0E 08 00 06 5F 1F 04 00 00 00 1D 01"
            + "90 FA 00");
    CoderAare instance = new CoderAare(true, true);
    OpenResponse result = instance.decodeObject(new SafeReadInputStream(in));

    assertEquals(DlmsOids.DLMS_UA_AC_LN, result.getApplicationContextName());
    assertEquals(0, result.getResult());
    assertEquals(new FailureType(1, 0), result.getFailureType());
    BitString acseReq = new BitString(1, new byte[]
            {
              (byte)0x80
            });
    assertEquals(acseReq, result.getAcseRequirements());
    assertEquals(DlmsOids.DLMS_UA_AMN_COSEM_LOW_LEVEL_SECURITY_MECHANISM_NAME,
                 result.getSecurityMechanismName());
    assertEquals(AuthenticationValue.createCharstring("55555555"), result.getRespondingAuthenticationValue());


    assertNotNull(result.getNegotiatedXDlmsContext());

    NegotiatedXDlmsContext context = result.getNegotiatedXDlmsContext();


    assertEquals(6, context.getNegotiatedDlmsVersionNumber());
    assertEquals(
            EnumSet.of(
            DlmsConformance.GET,
            DlmsConformance.SET,
            DlmsConformance.SELECTIVE_ACCESS,
            DlmsConformance.ACTION), context.getNegotiatedDlmsConformance());
    assertEquals(400, context.getServerMaxReceivePduSize());

    System.out.println("VAA: " + context.getVaaName());
  }

  /**
   * Test of decodeObject method, of class CoderAare.<P>
   *
   * With manually corrected EK240 answer.
   */
  @Test
  public void testDecodeObject2() throws Exception
  {
    System.out.println("decodeObject");

    InputStream in = CodingUtils.string2InputStream(
            "61 42 A1 09 06 07 60 85 74 05 08 01 01 A2 03 02 01 00 A3 05 A1 03 02 01 00 88 02 07 80 89 07"
            + "60 85 74 05 08 02 01 AA 0A 80 08 35 35 35 35 35 35 35 35 BE 10 04 0E 08 00 06 5F 1F 04 00 00 00 1D 01"
            + "90 FA 00");
    CoderAare instance = new CoderAare(true, true);
    //OpenResponse expResult = null;
    OpenResponse result = instance.decodeObject(new SafeReadInputStream(in));

    assertEquals(DlmsOids.DLMS_UA_AC_LN, result.getApplicationContextName());
    assertEquals(0, result.getResult());
    assertEquals(new FailureType(1, 0), result.getFailureType());
    BitString acseReq = new BitString(1, new byte[]
            {
              (byte)0x80
            });
    assertEquals(acseReq, result.getAcseRequirements());
    assertEquals(DlmsOids.DLMS_UA_AMN_COSEM_LOW_LEVEL_SECURITY_MECHANISM_NAME,
                 result.getSecurityMechanismName());
    assertEquals(AuthenticationValue.createCharstring("55555555"), result.getRespondingAuthenticationValue());


    assertNotNull(result.getNegotiatedXDlmsContext());

    NegotiatedXDlmsContext context = result.getNegotiatedXDlmsContext();


    assertEquals(6, context.getNegotiatedDlmsVersionNumber());
    assertEquals(
            EnumSet.of(
            DlmsConformance.GET,
            DlmsConformance.SET,
            DlmsConformance.SELECTIVE_ACCESS,
            DlmsConformance.ACTION), context.getNegotiatedDlmsConformance());
    assertEquals(400, context.getServerMaxReceivePduSize());

    System.out.println("VAA: " + context.getVaaName());

  }

  /**
   * Test of decodeObject method, of class CoderAare.<P>
   *
   * With original EK240 answer
   */
  @Test
  public void testDecodeObject3() throws Exception
  {
    System.out.println("decodeObject");

    InputStream in = CodingUtils.string2InputStream(
            "61 41 A1 09 06 07 60 85 74 05 08 01 01 A2 03 02 01 00 A3 05 A1 03 02 01 00 88 02 07 80 89 07 "
            + "60 85 74 05 08 02 01 AA 02 80 08 35 35 35 35 35 35 35 35 BE 0F 04 0D 08 00 06 5F 1F 04 00 00 00 19 01 "
            + "90 FA 00");
    CoderAare instance = new CoderAare(true, false);
    OpenResponse result = instance.decodeObject(new SafeReadInputStream(in));

    assertEquals(DlmsOids.DLMS_UA_AC_LN, result.getApplicationContextName());
    assertEquals(0, result.getResult());
    assertEquals(new FailureType(1, 0), result.getFailureType());
    BitString acseReq = new BitString(1, new byte[]
            {
              (byte)0x80
            });
    assertEquals(acseReq, result.getAcseRequirements());
    assertEquals(DlmsOids.DLMS_UA_AMN_COSEM_LOW_LEVEL_SECURITY_MECHANISM_NAME,
                 result.getSecurityMechanismName());
    assertEquals(AuthenticationValue.createCharstring("55555555"), result.getRespondingAuthenticationValue());


    assertNotNull(result.getNegotiatedXDlmsContext());

    NegotiatedXDlmsContext context = result.getNegotiatedXDlmsContext();


    assertEquals(6, context.getNegotiatedDlmsVersionNumber());
    assertEquals(
            EnumSet.of(
            DlmsConformance.GET,
            DlmsConformance.SET,
            DlmsConformance.ACTION), context.getNegotiatedDlmsConformance());
    assertEquals(400, context.getServerMaxReceivePduSize());

    System.out.println("VAA: " + context.getVaaName());

  }


    /**
   * Test of decodeObject method, of class CoderAare.<P>
   *
   * Check if a result!=0 will be decoded
   */
  @Test
  public void testDecodeObject4() throws Exception
  {
    System.out.println("decodeObject 4");

    InputStream in = CodingUtils.string2InputStream(
            "61 29 A1 09 06 07 60 85 74 05 08 01 01 A2 03 02 01 02 A3 05 A1 03 02 01 00 BE 10 04 0E 08 00 06 5F 1F 04 00 00 00 19 01 7C 00 07  ");

    CoderAare instance = new CoderAare(true, true);
    OpenResponse result = instance.decodeObject(new SafeReadInputStream(in));
    assertEquals(2, result.getResult());
  }


  //61 1F A1 09 06 07 60 85 74 05 08 01 01 A2 03 02 01 00 A3 05 A1 03 02 01 01 BE 06 04 04 0E 01 06 00

   /**
   * Test of decodeObject method, of class CoderAare.<P>
   *
   * Check if Confirmed Service Error will be decoded
   */
  @Test
  public void testDecodeObject5() throws Exception
  {
    System.out.println("decodeObject 5");

    InputStream in = CodingUtils.string2InputStream(
            "61 1F A1 09 06 07 60 85 74 05 08 01 01 A2 03 02 01 00 A3 05 A1 03 02 01 01 BE 06 04 04 0E 01 06 00 ");

    CoderAare instance = new CoderAare(true, true);
    OpenResponse result = instance.decodeObject(new SafeReadInputStream(in));
    assertNotNull(result.getxDlmsInitiateError());
    assertEquals(1, result.getxDlmsInitiateError().getConfirmedServiceErrorType());
    assertEquals(6, result.getxDlmsInitiateError().getServiceErrorType());
    assertEquals(0, result.getxDlmsInitiateError().getError());
  }
  
     /**
   * Test of decodeObject method, of class CoderAare.<P>
   *
   * Check if Confirmed Service Error will be decoded
   */
  @Test(expected=IOException.class)
  public void testDecodeObject6() throws Exception
  {
    System.out.println("decodeObject 5");

    InputStream in = CodingUtils.string2InputStream(
            "60 1F A1 09 06 07 60 85 74 05 08 01 01 A2 03 02 01 00 A3 05 A1 03 02 01 01 BE 06 04 04 0E 01 06 00 ");

    CoderAare instance = new CoderAare(true, true);
    instance.decodeObject(new SafeReadInputStream(in));
  }

  /**
   * Test of decodeObject method, of class CoderAare.<P>
   *
   * With manually corrected EK240 answer.
   */
  @Test
  public void testDecode_EncodeObject() throws Exception
  {
    System.out.println("decodeObject");

    byte[] bytes = CodingUtils.string2ByteArray("61 42 A1 09 06 07 60 85 74 05 08 01 01 A2 03 02 01 00 A3 05 A1 03 02 01 00 88 02 07 80 89 07"
                                                + "60 85 74 05 08 02 01 AA 0A 80 08 35 35 35 35 35 35 35 35 BE 10 04 0E 08 00 06 5F 1F 04 00 00 00 1D 01"
                                                + "90 FA 00");

    //decode
    InputStream in = new ByteArrayInputStream(bytes);
    CoderAare instance = new CoderAare(true, true);
    OpenResponse result = instance.decodeObject(new SafeReadInputStream(in));

    //encode
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    instance.encodeObject(result, out);

    byte[] apdu = out.toByteArray();

    System.out.println("############ " + CodingUtils.byteArrayToString(apdu));

    assertArrayEquals(bytes, apdu);
  }

  /**
   * Test of decodeObject method, of class CoderAare.<P>
   * With GB example
   *
   */
  @Test
  public void testDecode_EncodeObject2() throws Exception
  {
    System.out.println("decodeObject");

    byte[] bytes = CodingUtils.string2ByteArray(
            "61 29 A1 09 06 07 60 85 74 05 08 01 01 A2 03 02 01 00"
            + "A3 05 A1 03 02 01 00 BE 10 04 0E 08 00 06 5F 1F 04 00"
            + "00 50 1F 01 F4 00 07");

    //decode
    InputStream in = new ByteArrayInputStream(bytes);
    CoderAare instance = new CoderAare(true, true);
    OpenResponse result = instance.decodeObject(new SafeReadInputStream(in));

    //encode
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    instance.encodeObject(result, out);

    assertArrayEquals(bytes, out.toByteArray());
  }

  /**
   * Telegram is wrong.
   */
  public void testDecodeOpenResponseGcm() throws Exception
  {
    System.out.println("open response (high security level)");

//    InputStream in = CodingUtils.string2InputStream("61 81 90 A1 09 06 07 60 85 74 05 08 01 03 A2 03 02 01 00 A3 05 A1 03 02 01 0E 88 02 07 80 89 "
//                                                    + "07 60 85 74 05 08 02 05 AA 42 80 40 46 35 51 42 69 56 4F 36 47 36 32 6B 45 38 6C 64 36 44 4E 52 70 73 "
//                                                    + "77 4E 7A 54 68 5A 41 32 62 53 74 76 71 4E 6A 4B 35 6D 48 63 44 36 63 5A 63 72 45 56 33 69 6E 54 37 58 "
//                                                    + "43 38 7A 63 50 6A 54 78 BE 26 04 24 28 22 30 01 23 45 67 46 85 7F B0 82 5B CA 86 45 00 3D 01 A6 D2 24 "
//                                                    + "34 6C B6 CF 73 36 E9 F1 AE B7 B4 EB 89 21");
//
   InputStream in = CodingUtils.string2InputStream("61 81 9C A1 09 06 07 60 85 74 05 08 01 03 A2 03 02 01 00 A3 05 A1 03 02 01 0E A4 0A 04 08 45 "
   +"4C 53 00 00 3D 2E AF 88 02 07 80 89 07 60 85 74 05 08 02 05 AA 42 80 40 44 5A 71 54 4E 59 55 66 76 78 "
   +"69 50 6F 77 47 50 31 33 66 36 44 4D 76 31 63 33 4B 37 58 50 33 34 56 64 45 4A 41 42 6A 6B 75 61 49 4A "
   +"4D 49 33 6D 64 4B 43 77 72 4A 50 42 65 4F 65 41 56 4B 65 36 BE 26 04 24 28 22 30 01 23 45 67 46 85 7F "
   +"B0 82 5B CA 86 45 00 3D 01 A6 D2 24 34 6C B6 CF 73 36 E9 F1 AE B7 B4 EB 89 21 ");


    CoderAare instance = new CoderAare(true, true);

       

    AbstractCosemServiceInvocation result = instance.decodeObject(in);

    assertNotNull(result);

    assertNull(result.getSecurityControlField());

    OpenResponse openResponse = (OpenResponse)result;



    System.out.println("open response: " + openResponse.toString());
    System.out.println("Initiate response: " + CodingUtils.byteArrayToString(openResponse.getUserInfo()));

    CoderXDlmsApdu coderXDlmsApdu= new CoderXDlmsApdu();

    byte[] encryptionKey = CodingUtils.string2ByteArray("000102030405060708090A0B0C0D0E0F");
    byte[] authenticationKey = CodingUtils.string2ByteArray("D0D1D2D3D4D5D6D7D8D9DADBDCDDDEDF");
    byte[] systemTitle = CodingUtils.string2ByteArray("4D4D4D0000BC614E");
    byte[] systemTitleServer = openResponse.getRespondingApTitle();
    DlmsSecurityProviderGcm securityProvider = new DlmsSecurityProviderGcm(encryptionKey, authenticationKey,
                                                                           systemTitle,
                                                                           systemTitleServer);
    securityProvider.setFrameCounter(19088743);
    coderXDlmsApdu.setSecurityProvider(securityProvider);
    coderXDlmsApdu.decodeObjectFromBytes(openResponse.getUserInfo());
  }


}
