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
public class DlmsDataOctetStringTest {

    public DlmsDataOctetStringTest() {
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
   * Test of getType method, of class DlmsDataOctetString.
   */
  @Test
  public void testEquals()
  {
    System.out.println("equals");
    DlmsDataOctetString instance = new DlmsDataOctetString(new byte[]{(byte)1,(byte)2,(byte)3,(byte)4,(byte)5,(byte)6});
    DlmsDataOctetString other = new DlmsDataOctetString(new byte[]{(byte)1,(byte)2,(byte)3,(byte)4,(byte)5,(byte)6});
    boolean expResult = true;
    boolean result = instance.equals(other);
    assertEquals(expResult, result);
  }

    /**
   * Test of getType method, of class DlmsDataOctetString.
   */
  @Test
  public void testEquals2()
  {
    System.out.println("equals");
    DlmsDataOctetString instance = new DlmsDataOctetString(new byte[]{(byte)1,(byte)2,(byte)3,(byte)4,(byte)5,(byte)6});
    DlmsDataOctetString other = new DlmsDataOctetString(new byte[]{(byte)1,(byte)2,(byte)3,(byte)4,(byte)5,(byte)7});
    boolean expResult = false;
    boolean result = instance.equals(other);
    assertEquals(expResult, result);
  }
  
  
      /**
   * Test of getType method, of class DlmsDataOctetString.
   */
  @Test
  public void testImmutable()
  {
    System.out.println("immutable");
    DlmsDataOctetString instance = new DlmsDataOctetString(new byte[]{(byte)1,(byte)2,(byte)3,(byte)4,(byte)5,(byte)6});
    DlmsDataOctetString untouched = new DlmsDataOctetString(new byte[]{(byte)1,(byte)2,(byte)3,(byte)4,(byte)5,(byte)6});
    
    byte[] value = instance.getValue();
    value[4]=77;

    assertEquals(untouched, instance);
  }


}