/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elster.dlms.apdu.coding;

import com.elster.coding.CodingUtils;
import com.elster.dlms.cosem.application.services.set.CosemSetRequestWithList;
import com.elster.dlms.types.basic.CosemAttributeDescriptor;
import com.elster.dlms.types.basic.ObisCode;
import com.elster.dlms.types.basic.ServiceInvocationId;
import com.elster.dlms.types.data.DlmsData;
import com.elster.dlms.types.data.DlmsDataOctetString;
import com.elster.dlms.types.data.DlmsDataVisibleString;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author osse
 */
public class CoderSetRequestWithListTest
{
  
      //Example from GB ed7 p.291 (C1 04 removed)
  private final byte testBytes[] = CodingUtils.
            string2ByteArray(
            "81 02 00010000800000FF0200 00010000800100FF0200 02 0932 01020304050607080910111213141516 17181920212223242526272829303132"
            + "33343536373839404142434445464748 4950 0A03 303030");
  
  public CoderSetRequestWithListTest()
  {
  }

  /**
   * Test of encodeObject method, of class CoderSetRequestWithList.
   */
  @Test
  public void testEncodeObject() throws Exception
  {


    System.out.println("encodeObject");
    CosemSetRequestWithList object = new CosemSetRequestWithList();
    
    object.getAttributeDescriptors().add(new CosemAttributeDescriptor(new ObisCode(0x00,0x00,0x80,0x00,0x00,0xFF), 1,2));
    object.getAttributeDescriptors().add(new CosemAttributeDescriptor(new ObisCode(0x00,0x00,0x80,0x01,0x00,0xFF), 1,2));
    object.getValues().add( new DlmsDataOctetString(CodingUtils.string2ByteArray("01020304050607080910111213141516 17181920212223242526272829303132 33343536373839404142434445464748 4950")));
    object.getValues().add( new DlmsDataVisibleString("000"));
    object.setInvocationId(new ServiceInvocationId(0x81));
    
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    final CoderSetRequestWithList instance = new CoderSetRequestWithList();
    instance.encodeObject(object, out);
    
    assertArrayEquals(testBytes, out.toByteArray());

  }

  /**
   * Test of decodeObject method, of class CoderSetRequestWithList.
   */
  @Test
  public void testDecodeObject() throws Exception
  {

    System.out.println("decodeObject");

    final InputStream in = new ByteArrayInputStream(testBytes);
    final CoderSetRequestWithList instance = new CoderSetRequestWithList();
    final CosemSetRequestWithList result = instance.decodeObject(in);
    
    assertEquals(2,result.getAttributeDescriptors().size());
    assertEquals(2,result.getValues().size());
    
    final CosemAttributeDescriptor desriptor1 = result.getAttributeDescriptors().get(0);
    final CosemAttributeDescriptor desriptor2 = result.getAttributeDescriptors().get(1);
    final DlmsData value1 = result.getValues().get(0);
    final DlmsData value2 = result.getValues().get(1);
    
    
    final CosemAttributeDescriptor expectedDescriptor1= new CosemAttributeDescriptor(new ObisCode(0x00,0x00,0x80,0x00,0x00,0xFF), 1,2);
    final CosemAttributeDescriptor expectedDescriptor2= new CosemAttributeDescriptor(new ObisCode(0x00,0x00,0x80,0x01,0x00,0xFF), 1,2);
    
    final DlmsData expectedValue1= new DlmsDataOctetString(CodingUtils.
            string2ByteArray("01020304050607080910111213141516 17181920212223242526272829303132 33343536373839404142434445464748 4950"));
    final DlmsData expectedValue2= new DlmsDataVisibleString("000");
    
    assertEquals(expectedDescriptor1,desriptor1);
    assertEquals(expectedDescriptor2,desriptor2);
    assertEquals(expectedValue1,value1);
    assertEquals(expectedValue2,value2);
  }

}
