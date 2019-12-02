/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.elster.dlms.types.data;

import com.elster.dlms.types.data.DlmsData.DataType;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author osse
 */
public class DlmsDataNullTest {

    public DlmsDataNullTest() {
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
   * Test of getType method, of class DlmsDataNull.
   */
  @Test
  public void testEquals()
  {
    System.out.println("equals");
    DlmsDataNull instance = new DlmsDataNull();

    DlmsData other= new DlmsDataNull();

    boolean result= instance.equals(other);

    assertTrue(result);
  }

  @Test
  public void testEquals2()
  {
    System.out.println("equals 2");
    DlmsDataNull instance = new DlmsDataNull();

    DlmsData other= new DlmsDataInteger(1);

    boolean result= instance.equals(other);

    assertFalse(result);
  }

  @Test
  public void testEquals3()
  {
    System.out.println("equals 3");
    DlmsDataNull instance = new DlmsDataNull();

    DlmsData other= new DlmsDataInteger(1);

    boolean result= other.equals(instance);

    assertFalse(result);
  }

  /**
   * Test of getType method, of class DlmsDataNull.
   */
  @Test
  public void testGetType()
  {
    System.out.println("getType");
    DlmsDataNull instance = new DlmsDataNull();
    DataType expResult = DlmsData.DataType.NULL_DATA;
    DataType result = instance.getType();
    assertEquals(expResult, result);
  }

  /**
   * Test of getValue method, of class DlmsDataNull.
   */
  @Test
  public void testGetValue()
  {
    System.out.println("getValue");
    DlmsDataNull instance = new DlmsDataNull();
    Object result = instance.getValue();
    assertNull(result);
  }

  /**
   * Test of hashCode method, of class DlmsDataNull.
   */
  @Test
  public void testHashCode()
  {
    System.out.println("hashCode");
    DlmsDataNull instance = new DlmsDataNull();
    int expResult = 11625432;
    int result = instance.hashCode();
    assertEquals(expResult, result);
  }


}