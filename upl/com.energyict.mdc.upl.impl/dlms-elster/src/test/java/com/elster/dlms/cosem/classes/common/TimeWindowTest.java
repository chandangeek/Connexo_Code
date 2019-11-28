/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elster.dlms.cosem.classes.common;

import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidationExecption;
import com.elster.dlms.types.basic.DlmsDateTime;
import com.elster.dlms.types.data.DlmsData;
import com.elster.dlms.types.data.DlmsDataOctetString;
import com.elster.dlms.types.data.DlmsDataStructure;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author osse
 */
public class TimeWindowTest
{
  public TimeWindowTest()
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
//
//  /**
//   * Test of fromDlmsDataArray method, of class TimeWindow.
//   */
//  @Test
//  public void testFromDlmsDataArray() throws Exception
//  {
//    System.out.println("fromDlmsDataArray");
//    DlmsData data = null;
//    TimeWindow[] expResult = null;
//    TimeWindow[] result = TimeWindow.fromDlmsDataArray(data);
//    assertEquals(expResult, result);
//    // TODO review the generated test code and remove the default call to fail.
//    fail("The test case is a prototype.");
//  }
//
//  /**
//   * Test of getEndTime method, of class TimeWindow.
//   */
//  @Test
//  public void testGetEndTime()
//  {
//    System.out.println("getEndTime");
//    TimeWindow instance = null;
//    DlmsDateTime expResult = null;
//    DlmsDateTime result = instance.getEndTime();
//    assertEquals(expResult, result);
//    // TODO review the generated test code and remove the default call to fail.
//    fail("The test case is a prototype.");
//  }
//
//  /**
//   * Test of getStartTime method, of class TimeWindow.
//   */
//  @Test
//  public void testGetStartTime()
//  {
//    System.out.println("getStartTime");
//    TimeWindow instance = null;
//    DlmsDateTime expResult = null;
//    DlmsDateTime result = instance.getStartTime();
//    assertEquals(expResult, result);
//    // TODO review the generated test code and remove the default call to fail.
//    fail("The test case is a prototype.");
//  }

  /**
   * Test of toDlmsData method, of class TimeWindow.
   */
  @Test
  public void testFromAndToDlmsData() throws ValidationExecption
  {
    System.out.println("toDlmsData");
    
    final DlmsDateTime start=new DlmsDateTime(2011,10,9,8,7,6,0);
    final DlmsDateTime end=new DlmsDateTime(2012,11,10,9,8,7,0);
    
    final TimeWindow instance = new TimeWindow(start,end);
    final DlmsData instanceAsData = instance.toDlmsData();
    
    final DlmsData expectedData=new DlmsDataStructure(new DlmsDataOctetString(start.toBytes()), new DlmsDataOctetString(
            end.toBytes()));

    assertEquals(expectedData, instanceAsData);
    TimeWindow.VALIDATOR.validate(instanceAsData);
    
    
    assertEquals(instance, new TimeWindow(expectedData)); //Test if time window can be created from the data.
    
    
    
  }

//  /**
//   * Test of toString method, of class TimeWindow.
//   */
//  @Test
//  public void testToString()
//  {
//    System.out.println("toString");
//    TimeWindow instance = null;
//    String expResult = "";
//    String result = instance.toString();
//    assertEquals(expResult, result);
//    // TODO review the generated test code and remove the default call to fail.
//    fail("The test case is a prototype.");
//  }
//
//  /**
//   * Test of equals method, of class TimeWindow.
//   */
//  @Test
//  public void testEquals()
//  {
//    System.out.println("equals");
//    Object obj = null;
//    TimeWindow instance = null;
//    boolean expResult = false;
//    boolean result = instance.equals(obj);
//    assertEquals(expResult, result);
//    // TODO review the generated test code and remove the default call to fail.
//    fail("The test case is a prototype.");
//  }
//
//  /**
//   * Test of hashCode method, of class TimeWindow.
//   */
//  @Test
//  public void testHashCode()
//  {
//    System.out.println("hashCode");
//    TimeWindow instance = null;
//    int expResult = 0;
//    int result = instance.hashCode();
//    assertEquals(expResult, result);
//    // TODO review the generated test code and remove the default call to fail.
//    fail("The test case is a prototype.");
//  }
  
}
