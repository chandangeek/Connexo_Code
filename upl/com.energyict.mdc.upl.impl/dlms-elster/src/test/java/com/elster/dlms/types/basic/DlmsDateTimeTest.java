/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elster.dlms.types.basic;

import com.elster.dlms.types.basic.DlmsDateTime.ClockStatus;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumSet;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.SimpleTimeZone;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author osse
 */
public class DlmsDateTimeTest
{
  public DlmsDateTimeTest()
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
   * Test of getCalendar method, of class DlmsDateTime.
   */
  @Test
  public void testGetCalendar()
  {
    System.out.println("getCalendar");
    DlmsDateTime instance = new DlmsDateTime(new DlmsDate(2001, 1, 2), new DlmsTime(3, 4, 5, 0), 60, 0);
    Calendar expResult = new GregorianCalendar(new SimpleTimeZone(60 * 60 * 1000, "60 min"));
    expResult.clear();
    expResult.set(2001, 0, 2, 3, 4, 5);

    Calendar result = instance.getCalendar();

    System.out.println("Expected: " + expResult.toString());
    System.out.println("Result: " + result.toString());

    System.out.println("Expected: " + expResult.getTime().toString());
    System.out.println("Result: " + result.getTime().toString());

    assertEquals(expResult.getTime(), result.getTime());
  }

  /**
   * Creating a DLMS-Date-Time object with an Calendar.<P>
   * In summer time.
   */
  @Test
  public void testCreateWithCalendar1()
  {
    System.out.println("create");
    Calendar calendar = new GregorianCalendar(Locale.GERMAN);
    calendar.clear();
    calendar.set(2011, 06, 12, 14, 31, 5);
    DlmsDateTime instance = new DlmsDateTime(calendar, true);
    System.out.println("Instance:" + instance);
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ");
    dateFormat.setTimeZone(calendar.getTimeZone());
    System.out.println("Used time:" + dateFormat.format(calendar.getTime()));

    assertEquals(2011, instance.getDlmsDate().getYear());
    assertEquals(7, instance.getDlmsDate().getMonth());
    assertEquals(12, instance.getDlmsDate().getDayOfMonth());
    assertEquals(14, instance.getDlmsTime().getHour());
    assertEquals(31, instance.getDlmsTime().getMinute());
    assertEquals(5, instance.getDlmsTime().getSecond());
    assertEquals(0, instance.getDlmsTime().getHundredths());
    assertEquals(120, instance.getDeviation());
    assertTrue(instance.getClockStatusSet().contains(DlmsDateTime.ClockStatus.DAYLIGHT_SAVING_ACTIVE));
    assertEquals(0x80, instance.getClockStatus());

  }

  /**
   * Creating a DLMS-Date-Time object with an Calendar.<P>
   * In summer time, not setting the DST-Flag
   */
  @Test
  public void testCreateWithCalendar2()
  {
    System.out.println("create 2");
    Calendar calendar = new GregorianCalendar(Locale.GERMAN);
    calendar.clear();
    calendar.set(2011, 06, 12, 14, 31, 5);
    DlmsDateTime instance = new DlmsDateTime(calendar, false);
    System.out.println("Instance:" + instance);
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ");
    dateFormat.setTimeZone(calendar.getTimeZone());
    System.out.println("Used time:" + dateFormat.format(calendar.getTime()));


    assertEquals(2011, instance.getDlmsDate().getYear());
    assertEquals(7, instance.getDlmsDate().getMonth());
    assertEquals(12, instance.getDlmsDate().getDayOfMonth());
    assertEquals(14, instance.getDlmsTime().getHour());
    assertEquals(31, instance.getDlmsTime().getMinute());
    assertEquals(5, instance.getDlmsTime().getSecond());
    assertEquals(0, instance.getDlmsTime().getHundredths());
    assertEquals(120, instance.getDeviation());
    assertFalse(instance.getClockStatusSet().contains(DlmsDateTime.ClockStatus.DAYLIGHT_SAVING_ACTIVE));
    assertEquals(0x00, instance.getClockStatus());

  }

  /**
   * Creating a DLMS-Date-Time object with an Calendar.<P>
   * Not in summer time.
   */
  @Test
  public void testCreateWithCalendar3()
  {
    System.out.println("create 3");
    Calendar calendar = new GregorianCalendar(Locale.GERMAN);
    calendar.clear();
    calendar.set(2011, 01, 12, 14, 31, 5);
    DlmsDateTime instance = new DlmsDateTime(calendar, true);
    System.out.println("Instance:" + instance);
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ");
    dateFormat.setTimeZone(calendar.getTimeZone());
    System.out.println("Used time:" + dateFormat.format(calendar.getTime()));

    assertEquals(2011, instance.getDlmsDate().getYear());
    assertEquals(2, instance.getDlmsDate().getMonth());
    assertEquals(12, instance.getDlmsDate().getDayOfMonth());
    assertEquals(14, instance.getDlmsTime().getHour());
    assertEquals(31, instance.getDlmsTime().getMinute());
    assertEquals(5, instance.getDlmsTime().getSecond());
    assertEquals(0, instance.getDlmsTime().getHundredths());
    assertEquals(60, instance.getDeviation());
    assertFalse(instance.getClockStatusSet().contains(DlmsDateTime.ClockStatus.DAYLIGHT_SAVING_ACTIVE));
    assertEquals(0x00, instance.getClockStatus());
  }

  @Test
  public void testCreatFromUtc()
  {
    //--- prepare (Time in summer, DST off, time zone +60)
    Calendar calInSummer = new GregorianCalendar(new SimpleTimeZone(60 * 60 * 1000, "utc+60"));
    calInSummer.clear();

    calInSummer.set(2011, 8 - 1, 15, 12, 5, 14);

    assertEquals(15, calInSummer.get(Calendar.DAY_OF_MONTH));

    DlmsDateTime dateTime =
            DlmsDateTime.createFromUtc(calInSummer.getTime(), 60);

    assertEquals(2011, dateTime.getDlmsDate().getYear());
    assertEquals(8, dateTime.getDlmsDate().getMonth());
    assertEquals(15, dateTime.getDlmsDate().getDayOfMonth());
    assertEquals(12, dateTime.getDlmsTime().getHour());
    assertEquals(5, dateTime.getDlmsTime().getMinute());
    assertEquals(14, dateTime.getDlmsTime().getSecond());
  }

  @Test
  public void testCreatFromUtc2()
  {
    //--- prepare (Time in summer, DST off, time zone +60)
    Calendar calInSummer = new GregorianCalendar(new SimpleTimeZone(0, "utc"));
    calInSummer.clear();

    calInSummer.set(2020, 6 - 1, 21, 12, 0, 0);

    assertEquals(21, calInSummer.get(Calendar.DAY_OF_MONTH));

    DlmsDateTime dateTime =
            DlmsDateTime.createFromUtc(calInSummer.getTime(), 120);

    assertEquals(2020, dateTime.getDlmsDate().getYear());
    assertEquals(6, dateTime.getDlmsDate().getMonth());
    assertEquals(21, dateTime.getDlmsDate().getDayOfMonth());
    assertEquals(14, dateTime.getDlmsTime().getHour());
    assertEquals(0, dateTime.getDlmsTime().getMinute());
    assertEquals(0, dateTime.getDlmsTime().getSecond());
    assertEquals(120, dateTime.getDeviation());
  }

  @Test
  public void testCreatFromLocal()
  {
    //--- prepare (Time in summer, DST off, time zone +60)
    Calendar calInSummer = new GregorianCalendar(new SimpleTimeZone(0, "undef"));
    calInSummer.clear();

    calInSummer.set(2020, 6 - 1, 21, 12, 0, 0);

    assertEquals(21, calInSummer.get(Calendar.DAY_OF_MONTH));

    DlmsDateTime dateTime =
            DlmsDateTime.createFromLocal(calInSummer.getTime());

    assertEquals(2020, dateTime.getDlmsDate().getYear());
    assertEquals(6, dateTime.getDlmsDate().getMonth());
    assertEquals(21, dateTime.getDlmsDate().getDayOfMonth());
    assertEquals(12, dateTime.getDlmsTime().getHour());
    assertEquals(0, dateTime.getDlmsTime().getMinute());
    assertEquals(0, dateTime.getDlmsTime().getSecond());
    assertEquals(DlmsDateTime.DEVIATION_NOT_SPECIFIED, dateTime.getDeviation());
  }

  @Test
  public void testToBytesFromBytes()
  {

    DlmsDateTime instance = DlmsDateTime.createFromUtc(new Date(), -920);
    assertEquals(-920, instance.getDeviation());

    byte[] toBytes = instance.toBytes();
    DlmsDateTime instance2 = new DlmsDateTime(toBytes);

    assertEquals(instance, instance2);
    assertEquals(-920, instance2.getDeviation());  //to be sure.
  }

  @Test
  public void testToBytesFromBytes2()
  {

    DlmsDateTime instance = DlmsDateTime.createFromUtc(new Date(), 920);
    assertEquals(920, instance.getDeviation());

    byte[] toBytes = instance.toBytes();
    DlmsDateTime instance2 = new DlmsDateTime(toBytes);

    assertEquals(instance, instance2);
    assertEquals(920, instance2.getDeviation());  //to be sure.
  }

  @Test
  public void testToBytesFromBytes3()
  {

    DlmsDateTime instance = DlmsDateTime.createFromUtc(new Date(), DlmsDateTime.DEVIATION_NOT_SPECIFIED);
    assertEquals(DlmsDateTime.DEVIATION_NOT_SPECIFIED, instance.getDeviation());

    byte[] toBytes = instance.toBytes();
    DlmsDateTime instance2 = new DlmsDateTime(toBytes);

    assertEquals(instance, instance2);
    assertEquals(DlmsDateTime.DEVIATION_NOT_SPECIFIED, instance2.getDeviation());  //to be sure.
  }

  @Test
  public void testCreateDlmsDateTime()
  {
    DlmsDateTime instance = new DlmsDateTime(new Date());
    System.out.println("with date:" + instance);

    instance = new DlmsDateTime(new GregorianCalendar(), true);
    System.out.println("with calendar:" + instance);
  }

  @Test
  public void testStringValue()
  {
    final DlmsDate date = new DlmsDate(2020, 12, 6, 2);
    final DlmsTime time = new DlmsTime(11, 12, 13, 0);
    final DlmsDateTime instance = new DlmsDateTime(date, time, 120, 0);
    System.out.println(instance.stringValue());
    assertEquals("Tue 2020-12-06,11:12:13.00+02:00", instance.stringValue());
  }

  @Test
  public void testStringValue2()
  {
    final DlmsDate date = new DlmsDate(2020, 12, 6);
    final DlmsTime time = new DlmsTime(11, 12, 13, 0);
    final DlmsDateTime instance = new DlmsDateTime(date, time, DlmsDateTime.DEVIATION_NOT_SPECIFIED, EnumSet.
            of(DlmsDateTime.ClockStatus.DAYLIGHT_SAVING_ACTIVE));
    System.out.println(instance.stringValue());
    assertEquals("2020-12-06,11:12:13.00[DST]", instance.stringValue());
  }

  @Test
  public void testStringValue3()
  {
    final DlmsDate date = new DlmsDate(2020, 12, 6);
    final DlmsTime time = new DlmsTime(11, 12, 13, 0);
    final DlmsDateTime instance = new DlmsDateTime(date, time, DlmsDateTime.DEVIATION_NOT_SPECIFIED, EnumSet.
            of(ClockStatus.DAYLIGHT_SAVING_ACTIVE, ClockStatus.DOUBTFUL_VALUE));
    System.out.println(instance.stringValue());
    assertEquals("2020-12-06,11:12:13.00[DV,DST]", instance.stringValue());
  }

  @Test
  public void testStringValue4()
  {
    final DlmsDate date = new DlmsDate(DlmsDate.YEAR_NOT_SPECIFIED, 12, DlmsDate.DAY_OF_MONTH_NOT_SPECIFIED);
    final DlmsTime time = new DlmsTime(11, 12, 13, 0);
    final DlmsDateTime instance = new DlmsDateTime(date, time, DlmsDateTime.DEVIATION_NOT_SPECIFIED, 0);
    System.out.println(instance.stringValue());
    assertEquals("*-12-*,11:12:13.00", instance.stringValue());
  }

  @Test
  public void testStringValue5()
  {
    final DlmsDate date = new DlmsDate(2020, 12, 6, DlmsDate.DlmsDayOfWeek.SUNDAY);
    final DlmsTime time = new DlmsTime(11, 12, 13, 0);
    final DlmsDateTime instance = new DlmsDateTime(date, time, -150, EnumSet.of(
            ClockStatus.DAYLIGHT_SAVING_ACTIVE, ClockStatus.DOUBTFUL_VALUE));
    System.out.println(instance.stringValue());
    assertEquals("Sun 2020-12-06,11:12:13.00-02:30[DV,DST]", instance.stringValue());
  }

  @Test
  public void testStringValue6()
  {
    final DlmsDate date = new DlmsDate(DlmsDate.YEAR_NOT_SPECIFIED, 3, DlmsDate.DAY_OF_MONTH_LAST_DAY_OF_MONTH,
                                       DlmsDate.DlmsDayOfWeek.SUNDAY);
    final DlmsTime time = new DlmsTime(11, 12, 13, 0);
    final DlmsDateTime instance = new DlmsDateTime(date, time, 120, 0);
    System.out.println(instance.stringValue());
    assertEquals("Sun *-03-last,11:12:13.00+02:00", instance.stringValue());
  }

}