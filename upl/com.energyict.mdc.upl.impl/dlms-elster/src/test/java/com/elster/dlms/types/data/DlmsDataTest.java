/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elster.dlms.types.data;

import java.lang.reflect.Method;
import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;

/**
 *
 * @author osse
 */
public class DlmsDataTest
{
  public DlmsDataTest()
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
   * Test the return types of the getValue methods
   */
  @Test
  public void testValueTypes() throws NoSuchMethodException
  {
    System.out.println("value types");
    for (DlmsData.DataType dt : DlmsData.DataType.values())
    {
      Method m= dt.getImplementingClass().getMethod("getValue");
      System.out.println(dt+" "+dt.getValueClass().getSimpleName()+", "+m.getReturnType().getSimpleName() );
      assertEquals("Wrong return type found for "+dt.getOrgName(),dt.getValueClass(),m.getReturnType());
    }
  }

}
