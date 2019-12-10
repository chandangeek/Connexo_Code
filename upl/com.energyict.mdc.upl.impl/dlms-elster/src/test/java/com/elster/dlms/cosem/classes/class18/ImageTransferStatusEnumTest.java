/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elster.dlms.cosem.classes.class18;

import com.elster.dlms.cosem.classes.common.CosemEnumFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author osse
 */
public class ImageTransferStatusEnumTest
{
  public ImageTransferStatusEnumTest()
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
   * Test of getFactory method, of class ImageTransferStatusEnum.<P>
   * Test if all values could be found.
   *
   */
  @Test
  public void testGetFactory()
  {
    System.out.println("getFactory");
    CosemEnumFactory instance =
            ImageTransferStatusEnum.getFactory();
    for (ImageTransferStatusEnum e : ImageTransferStatusEnum.values())
    {
      assertEquals(e, instance.findValue(e.getId()));
      assertEquals(e.getId(), instance.findValue(e.getId()).getId());
    }
    assertNull(instance.findValue(100000));
    assertNotNull(instance.findValueWithDefault(100000));
  }
}
