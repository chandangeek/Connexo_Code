/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elster.dlms.apdu.coding;

import com.elster.dlms.cosem.application.services.get.CosemGetResponse;
import com.elster.dlms.cosem.application.services.get.CosemGetResponseNormal;
import com.elster.dlms.types.data.DlmsData;
import com.elster.dlms.types.data.DlmsDataOctetString;
import com.elster.coding.CodingUtils;
import com.elster.dlms.cosem.application.services.common.DataAccessResult;
import com.elster.dlms.cosem.application.services.get.GetDataResult;
import com.elster.dlms.types.basic.ServiceInvocationId;
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
public class CoderGetResponseTest
{
  public CoderGetResponseTest()
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
   * Test of encodeObject method, of class CoderGetResponse.
   */
  @Test
  public void testEncodeObject() throws Exception
  {
    byte octetPart[] = CodingUtils.string2ByteArray(
            "01020304050607080910111213141516"
            + "17181920212223242526272829303132"
            + "33343536373839404142434445464748"
            + "4950");

    byte expecteds[] = CodingUtils.string2ByteArray(
            "0181"
            + "00"
            + "0932"
            + "01020304050607080910111213141516"
            + "17181920212223242526272829303132"
            + "33343536373839404142434445464748"
            + "4950");

    System.out.println("encodeObject");
    CosemGetResponseNormal object = new CosemGetResponseNormal();
    object.setGetDataResult(new GetDataResult(DataAccessResult.SUCCESS, new DlmsDataOctetString(octetPart)));
    object.setInvocationId(new ServiceInvocationId(0x81));


    ByteArrayOutputStream out = new ByteArrayOutputStream();
    CoderGetResponse instance = new CoderGetResponse();
    instance.encodeObject(object, out);

    assertArrayEquals(expecteds, out.toByteArray());
  }

  /**
   * Test of decodeObject method, of class CoderGetResponse.
   */
  @Test
  public void testDecodeObject() throws Exception
  {
    System.out.println("decodeObject");

    //Green Book Ed.7 p.285 (C4 removed)
    byte inBytes[] = CodingUtils.string2ByteArray(
            "0181"
            + "00"
            + "0932"
            + "01020304050607080910111213141516"
            + "17181920212223242526272829303132"
            + "33343536373839404142434445464748"
            + "4950");

    byte octetPart[] = CodingUtils.string2ByteArray(
            "01020304050607080910111213141516"
            + "17181920212223242526272829303132"
            + "33343536373839404142434445464748"
            + "4950");


    InputStream in = new ByteArrayInputStream(inBytes);
    CoderGetResponse instance = new CoderGetResponse();

    CosemGetResponse result = instance.decodeObject(in);

    assertEquals(0x81, result.getInvocationId().toInteger());
    assertEquals(CosemGetResponse.ResponseType.NORMAL, result.getResponseType());
    assertEquals(0x81, result.getInvocationId().toInteger());

    //Response type
    CosemGetResponseNormal getResponseNormal = (CosemGetResponseNormal)result;

    //Access result
    assertEquals(0, getResponseNormal.getGetDataResult().getAccessResult().getId());
    assertNotNull(getResponseNormal.getGetDataResult().getData());

    //Data
    assertEquals(DlmsData.DataType.OCTET_STRING, getResponseNormal.getGetDataResult().getData().getType());
    DlmsDataOctetString octetString = (DlmsDataOctetString)(getResponseNormal.getGetDataResult().getData());
    assertArrayEquals(octetPart, octetString.getValue());
  }

}
