/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.elster.dlms.types.data;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author osse
 */
public class DlmsDataStructureTest {

    public DlmsDataStructureTest() {
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
   * Test of getType method, of class DlmsDataStructure.
   */
  @Test
  public void testEquals()
  {
    System.out.println("getType");
    DlmsDataStructure instance = new DlmsDataStructure(new DlmsDataLong(123),new DlmsDataLong(456));
    DlmsDataStructure instance2 = new DlmsDataStructure(new DlmsDataLong(123),new DlmsDataLong(456));
    DlmsDataStructure instance3 = new DlmsDataStructure(new DlmsDataLong(123),new DlmsDataLong(457));
    assertEquals(instance.hashCode(), instance2.hashCode());
    assertNotSame(instance.hashCode(),instance3.hashCode()); // Theoretical it not an error if the to hash codes are identical, but it is very unlikely
    assertEquals("The two instances must be equal", instance,instance2);
    assertFalse("The two instances must not be equal", instance.equals(instance3));
  }
  
  
    /**
   * Test of getType method, of class DlmsDataStructure.
   */
  @Test
  public void testImmutable()
  {
    System.out.println("getType");
    DlmsDataStructure instance = new DlmsDataStructure(new DlmsDataLong(123),new DlmsDataLong(456));
    DlmsDataStructure untouchedInstance = new DlmsDataStructure(new DlmsDataLong(123),new DlmsDataLong(456));
    
    DlmsData[] value = instance.getValue();
    value[1]=new DlmsDataLong(789);
    
    assertEquals(untouchedInstance, instance);
  }

}