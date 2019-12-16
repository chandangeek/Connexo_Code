/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elster.dlms.cosem.classes.info;

import com.elster.dlms.cosem.classes.common.CosemClassIds;
import com.elster.dlms.cosem.classes.info.CosemAttributeInfo.OctetStringType;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author osse
 */
public class CosemAttributeInfoTest
{
  public CosemAttributeInfoTest()
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
   * Test of getName method, of class CosemAttributeInfo.
   */
  @Test
  public void testGetName()
  {
    System.out.println("getName");
    CosemAttributeInfo instance = CosemClassInfos.getInstance().getAttributeInfo(CosemClassIds.DATA, 0,
                                                                                 2);
    String expResult = "data";
    String result = instance.getName();
    assertEquals(expResult, result);
  }

  /**
   * Test of getId method, of class CosemAttributeInfo.
   */
  @Test
  public void testGetId()
  {
    System.out.println("getId");
    CosemAttributeInfo instance = CosemClassInfos.getInstance().getAttributeInfo(CosemClassIds.DATA, 0,
                                                                                 2);
    int expResult = 2;
    int result = instance.getId();
    assertEquals(expResult, result);
  }

  /**
   * Test of getOctetStringType method, of class CosemAttributeInfo.
   */
  @Test
  public void testGetOctetStringType()
  {
    System.out.println("getOctetStringType");
    CosemAttributeInfo instance = CosemClassInfos.getInstance().getAttributeInfo(CosemClassIds.CLOCK, 0,
                                                                                 2);
    OctetStringType expResult = OctetStringType.DATE_TIME;
    OctetStringType result = instance.getOctetStringType();
    assertEquals(expResult, result);
  }
  
  /**
   * Test of getOctetStringType method, of class CosemAttributeInfo.
   */
  @Test
  public void testGetOctetStringType2()
  {
    System.out.println("getOctetStringType 2");
    CosemAttributeInfo instance = CosemClassInfos.getInstance().getAttributeInfo(CosemClassIds.CLOCK, 0,
                                                                                 3);
    OctetStringType expResult = OctetStringType.OCTETS;
    OctetStringType result = instance.getOctetStringType();
    assertEquals("A default of OCTETS is expected",expResult, result); //since 2012-11-22
  }
  
  
}
