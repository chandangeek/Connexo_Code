/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elster.dlms.apdu.coding;

import com.elster.dlms.types.basic.CosemAttributeDescriptor;
import com.elster.dlms.types.basic.ObisCode;
import com.elster.dlms.types.basic.ServiceInvocationId;
import com.elster.dlms.cosem.application.services.get.CosemGetRequest;
import com.elster.dlms.cosem.application.services.get.CosemGetRequestNormal;
import com.elster.coding.CodingUtils;
import com.elster.dlms.types.basic.AccessSelectionParameters;
import com.elster.dlms.types.data.DlmsDataEnum;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author osse
 */
public class CoderGetRequestTest
{
  public CoderGetRequestTest()
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
   * Test of encodeObject method, of class CoderGetRequest.
   */
  @Test
  public void testEncodeObject() throws Exception
  {
    System.out.println("encodeObject");
    //Green Book Ed.7 p.285 (C0 removed)
    byte expecteds[] = CodingUtils.string2ByteArray("01 81 00 01 00 00 80 00 00 FF 02 00");

    ServiceInvocationId serviceInvocationId = new ServiceInvocationId(0x81);
    CosemAttributeDescriptor cosemAttributeDescriptor = new CosemAttributeDescriptor(
            new ObisCode(0x00, 0x00, 0x80, 0x00, 0x00, 0xFF), 1, 2);

    CosemGetRequestNormal object = new CosemGetRequestNormal();
    object.setInvocationId(serviceInvocationId);
    object.setAttributeDescriptor(cosemAttributeDescriptor);

    ByteArrayOutputStream out = new ByteArrayOutputStream();

    CoderGetRequest instance = new CoderGetRequest();

    instance.encodeObject(object, out);
    System.out.println(CodingUtils.byteArrayToString(out.toByteArray()));

    assertArrayEquals(expecteds, out.toByteArray());

  }

  /**
   * Test of encodeObject method, of class CoderGetRequest.
   */
  @Test
  public void testEncodeObjectWithSelectiveAccess() throws Exception
  {
    System.out.println("encodeObject");
    //Green Book Ed.7 p.285 (C0 removed) + Access selection parameters.
    byte expecteds[] = CodingUtils.string2ByteArray("01 81 00 01 00 00 80 00 00 FF 02 01 09 16 05");

    ServiceInvocationId serviceInvocationId = new ServiceInvocationId(0x81);
    CosemAttributeDescriptor cosemAttributeDescriptor = new CosemAttributeDescriptor(
            new ObisCode(0x00, 0x00, 0x80, 0x00, 0x00, 0xFF),1,2,
            new AccessSelectionParameters(9, new DlmsDataEnum(5)));

    CosemGetRequestNormal object = new CosemGetRequestNormal();


    object.setInvocationId(serviceInvocationId);
    object.setAttributeDescriptor(cosemAttributeDescriptor);

    ByteArrayOutputStream out = new ByteArrayOutputStream();

    CoderGetRequest instance = new CoderGetRequest();

    instance.encodeObject(object, out);
    System.out.println(CodingUtils.byteArrayToString(out.toByteArray()));

    assertArrayEquals(expecteds, out.toByteArray());

  }

  /**
   * Test of decodeObject method, of class CoderGetRequest.
   */
  @Test
  public void testDecodeObject() throws Exception
  {
    System.out.println("decodeObject");

    //Green Book Ed.7 p.285 (C0 removed)
    byte inBytes[] = CodingUtils.string2ByteArray("01 81 00 01 00 00 80 00 00 FF 02 00");

    InputStream in = new ByteArrayInputStream(inBytes);
    CoderGetRequest instance = new CoderGetRequest();

    CosemGetRequest result = instance.decodeObject(in);

    assertEquals(0x81, result.getInvocationId().toInteger());
    assertEquals(CosemGetRequest.RequestType.NORMAL, result.getRequestType());
    CosemAttributeDescriptor cosemAttributeDescriptor =
            ((CosemGetRequestNormal)result).getAttributeDescriptor();
    assertEquals(1, cosemAttributeDescriptor.getClassId());
    assertEquals(new ObisCode(0x00, 0x00, 0x80, 0x00, 0x00, 0xFF), cosemAttributeDescriptor.getInstanceId());
    assertEquals(2, cosemAttributeDescriptor.getAttributeId());
    assertEquals(null, cosemAttributeDescriptor.getAccessSelectionParameters());
  }

}
