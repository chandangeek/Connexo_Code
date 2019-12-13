/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elster.dlms.apdu.coding;

import com.elster.coding.CodingUtils;
import com.elster.dlms.cosem.application.services.action.CosemActionResponseNormal;
import com.elster.dlms.cosem.application.services.common.AbstractCosemServiceInvocation;
import com.elster.dlms.cosem.application.services.common.DataAccessResult;
import com.elster.dlms.cosem.application.services.common.SecurityControlField;
import com.elster.dlms.cosem.application.services.get.CosemGetRequest;
import com.elster.dlms.cosem.application.services.get.CosemGetRequestNormal;
import com.elster.dlms.cosem.application.services.open.DlmsConformance;
import com.elster.dlms.cosem.application.services.open.OpenRequest;
import com.elster.dlms.cosem.application.services.open.ProposedXDlmsContext;
import com.elster.dlms.cosem.application.services.set.CosemSetRequestWithList;
import com.elster.dlms.cosem.application.services.set.CosemSetResponseWithList;
import com.elster.dlms.definitions.DlmsOids;
import com.elster.dlms.security.CipherException;
import com.elster.dlms.security.DlmsSecurityProviderGcm;
import com.elster.dlms.security.IDlmsSecurityProvider;
import com.elster.dlms.security.IDlmsSecurityProvider.DecodingResult;
import com.elster.dlms.testutils.TestUtils;
import com.elster.dlms.types.basic.CosemAttributeDescriptor;
import com.elster.dlms.types.basic.ObisCode;
import com.elster.dlms.types.basic.ServiceInvocationId;
import com.elster.dlms.types.data.DlmsDataOctetString;
import com.elster.dlms.types.data.DlmsDataVisibleString;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.EnumSet;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author osse
 */
public class CoderXDlmsApduTest
{
  public CoderXDlmsApduTest()
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

  private IDlmsSecurityProvider createSecurityProvider()
  {

    byte[] encryptionKey = CodingUtils.string2ByteArray("000102030405060708090A0B0C0D0E0F");
    byte[] authenticationKey = CodingUtils.string2ByteArray("D0D1D2D3D4D5D6D7D8D9DADBDCDDDEDF");
    byte[] systemTitle = CodingUtils.string2ByteArray("4D4D4D0000BC614E");
    DlmsSecurityProviderGcm securityProvider = new DlmsSecurityProviderGcm(encryptionKey, authenticationKey,
                                                                           systemTitle,
                                                                           systemTitle);
    securityProvider.setFrameCounter(0x01234567L);
    return securityProvider;
  }

  private CosemGetRequestNormal createGetRequestNormal()
  {
    ServiceInvocationId serviceInvocationId = new ServiceInvocationId(0x00);
    CosemGetRequestNormal getRequest = new CosemGetRequestNormal();

    CosemAttributeDescriptor cosemAttributeDescriptor = new CosemAttributeDescriptor(
            new ObisCode("0.0.1.0.0.255"), 8, 2);

    getRequest.setInvocationId(serviceInvocationId);
    getRequest.setAttributeDescriptor(cosemAttributeDescriptor);
    return getRequest;

  }

  /**
   * Test of encodeObject method, of class CoderXDlmsApdu.
   */
  @Test
  public void testEncodeGetRequestNormal() throws Exception
  {
    System.out.println("encodeObject (get request, no security)");
    //GB ed.7 p.285 
    byte expecteds[] = CodingUtils.string2ByteArray("C0 01 81 00 01 00 00 80 00 00 FF 02 00");

    ServiceInvocationId serviceInvocationId = new ServiceInvocationId(0x81);
    CosemAttributeDescriptor cosemAttributeDescriptor = new CosemAttributeDescriptor(
            new ObisCode(0x00, 0x00, 0x80, 0x00, 0x00, 0xFF), 1, 2);
    CosemGetRequestNormal object = new CosemGetRequestNormal();

    object.setInvocationId(serviceInvocationId);
    object.setAttributeDescriptor(cosemAttributeDescriptor);
    ByteArrayOutputStream out = new ByteArrayOutputStream();

    CoderXDlmsApdu instance = new CoderXDlmsApdu();
    instance.encodeObject(object, out);

    assertArrayEquals(out.toByteArray(), expecteds);
  }

  /**
   * Test of encodeObject method, of class CoderXDlmsApdu.
   */
  @Test
  public void testEncodeGetRequestNormal2() throws Exception
  {
    System.out.println("encodeObject (get request, no security)");
    //GB ed.7 p.285 
    byte expecteds[] = CodingUtils.string2ByteArray("C0 01 00 00 08 00 00 01 00 00 FF 02 00");

    CosemGetRequestNormal getRequest = createGetRequestNormal();

    ByteArrayOutputStream out = new ByteArrayOutputStream();

    CoderXDlmsApdu instance = new CoderXDlmsApdu();
    instance.encodeObject(getRequest, out);

    assertArrayEquals(out.toByteArray(), expecteds);
  }

  /**
   * Test of encodeObject method, of class CoderXDlmsApdu.
   */
  @Test
  public void testEncodeGetRequestNormalAuthenticated() throws Exception
  {
    System.out.println("encodeObject (get request, authenticated)");
    //GB ed.7 p.285
    byte expecteds[] = CodingUtils.string2ByteArray(
            "C81E1001234567C0010000080000010000FF020006725D910F9221D263877516");

    CosemGetRequestNormal getRequest = createGetRequestNormal();
    getRequest.setSecurityControlField(
            new SecurityControlField(0, true, false, SecurityControlField.CipheringMethod.GLOBAL_UNICAST));

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    CoderXDlmsApdu instance = new CoderXDlmsApdu();

    IDlmsSecurityProvider securityProvider = createSecurityProvider();

    instance.setSecurityProvider(securityProvider);
    instance.encodeObject(getRequest, out);

    assertArrayEquals(out.toByteArray(), expecteds);
  }

  /**
   * Test of encodeObject method, of class CoderXDlmsApdu.
   */
  @Test
  public void testEncodeGetRequestNormalEncrypted() throws Exception
  {
    System.out.println("encodeObject (get request, encrypted)");
    //GB ed.7 p.285
    byte expecteds[] = CodingUtils.string2ByteArray(
            "C8122001234567411312FF935A47566827C467BC");

    CosemGetRequestNormal getRequest = createGetRequestNormal();
    getRequest.setSecurityControlField(
            new SecurityControlField(0, false, true, SecurityControlField.CipheringMethod.GLOBAL_UNICAST));


    ByteArrayOutputStream out = new ByteArrayOutputStream();
    CoderXDlmsApdu instance = new CoderXDlmsApdu();

    IDlmsSecurityProvider securityProvider = createSecurityProvider();

    instance.setSecurityProvider(securityProvider);
    instance.encodeObject(getRequest, out);

    assertArrayEquals(out.toByteArray(), expecteds);
  }

  /**
   * Test of encodeObject method, of class CoderXDlmsApdu.
   */
  @Test
  public void testEncodeGetRequestNormalAuthenticatedEncrypted() throws Exception
  {
    System.out.println("encodeObject (get request, autheticated and encrypted)");
    //GB ed.7 p.285
    byte expecteds[] = CodingUtils.string2ByteArray(
            "C81E3001234567411312FF935A47566827C467BC7D825C3BE4A77C3FCC056B6B");

    CosemGetRequestNormal getRequest = createGetRequestNormal();
    getRequest.setSecurityControlField(
            new SecurityControlField(0, true, true, SecurityControlField.CipheringMethod.GLOBAL_UNICAST));


    ByteArrayOutputStream out = new ByteArrayOutputStream();
    CoderXDlmsApdu instance = new CoderXDlmsApdu();

    IDlmsSecurityProvider securityProvider = createSecurityProvider();

    instance.setSecurityProvider(securityProvider);
    instance.encodeObject(getRequest, out);

    assertArrayEquals(out.toByteArray(), expecteds);
  }

  /**
   * Test of decodeObject method, of class CoderXDlmsApdu.
   */
  @Test
  public void testDecodeActionResponseNormal() throws Exception
  {
    System.out.println("decodeObject (action response, no security");

    InputStream in = CodingUtils.string2InputStream(
            "C7 01 12 00 01 00 02 02 11 00 0A 13 32 30 31 32 2D 30 36 2D 32 38 2C 30 30 3A 35 30 3A 31 34");
    CoderXDlmsApdu instance = new CoderXDlmsApdu();
    AbstractCosemServiceInvocation result = instance.decodeObject(in);

    assertTrue(result instanceof CosemActionResponseNormal);
    assertNotNull(((CosemActionResponseNormal)result).getActionResponseWithOptionalData().getGetDataResult().
            getData());
  }

  /**
   * Test of decodeObject method, of class CoderXDlmsApdu.
   */
  @Test
  public void testDecodeGetRequestNormal() throws Exception
  {
    System.out.println("decodeObject (get request, no security");

    InputStream in = CodingUtils.string2InputStream("C0 01 81 00 01 00 00 80 00 00 FF 02 00");
    CoderXDlmsApdu instance = new CoderXDlmsApdu();
    AbstractCosemServiceInvocation result = instance.decodeObject(in);

    assertTrue(result instanceof CosemGetRequestNormal);

    CosemGetRequestNormal getRequestNormal = (CosemGetRequestNormal)result;

    assertEquals(0x81, getRequestNormal.getInvocationId().toInteger());
    assertEquals(CosemGetRequest.RequestType.NORMAL, getRequestNormal.getRequestType());
    CosemAttributeDescriptor cosemAttributeDescriptor =
            ((CosemGetRequestNormal)result).getAttributeDescriptor();
    assertEquals(1, cosemAttributeDescriptor.getClassId());
    assertEquals(new ObisCode(0x00, 0x00, 0x80, 0x00, 0x00, 0xFF), cosemAttributeDescriptor.getInstanceId());
    assertEquals(2, cosemAttributeDescriptor.getAttributeId());
    assertEquals(null, cosemAttributeDescriptor.getAccessSelectionParameters());
  }

  /**
   * Test of decodeObject method, of class CoderXDlmsApdu.
   */
  @Test
  public void testDecodeSetRequestWithList() throws Exception
  {
    System.out.println("decodeObject (set request with list, no security");

    //Example from GB ed7 p.291 
    final InputStream in = CodingUtils.
            string2InputStream(
            "C1 04 81 02 00010000800000FF0200 00010000800100FF0200 02 0932 01020304050607080910111213141516 17181920212223242526272829303132"
            + "33343536373839404142434445464748 4950 0A03 303030");
    final CoderXDlmsApdu instance = new CoderXDlmsApdu();
    final AbstractCosemServiceInvocation result = instance.decodeObject(in);

    assertTrue(result instanceof CosemSetRequestWithList);

    assertEquals(2, ((CosemSetRequestWithList)result).getValues().size());
    assertEquals("000", ((CosemSetRequestWithList)result).getValues().get(1).stringValue());
    // The complete result is tested in the CoderSetRequestWithListTest
  }

  /**
   * Test of encodeObject method, of class CoderSetRequestWithList.
   */
  @Test
  public void testEncodeObject() throws Exception
  {

     //Example from GB ed7 p.291 

    System.out.println("encodeObject");
    CosemSetRequestWithList object = new CosemSetRequestWithList();

    object.getAttributeDescriptors().add(new CosemAttributeDescriptor(new ObisCode(0x00, 0x00, 0x80, 0x00,
                                                                                   0x00, 0xFF), 1, 2));
    object.getAttributeDescriptors().add(new CosemAttributeDescriptor(new ObisCode(0x00, 0x00, 0x80, 0x01,
                                                                                   0x00, 0xFF), 1, 2));
    object.getValues().add(
            new DlmsDataOctetString(
            CodingUtils.string2ByteArray(
            "01020304050607080910111213141516 17181920212223242526272829303132 33343536373839404142434445464748 4950")));
    object.getValues().add(new DlmsDataVisibleString("000"));
    object.setInvocationId(new ServiceInvocationId(0x81));

    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    CoderXDlmsApdu instance = new CoderXDlmsApdu();
    instance.encodeObject(object, out);
    byte[] expects =
            CodingUtils.
            string2ByteArray(
            "C1 04 81 02 00010000800000FF0200 00010000800100FF0200 02 0932 01020304050607080910111213141516 17181920212223242526272829303132"
            + "33343536373839404142434445464748 4950 0A03 303030");
    assertArrayEquals(expects, out.toByteArray());
  }

  /**
   * Test of decodeObject method, of class CoderSetResponseWithList.
   */
  @Test
  public void testDecodeObject() throws Exception
  {
    System.out.println("decodeObject");
    //Example from GB ed.7 p.291 
    final InputStream in = CodingUtils.string2InputStream("C505 81 02 00 00");
    final CoderXDlmsApdu instance = new CoderXDlmsApdu();
    AbstractCosemServiceInvocation result = instance.decodeObject(in);

    assertTrue(result instanceof CosemSetResponseWithList);

    CosemSetResponseWithList setResponseWithList = (CosemSetResponseWithList)result;

    assertEquals(2, setResponseWithList.getDataAccessResults().size());
    assertEquals(DataAccessResult.SUCCESS, setResponseWithList.getDataAccessResults().get(0));
    assertEquals(DataAccessResult.SUCCESS, setResponseWithList.getDataAccessResults().get(1));
  }

  /**
   * Test of decodeObject method, of class CoderXDlmsApdu.
   */
  @Test
  public void testDecodeGetRequestNormalAuthenticated() throws Exception
  {
    System.out.println("decodeObject (get request, authenticated");

    InputStream in = CodingUtils.string2InputStream(
            "C81E1001234567C0010000080000010000FF020006725D910F9221D263877516");
    CoderXDlmsApdu instance = new CoderXDlmsApdu();
    instance.setSecurityProvider(createSecurityProvider());

    AbstractCosemServiceInvocation result = instance.decodeObject(in);

    assertTrue(result instanceof CosemGetRequestNormal);
    assertEquals(new SecurityControlField(0, true, false, SecurityControlField.CipheringMethod.GLOBAL_UNICAST),
                 result.getSecurityControlField());

    CosemGetRequestNormal getRequestNormal = (CosemGetRequestNormal)result;

    assertEquals(0x00, getRequestNormal.getInvocationId().toInteger());
    assertEquals(CosemGetRequest.RequestType.NORMAL, getRequestNormal.getRequestType());
    CosemAttributeDescriptor cosemAttributeDescriptor =
            ((CosemGetRequestNormal)result).getAttributeDescriptor();
    assertEquals(8, cosemAttributeDescriptor.getClassId());
    assertEquals(new ObisCode("0.0.1.0.0.255"), cosemAttributeDescriptor.getInstanceId());
    assertEquals(2, cosemAttributeDescriptor.getAttributeId());
    assertEquals(null, cosemAttributeDescriptor.getAccessSelectionParameters());
  }

  /**
   * Test of decodeObject method, of class CoderXDlmsApdu.
   */
  @Test(expected = CipherException.class)
  public void testDecodeGetRequestNormalAuthenticatedFailure() throws Exception
  {
    System.out.println("decodeObject (get request, authenticated, exception expected.)");

    InputStream in = CodingUtils.string2InputStream(
            "C81E1001234567C0010000080000010000FF020006725D910F9221D263878516");//(manipulated)
    CoderXDlmsApdu instance = new CoderXDlmsApdu();
    instance.setSecurityProvider(createSecurityProvider());

    AbstractCosemServiceInvocation result = instance.decodeObject(in);
    assertNotNull(result);
  }

  /**
   * Test of decodeObject method, of class CoderXDlmsApdu.
   */
  @Test
  public void testDecodeGetRequestNormalEncrypted() throws Exception
  {
    System.out.println("decodeObject (get request, encrypted)");

    InputStream in = CodingUtils.string2InputStream("C8122001234567411312FF935A47566827C467BC");
    CoderXDlmsApdu instance = new CoderXDlmsApdu();
    instance.setSecurityProvider(createSecurityProvider());

    AbstractCosemServiceInvocation result = instance.decodeObject(in);

    assertTrue(result instanceof CosemGetRequestNormal);

    assertEquals(new SecurityControlField(0, false, true, SecurityControlField.CipheringMethod.GLOBAL_UNICAST),
                 result.getSecurityControlField());


    CosemGetRequestNormal getRequestNormal = (CosemGetRequestNormal)result;

    assertEquals(0x00, getRequestNormal.getInvocationId().toInteger());
    assertEquals(CosemGetRequest.RequestType.NORMAL, getRequestNormal.getRequestType());
    CosemAttributeDescriptor cosemAttributeDescriptor =
            ((CosemGetRequestNormal)result).getAttributeDescriptor();
    assertEquals(8, cosemAttributeDescriptor.getClassId());
    assertEquals(new ObisCode("0.0.1.0.0.255"), cosemAttributeDescriptor.getInstanceId());
    assertEquals(2, cosemAttributeDescriptor.getAttributeId());
    assertEquals(null, cosemAttributeDescriptor.getAccessSelectionParameters());
  }

  /**
   * Test of decodeObject method, of class CoderXDlmsApdu.
   */
  @Test
  public void testDecodeGetRequestNormalAuthenticatedEncrypted() throws Exception
  {
    System.out.println("decodeObject (get request,authenticated and encrypted)");

    InputStream in = CodingUtils.string2InputStream(
            "C81E3001234567411312FF935A47566827C467BC7D825C3BE4A77C3FCC056B6B");
    CoderXDlmsApdu instance = new CoderXDlmsApdu();
    instance.setSecurityProvider(createSecurityProvider());

    AbstractCosemServiceInvocation result = instance.decodeObject(in);

    assertTrue(result instanceof CosemGetRequestNormal);

    assertEquals(new SecurityControlField(0, true, true, SecurityControlField.CipheringMethod.GLOBAL_UNICAST),
                 result.getSecurityControlField());

    CosemGetRequestNormal getRequestNormal = (CosemGetRequestNormal)result;

    assertEquals(0x00, getRequestNormal.getInvocationId().toInteger());
    assertEquals(CosemGetRequest.RequestType.NORMAL, getRequestNormal.getRequestType());
    CosemAttributeDescriptor cosemAttributeDescriptor =
            ((CosemGetRequestNormal)result).getAttributeDescriptor();
    assertEquals(8, cosemAttributeDescriptor.getClassId());
    assertEquals(new ObisCode("0.0.1.0.0.255"), cosemAttributeDescriptor.getInstanceId());
    assertEquals(2, cosemAttributeDescriptor.getAttributeId());
    assertEquals(null, cosemAttributeDescriptor.getAccessSelectionParameters());
  }

  /**
   * Test of decodeObject method, of class CoderXDlmsApdu.
   */
  @Test(expected = CipherException.class)
  public void testDecodeGetRequestNormalAuthenticatedEncryptedFailure() throws Exception
  {
    System.out.println("decodeObject (get request,authenticated and encrypted, failure expected)");

    InputStream in = CodingUtils.string2InputStream(
            "C81E3001234567411312FF935A47566827C467BC7D825C3BE4A77C3FCC05666B"); //manipulated
    CoderXDlmsApdu instance = new CoderXDlmsApdu();
    instance.setSecurityProvider(createSecurityProvider());

    AbstractCosemServiceInvocation result = instance.decodeObject(in);
    assertNotNull(result);
  }

  /**
   * Test of encodeObject method, of class CoderXDlmsApdu.
   */
  @Test
  public void testEncodeDecodeAARQGCM() throws Exception
  {
    System.out.println("encodeObject, decodeObject (AARQ for GCM)");

    CoderXDlmsApdu instance = new CoderXDlmsApdu();
    IDlmsSecurityProvider securityProvider = createSecurityProvider();
    instance.setSecurityProvider(securityProvider);


    //>>>>>>>>
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

    final OpenRequest openRequest = new OpenRequest();
    openRequest.setAcseRequirements(OpenRequest.ASCE_AUTHENTICATION);
    openRequest.setApplicationContextName(DlmsOids.DLMS_UA_AC_LN_WC);
    openRequest.setCallingApTitle(CodingUtils.string2ByteArray("11 22 33 44 55 66 77 88"));
    openRequest.setCallingAuthenticationValue(securityProvider.buildCallingAuthenticationValue());

    openRequest.setSecurityMechanismName(
            DlmsOids.DLMS_UA_AMN_COSEM_HIGH_LEVEL_SECURITY_MECHANISM_NAME_USING_GMAC);

    openRequest.setProposedXDlmsContext(proposedXDlmsContext);


    openRequest.setUserInfo(instance.encodeObjectToBytes(proposedXDlmsContext));
    //<<<<

    CoderAarq coderAarq = new CoderAarq();

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    coderAarq.encodeObject(openRequest, out);

    ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
    AbstractCosemServiceInvocation serviceInvocation = coderAarq.decodeObject(in);

    assertEquals(AbstractCosemServiceInvocation.ServiceType.OPEN, serviceInvocation.getServiceType());
    OpenRequest openRequestDecoded = (OpenRequest)serviceInvocation;

    System.out.println("Encrypted user info:"
                       + CodingUtils.byteArrayToString(openRequestDecoded.getUserInfo()));

    DecodingResult decodingResult =
            securityProvider.decode(CodingUtils.copyOfRange(openRequestDecoded.getUserInfo(), 2,
                                                       openRequestDecoded.getUserInfo().length),
                                    SecurityControlField.CipheringMethod.GLOBAL_UNICAST);
    System.out.println("Decrypted user info:" + CodingUtils.byteArrayToString(decodingResult.getData()));

    assertNotNull(openRequestDecoded.getProposedXDlmsContext());
    assertTrue(decodingResult.getData().length > 3);
    assertNotSame(0x01, decodingResult.getData()[2]);// Ensure that doubled leading tag bug is solved.
  }

  @Test(expected = IOException.class)
  public void testWrongFrame001() throws IOException
  {
    System.out.println("decodeObject, wrong frame 001");
    CoderXDlmsApdu instance = new CoderXDlmsApdu();
    instance.decodeObject(
            TestUtils.hexResourceFile2InputStream("/com/elster/testfiles/wrongframe001.txt"));
    fail("Exception expected");
  }

}
