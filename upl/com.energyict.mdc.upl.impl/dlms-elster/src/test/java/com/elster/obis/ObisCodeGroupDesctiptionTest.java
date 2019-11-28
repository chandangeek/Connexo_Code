/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elster.obis;

import com.elster.dlms.types.basic.ObisCode;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author osse
 */
public class ObisCodeGroupDesctiptionTest
{


  public NameTable getNameTable()
  {
    return new NameTable();
  }



  /**
   * Test of describe method, of class ObisCodeGroupDescription.
   */
  @Test
  public void testDescribe()
  {
    System.out.println("describe");
    ObisCode code = new ObisCode(11, 12, 13, 14, 15, 16);
    ObisCodeGroupDescription instance = new ObisCodeGroupDescription("Billing period $F (255 is current)", 5,
                                                                     getNameTable());
    String expResult = "Billing period 16 (255 is current)";
    String result = instance.describe(code);
    assertEquals(expResult, result);
  }

  @Test
  public void testDescribe2()
  {
    System.out.println("describe");
    ObisCode code = new ObisCode(11, 12, 13, 14, 15, 16);
    ObisCodeGroupDescription instance = new ObisCodeGroupDescription("Limit $(E-10)",4,getNameTable());
    String expResult = "Limit 5";
    String result = instance.describe(code);
    assertEquals(expResult, result);
  }

  @Test
  public void testDescribe3()
  {
    System.out.println("describe");
    ObisCode code = new ObisCode(11, 12, 13, 14, 15, 16);
    ObisCodeGroupDescription instance = new ObisCodeGroupDescription("Limit bla bla",4,getNameTable());
    String expResult = "Limit bla bla";
    String result = instance.describe(code);
    assertEquals(expResult, result);
  }

}
