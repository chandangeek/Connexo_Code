/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elster.dlms.cosem.application.services.common;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author osse
 */
public class DataAccessResultTest
{
  public DataAccessResultTest()
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
   * Test of findById method, of class DataAccessResult.
   */
  @Test
  public void testFindById()
  {
    System.out.println("findById");
    for (int i = 0; i < 1000000; i++)
    {
      for (DataAccessResult r : DataAccessResult.values())
      {
        assertEquals(r, DataAccessResult.findById(r.getId()));
      }
    }
  }

}
