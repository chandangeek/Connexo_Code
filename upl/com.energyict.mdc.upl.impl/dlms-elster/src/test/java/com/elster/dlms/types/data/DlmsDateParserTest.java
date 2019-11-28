/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elster.dlms.types.data;

import com.elster.dlms.types.basic.DlmsDate;
import com.elster.dlms.types.basic.DlmsDateTime;
import com.elster.dlms.types.basic.DlmsTime;
import java.util.Locale;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author osse
 */
public class DlmsDateParserTest
{
  public DlmsDateParserTest()
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
   * Test of getInstance method, of class DlmsDateParser.
   */
  @Test
  public void testGetInstance_0args()
  {
    System.out.println("getInstance");
    DlmsDateParser result = DlmsDateParser.getInstance();
    assertNotNull(result);

  }

  /**
   * Test of getInstance method, of class DlmsDateParser.
   */
  @Test
  public void testGetInstance_Locale()
  {
    System.out.println("getInstance");
    Locale locale = Locale.GERMANY;
    DlmsDateParser result = DlmsDateParser.getInstance(locale);
    assertNotNull( result);
  }

  /**
   * Test of formatDlmsDate method, of class DlmsDateParser.
   */
  @Test
  public void testFormatDlmsDate1()
  {
    System.out.println("formatDlmsDate 1");
    DlmsDate date = new DlmsDate(2015,6,7);
    DlmsDateParser instance = DlmsDateParser.getInstance();
    String expResult = "2015-06-07";
    String result = instance.formatDlmsDate(date);
    assertEquals(expResult, result);
  }
  
  
   /**
   * Test of formatDlmsDate method, of class DlmsDateParser.
   */
  @Test
  public void testFormatDlmsDate2()
  {
    System.out.println("formatDlmsDate 2");
    DlmsDate date = new DlmsDate(DlmsDate.YEAR_NOT_SPECIFIED,6,7);
    DlmsDateParser instance = DlmsDateParser.getInstance();
    String expResult = "____-06-07";
    String result = instance.formatDlmsDate(date);
    assertEquals(expResult, result);
  }
  
  
     /**
   * Test of formatDlmsDate method, of class DlmsDateParser.
   */
  @Test
  public void testFormatDlmsDate3()
  {
    System.out.println("formatDlmsDate 3");
    DlmsDate date = new DlmsDate(2020,DlmsDate.MONTH_NOT_SPECIFIED,7);
    DlmsDateParser instance = DlmsDateParser.getInstance();
    String expResult = "2020-__-07";
    String result = instance.formatDlmsDate(date);
    assertEquals(expResult, result);
  }

    
     /**
   * Test of formatDlmsDate method, of class DlmsDateParser.
   */
  @Test
  public void testFormatDlmsDate4()
  {
    System.out.println("formatDlmsDate 4");
    DlmsDate date = new DlmsDate(2020,6,DlmsDate.DAY_OF_MONTH_NOT_SPECIFIED);
    DlmsDateParser instance = DlmsDateParser.getInstance();
    String expResult = "2020-06-__";
    String result = instance.formatDlmsDate(date);
    assertEquals(expResult, result);
  }

  
  /**
   * Test of formatDlmsDateTime method, of class DlmsDateParser.
   */
  @Test
  public void testFormatDlmsDateTime1()
  {
    System.out.println("formatDlmsDateTime 1");
    DlmsDateTime dateTime = new DlmsDateTime(2021,12,31,23,59,30,99);
    DlmsDateParser instance = DlmsDateParser.getInstance();
    String expResult = "2021-12-31 23:59:30.99";
    String result = instance.formatDlmsDateTime(dateTime);
    assertEquals(expResult, result);
  }
  
  
    
  /**
   * Test of formatDlmsDateTime method, of class DlmsDateParser.
   */
  @Test
  public void testFormatDlmsDateTime2()
  {
    System.out.println("formatDlmsDateTime 2");
    DlmsDate date = new DlmsDate(2015,6,7);
    DlmsTime time = new DlmsTime(0,1,2,3);
    DlmsDateTime dateTime = new DlmsDateTime(date,time,150,0);
    DlmsDateParser instance = DlmsDateParser.getInstance();
    String expResult = "2015-06-07 00:01:02.03+02:30";
    String result = instance.formatDlmsDateTime(dateTime);
    assertEquals(expResult, result);
  }
  
    /**
   * Test of formatDlmsDateTime method, of class DlmsDateParser.
   */
  @Test
  public void testFormatDlmsDateTime3()
  {
    System.out.println("formatDlmsDateTime 3");
    DlmsDate date = new DlmsDate(2015,6,7);
    DlmsTime time = new DlmsTime(0,1,2,3);
    DlmsDateTime dateTime = new DlmsDateTime(date,time,-720,0);
    DlmsDateParser instance = DlmsDateParser.getInstance();
    String expResult = "2015-06-07 00:01:02.03-12:00";
    String result = instance.formatDlmsDateTime(dateTime);
    assertEquals(expResult, result);
  }
  
  
    /**
   * Test of formatDlmsDateTime method, of class DlmsDateParser.
   */
  @Test
  public void testFormatDlmsDateTime4()
  {
    System.out.println("formatDlmsDateTime 4");
    DlmsDate date = new DlmsDate(2015,6,7);
    DlmsTime time = new DlmsTime(0,1,2,3);
    DlmsDateTime dateTime = new DlmsDateTime(date,time,-150,0);
    DlmsDateParser instance = DlmsDateParser.getInstance();
    String expResult = "2015-06-07 00:01:02.03-02:30";
    String result = instance.formatDlmsDateTime(dateTime);
    assertEquals(expResult, result);
  }

  
  
  

  /**
   * Test of formatDlmsTime method, of class DlmsDateParser.
   */
  @Test
  public void testFormatDlmsTime1()
  {
    System.out.println("formatDlmsTime 1");
    DlmsTime time = new DlmsTime(0,1,2,3);
    DlmsDateParser instance =  DlmsDateParser.getInstance();
    String expResult = "00:01:02.03";
    String result = instance.formatDlmsTime(time);
    assertEquals(expResult, result);
  }
  
  
  
  /**
   * Test of formatDlmsTime method, of class DlmsDateParser.
   */
  @Test
  public void testFormatDlmsTime2()
  {
    System.out.println("formatDlmsTime 2");
    DlmsTime time = new DlmsTime(0,1,2,DlmsTime.NOT_SPECIFIED);
    DlmsDateParser instance =  DlmsDateParser.getInstance();
    String expResult = "00:01:02";
    String result = instance.formatDlmsTime(time);
    assertEquals(expResult, result);
  }

//  /**
//   * Test of dayOfWeekToString method, of class DlmsDateParser.
//   */
//  @Test
//  public void testDayOfWeekToString()
//  {
//    System.out.println("dayOfWeekToString");
//    int dayOfWeek = 0;
//    DlmsDateParser instance = null;
//    String expResult = "";
//    String result = instance.dayOfWeekToString(dayOfWeek);
//    assertEquals(expResult, result);
//    // TODO review the generated test code and remove the default call to fail.
//    fail("The test case is a prototype.");
//  }
//
  /**
   * Test of parseDate method, of class DlmsDateParser.
   */
  @Test
  public void testParseDate() throws Exception
  {
    System.out.println("parseDate");
    String date = "2015-06-07";
    boolean mustBeRegular = true;
    DlmsDateParser instance = DlmsDateParser.getInstance();
    DlmsDate expResult = new DlmsDate(2015,6,7);
    DlmsDate result = instance.parseDate(date, mustBeRegular);
    assertEquals(expResult, result);
  }
  
  @Test
  public void testParseDate2() throws Exception
  {
    System.out.println("parseDate 2");
    String date = "____-06-07";
    boolean mustBeRegular = false;
    DlmsDateParser instance = DlmsDateParser.getInstance();
    DlmsDate expResult = new DlmsDate(DlmsDate.YEAR_NOT_SPECIFIED,6,7);
    DlmsDate result = instance.parseDate(date, mustBeRegular);
    assertEquals(expResult, result);
  }
  
  @Test
  public void testParseDate3() throws Exception
  {
    System.out.println("parseDate 3");
    String date ="2020-__-07";
    boolean mustBeRegular = false;
    DlmsDateParser instance = DlmsDateParser.getInstance();
    DlmsDate expResult = new DlmsDate(2020,DlmsDate.MONTH_NOT_SPECIFIED,7);
    DlmsDate result = instance.parseDate(date, mustBeRegular);
    assertEquals(expResult, result);
  }
    
  @Test
  public void testParseDate4() throws Exception
  {
    System.out.println("parseDate 4");
    String date ="2020-06-__";
    boolean mustBeRegular = false;
    DlmsDateParser instance = DlmsDateParser.getInstance();
    DlmsDate expResult = new DlmsDate(2020,6,DlmsDate.DAY_OF_MONTH_NOT_SPECIFIED);
    DlmsDate result = instance.parseDate(date, mustBeRegular);
    assertEquals(expResult, result);
  }
    
  
  

  /**
   * Test of parseTime method, of class DlmsDateParser.
   */
  @Test
  public void testParseTime() throws Exception
  {
    System.out.println("parseTime");
    String time = "00:01:02.03";
    boolean mustBeRegular = false;
    DlmsDateParser instance =  DlmsDateParser.getInstance();
    DlmsTime expResult = new DlmsTime(0,1,2,3);
    DlmsTime result = instance.parseTime(time, mustBeRegular);
    assertEquals(expResult, result);
  }
  

  /**
   * Test of parseTime method, of class DlmsDateParser.
   */
  @Test
  public void testParseTime2() throws Exception
  {
    System.out.println("parseTime 2");
    String time = "00:01:02";
    boolean mustBeRegular = false;
    DlmsDateParser instance =  DlmsDateParser.getInstance();
    DlmsTime expResult =  new DlmsTime(0,1,2,DlmsTime.NOT_SPECIFIED);
    DlmsTime result = instance.parseTime(time, mustBeRegular);
    assertEquals(expResult, result);
  }

  /**
   * Test of parseDateTime method, of class DlmsDateParser.
   */
  @Test
  public void testParseDateTime() throws Exception
  {
    System.out.println("parseDateTime");
    String dateTime = "2015-06-07 00:01:02.03+02:30";
    boolean mustBeRegular = false;
    DlmsDateParser instance = DlmsDateParser.getInstance();
    DlmsDate date = new DlmsDate(2015,6,7);
    DlmsTime time = new DlmsTime(0,1,2,3);    
    DlmsDateTime expResult = new DlmsDateTime(date,time,150,0);
    DlmsDateTime result = instance.parseDateTime(dateTime, mustBeRegular);
    assertEquals(expResult, result);
  }
  
  
  
  /**
   * Test of parseDateTime method, of class DlmsDateParser.
   */
  @Test
  public void testParseDateTime2 () throws Exception
  {
    System.out.println("parseDateTime 2");
    String dateTime =  "2015-06-07 00:01:02.03-12:00";
    boolean mustBeRegular = false;
    DlmsDateParser instance = DlmsDateParser.getInstance();
    DlmsDate date = new DlmsDate(2015,6,7);
    DlmsTime time = new DlmsTime(0,1,2,3);    
    DlmsDateTime expResult = new DlmsDateTime(date,time,-720,0);
    DlmsDateTime result = instance.parseDateTime(dateTime, mustBeRegular);
    assertEquals(expResult, result);
  }
  
    /**
   * Test of parseDateTime method, of class DlmsDateParser.
   */
  @Test
  public void testParseDateTime3() throws Exception
  {
    System.out.println("parseDateTime 3");
    String dateTime = "2015-06-07 00:01:02.03-02:30";
    boolean mustBeRegular = false;
    DlmsDateParser instance = DlmsDateParser.getInstance();
    DlmsDate date = new DlmsDate(2015,6,7);
    DlmsTime time = new DlmsTime(0,1,2,3);    
    DlmsDateTime expResult = new DlmsDateTime(date,time,-150,0);
    DlmsDateTime result = instance.parseDateTime(dateTime, mustBeRegular);
    assertEquals(expResult, result);
  }
  
    
    /**
   * Test of parseDateTime method, of class DlmsDateParser.
   */
  @Test
  public void testParseDateTime4() throws Exception
  {
    System.out.println("parseDateTime 4");
    String dateTime = "2015-06-07 00:01:02.03";
    boolean mustBeRegular = false;
    DlmsDateParser instance = DlmsDateParser.getInstance();
    DlmsDate date = new DlmsDate(2015,6,7);
    DlmsTime time = new DlmsTime(0,1,2,3);    
    DlmsDateTime expResult = new DlmsDateTime(date,time);
    DlmsDateTime result = instance.parseDateTime(dateTime, mustBeRegular);
    assertEquals(expResult, result);
  }
  
  
      /**
   * Test of parseDateTime method, of class DlmsDateParser.
   */
  @Test
  public void testParseDateTime5() throws Exception
  {
    System.out.println("parseDateTime 5");
    String dateTime = "2015-06-07 00:01:02.03 -02:30";
    boolean mustBeRegular = false;
    DlmsDateParser instance = DlmsDateParser.getInstance();
    DlmsDate date = new DlmsDate(2015,6,7);
    DlmsTime time = new DlmsTime(0,1,2,3);    
    DlmsDateTime expResult = new DlmsDateTime(date,time,-150,0);
    DlmsDateTime result = instance.parseDateTime(dateTime, mustBeRegular);
    assertEquals(expResult, result);
  }
  
  
        /**
   * Test of parseDateTime method, of class DlmsDateParser.
   */
  @Test
  public void testParseDateTime6() throws Exception
  {
    System.out.println("parseDateTime 6");
    String dateTime = "2015-06-07 00:01:02.03-02";
    boolean mustBeRegular = false;
    DlmsDateParser instance = DlmsDateParser.getInstance();
    DlmsDate date = new DlmsDate(2015,6,7);
    DlmsTime time = new DlmsTime(0,1,2,3);    
    DlmsDateTime expResult = new DlmsDateTime(date,time,-120,0);
    DlmsDateTime result = instance.parseDateTime(dateTime, mustBeRegular);
    assertEquals(expResult, result);
  }
  
  
  
  
  
}
