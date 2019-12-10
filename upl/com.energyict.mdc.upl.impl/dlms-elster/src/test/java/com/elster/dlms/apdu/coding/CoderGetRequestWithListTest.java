/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elster.dlms.apdu.coding;

import com.elster.coding.CodingUtils;
import com.elster.dlms.cosem.application.services.get.CosemGetRequest;
import com.elster.dlms.cosem.application.services.get.CosemGetRequestWithList;
import com.elster.dlms.types.basic.CosemAttributeDescriptor;
import com.elster.dlms.types.basic.ObisCode;
import com.elster.dlms.types.basic.ServiceInvocationId;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author osse
 */
public class CoderGetRequestWithListTest
{
  public CoderGetRequestWithListTest()
  {
  }


  /**
   * Test of encodeObject method, of class CoderGetRequestWithList.
   */
  @Test
  public void testEncodeObject() throws Exception
  {
    System.out.println("encodeObject");
    
    //Green Book Ed.7 p.288 (C0 removed)
    final byte expecteds[] = CodingUtils.string2ByteArray("0381 02 00010000800000FF0200 00010000800100FF0200");
    
    final CosemGetRequestWithList object = new CosemGetRequestWithList();
    object.setInvocationId(new ServiceInvocationId(0x81));
    object.getAttributeDescriptors().add(new CosemAttributeDescriptor(new ObisCode(0x00,0x00,0x80,0x00,0x00,0xFF), 1, 02));
    object.getAttributeDescriptors().add(new CosemAttributeDescriptor(new ObisCode(0x00,0x00,0x80,0x01,0x00,0xFF), 1, 02));
    
    
    final ByteArrayOutputStream out = new ByteArrayOutputStream();

    final CoderGetRequest instance = new CoderGetRequest();

    instance.encodeObject(object, out);

    assertArrayEquals(expecteds, out.toByteArray());
  }

  /**
   * Test of decodeObject method, of class CoderGetRequestWithList.
   */
  @Test
  public void testDecodeObject() throws Exception
  {
    System.out.println("decodeObject");
    
    //Green Book Ed.7 p.288 (C0 removed)
    final byte inBytes[] = CodingUtils.string2ByteArray("0381 02 00010000800000FF0200 00010000800100FF0200");

    final InputStream in = new ByteArrayInputStream(inBytes);
    final CoderGetRequest instance = new CoderGetRequest();

    final CosemGetRequest result = instance.decodeObject(in);

    assertTrue(result instanceof CosemGetRequestWithList);
    
    final CosemGetRequestWithList resultGetRequestWithList= (CosemGetRequestWithList)result;
    
    assertEquals(0x81,resultGetRequestWithList.getInvocationId().toInteger());
    assertEquals(2,resultGetRequestWithList.getAttributeDescriptors().size());
    assertEquals(new CosemAttributeDescriptor(new ObisCode(0x00,0x00,0x80,0x00,0x00,0xFF), 1, 02),resultGetRequestWithList.getAttributeDescriptors().get(0));
    assertEquals(new CosemAttributeDescriptor(new ObisCode(0x00,0x00,0x80,0x01,0x00,0xFF), 1, 02),resultGetRequestWithList.getAttributeDescriptors().get(1));
  }
  
}
