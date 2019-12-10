/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elster.dlms.apdu.coding;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import com.elster.axdr.coding.AXdrInputStream;
import com.elster.axdr.coding.AXdrOutputStream;
import com.elster.dlms.cosem.application.services.action.ActionResponse;
import com.elster.dlms.cosem.application.services.action.ActionResult;
import com.elster.dlms.cosem.application.services.action.CosemActionResponse;
import com.elster.dlms.cosem.application.services.action.CosemActionResponseNormal;
import com.elster.dlms.types.basic.ServiceInvocationId;
import com.elster.coding.CodingUtils;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author osse
 */
public class CoderActionResponseTest
{
  private final byte dataResponseNormal[];

  public CoderActionResponseTest()
  {
    dataResponseNormal = CodingUtils.string2ByteArray("01 C1 00 00");
  }

  /**
   * Test of encodeObject method, of class CoderActionResponse.
   */
  @Test
  public void testEncodeObject() throws Exception
  {
    System.out.println("encodeObject");
    CosemActionResponseNormal object = new CosemActionResponseNormal();

    object.setInvocationId(new ServiceInvocationId(0xC1));
    object.setActionResponseWithOptionalData(new ActionResponse(ActionResult.SUCCESS,null));

    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    AXdrOutputStream out = new AXdrOutputStream(byteArrayOutputStream);
    CoderActionResponse instance = new CoderActionResponse();
    instance.encodeObject(object, out);

    byte result[] = byteArrayOutputStream.toByteArray();

    assertArrayEquals(dataResponseNormal, result);
  }

  /**
   * Test of decodeObject method, of class CoderActionResponse.
   */
  @Test
  public void testDecodeObject() throws Exception
  {

    System.out.println("decodeObject");
    AXdrInputStream in = new AXdrInputStream(new ByteArrayInputStream(dataResponseNormal));
    CoderActionResponse instance = new CoderActionResponse();
    CosemActionResponse result = instance.decodeObject(in);
    assertEquals(CosemActionResponseNormal.class, result.getClass());

    CosemActionResponseNormal actionResponseNormal = (CosemActionResponseNormal)result;
    assertEquals(ActionResult.SUCCESS, actionResponseNormal.getActionResponseWithOptionalData().getActionResult());
    assertEquals(null, actionResponseNormal.getActionResponseWithOptionalData().getGetDataResult());
    assertEquals(0xC1, actionResponseNormal.getInvocationId().toInteger());
  }

}
