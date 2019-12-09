/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elster.dlms.apdu.coding;

import com.elster.dlms.types.basic.ObisCode;
import com.elster.axdr.coding.AXdrInputStream;
import com.elster.axdr.coding.AXdrOutputStream;
import com.elster.dlms.cosem.application.services.action.CosemActionRequestNormal;
import com.elster.dlms.types.basic.CosemMethodDescriptor;
import com.elster.dlms.types.basic.ServiceInvocationId;
import com.elster.coding.CodingUtils;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author osse
 */
public class CoderActionRequestNormalTest
{
  private final byte[] testData;

  public CoderActionRequestNormalTest()
  {
    testData = CodingUtils.string2ByteArray("C1 00 0F AA BB CC DD EE FF 02 00");
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
   * Test of encodeObject method, of class CoderActionRequestNormal.
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
    CoderActionRequestNormal instance = new CoderActionRequestNormal();
    instance.encodeObject(object, out);

    byte result[] = byteArrayOutputStream.toByteArray();

    assertArrayEquals(testData, result);
  }

  /**
   * Test of decodeObject method, of class CoderActionRequestNormal.
   */
  @Test
  public void testDecodeObject() throws Exception
  {
    System.out.println("decodeObject");
    AXdrInputStream in = new AXdrInputStream(new ByteArrayInputStream(testData));
    CoderActionRequestNormal instance = new CoderActionRequestNormal();
    CosemActionRequestNormal result = instance.decodeObject(in);

    CosemMethodDescriptor methodDescriptor = result.getMethodDescriptor();
    assertEquals(new ObisCode(0xAA, 0xBB, 0xCC, 0xDD, 0xEE, 0xFF), methodDescriptor.getInstanceId());
    assertEquals(15, methodDescriptor.getClassId());
    assertEquals(2, methodDescriptor.getMethodId());
    assertEquals(null, result.getMethodInvocationParamers());
    assertEquals(new ServiceInvocationId(0xC1), result.getInvocationId());
  }

}
