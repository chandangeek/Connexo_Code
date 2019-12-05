/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elster.dlms.apdu.coding;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import com.elster.dlms.types.basic.ObisCode;
import com.elster.dlms.types.basic.CosemMethodDescriptor;
import com.elster.dlms.types.basic.ServiceInvocationId;
import com.elster.dlms.cosem.application.services.action.CosemActionRequestNormal;
import com.elster.coding.CodingUtils;
import com.elster.axdr.coding.AXdrInputStream;
import com.elster.axdr.coding.AXdrOutputStream;
import com.elster.dlms.cosem.application.services.action.CosemActionRequest;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author osse
 */
public class CoderActionRequestTest
{
  private final byte[] dataActionRequestNormal;

  public CoderActionRequestTest()
  {
    dataActionRequestNormal = CodingUtils.string2ByteArray("01 C1 00 0F AA BB CC DD EE FF 02 00");
  }


  /**
   * Test of encodeObject method, of class CoderActionRequest.
   */
  @Test
  public void testEncodeObject() throws Exception
  {

    System.out.println("encodeObject");
    CosemActionRequestNormal object = new CosemActionRequestNormal();
    object.setInvocationId(new ServiceInvocationId(0xC1));
    object.setMethodDescriptor(new CosemMethodDescriptor(new ObisCode(0xAA, 0xBB, 0xCC, 0xDD, 0xEE, 0xFF), 15,
                                                         2));
    object.setMethodInvocationParamers(null);

    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    AXdrOutputStream out = new AXdrOutputStream(byteArrayOutputStream);
    CoderActionRequest instance = new CoderActionRequest();
    instance.encodeObject(object, out);

    byte result[] = byteArrayOutputStream.toByteArray();

    assertArrayEquals(dataActionRequestNormal, result);

  }

  /**
   * Test of decodeObject method, of class CoderActionRequest.
   */
  @Test
  public void testDecodeObject() throws Exception
  {
    System.out.println("decodeObject");
    AXdrInputStream in = new AXdrInputStream(new ByteArrayInputStream(dataActionRequestNormal));
    CoderActionRequest instance = new CoderActionRequest();
    CosemActionRequest result = instance.decodeObject(in);

    assertEquals(CosemActionRequestNormal.class, result.getClass());
  }

}
