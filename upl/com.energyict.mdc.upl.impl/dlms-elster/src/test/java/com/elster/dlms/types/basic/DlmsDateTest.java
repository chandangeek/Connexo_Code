/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.elster.dlms.types.basic;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.SimpleTimeZone;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author osse
 */
public class DlmsDateTest {

    public DlmsDateTest() {
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
   * Test of getLocalDate method, of class DlmsDate.
   */
  @Test
  public void testGetDate()
  {
    System.out.println("getDate");
    DlmsDate instance = new DlmsDate(2010,1,1);
    Calendar cal= new GregorianCalendar(new SimpleTimeZone(0,"undef"));
    cal.setTimeInMillis(0);
    cal.set(2010, 0, 1, 0, 0, 0);
    Date expResult = cal.getTime();
    Date result = instance.getLocalDate();
    assertEquals(expResult, result);
  }

 
}