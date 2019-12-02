/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elster.dlms.apdu.coding;

import com.elster.coding.CodingUtils;
import com.elster.dlms.cosem.application.services.common.DataAccessResult;
import com.elster.dlms.cosem.application.services.set.CosemSetResponseWithList;
import com.elster.dlms.types.basic.ServiceInvocationId;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author osse
 */
public class CoderSetResponseWithListTest
{
  public CoderSetResponseWithListTest()
  {
  }

  /**
   * Test of encodeObject method, of class CoderSetResponseWithList.
   */
  @Test
  public void testEncodeObject() throws Exception
  {
    System.out.println("encodeObject");
    final CosemSetResponseWithList object = new CosemSetResponseWithList();
    object.setInvocationId(new ServiceInvocationId(0x81));
    object.getDataAccessResults().add(DataAccessResult.SUCCESS);
    object.getDataAccessResults().add(DataAccessResult.SUCCESS);
    
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    final CoderSetResponseWithList instance = new CoderSetResponseWithList();
    instance.encodeObject(object, out);
    
    assertArrayEquals(CodingUtils.string2ByteArray("81 02 00 00"), out.toByteArray());
  }

  /**
   * Test of decodeObject method, of class CoderSetResponseWithList.
   */
  @Test
  public void testDecodeObject() throws Exception
  {
    System.out.println("decodeObject");
    //Example from GB ed.7 p.291 ("C505" removed)
    final InputStream in = CodingUtils.string2InputStream("81 02 00 00");
    final CoderSetResponseWithList instance = new CoderSetResponseWithList();
    final CosemSetResponseWithList result = instance.decodeObject(in);
    
    assertEquals(2, result.getDataAccessResults().size());
    assertEquals(DataAccessResult.SUCCESS ,  result.getDataAccessResults().get(0));
    assertEquals(DataAccessResult.SUCCESS ,  result.getDataAccessResults().get(1));
  }
  
}
