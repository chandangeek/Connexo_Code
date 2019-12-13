/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/types/basic/DlmsDate.java $
 * Version:     
 * $Id: DlmsDate.java 4476 2012-05-09 09:31:33Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  20.05.2010 12:21:46
 */
package com.elster.dlms.types.basic;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

/**
 * This class implements the DLMS date data type.
 * <P>
 * Objects of this class are immutable and therefore thread-safe
 * <P>
 * (see BB ed.10 p.27 ch. 4.1.6.1 Date and time formats )
 * @author osse
 */
public final class DlmsDate
{
  public static final int YEAR_NOT_SPECIFIED = 0xFFFF;
  public static final int MONTH_NOT_SPECIFIED = 0xFF;
  public static final int MONTH_DAYLIGHT_SAVINGS_END = 0xFD;
  public static final int MONTH_DAYLIGHT_SAVINGS_BEGIN = 0xFE;
  public static final int DAY_OF_MONTH_NOT_SPECIFIED = 0xFF;
  public static final int DAY_OF_MONTH_2ND_LAST_DAY_OF_MONTH = 0xFD;
  public static final int DAY_OF_MONTH_LAST_DAY_OF_MONTH = 0xFE;
  public static final int DAY_OF_WEEK_NOT_SPECIFIED = 0xFF;
  //---
  /**
   * Date with all attributes not specified.
   */
  public static final DlmsDate NOT_SPECIFIED_DATE =
          new DlmsDate(YEAR_NOT_SPECIFIED, MONTH_NOT_SPECIFIED, DAY_OF_MONTH_NOT_SPECIFIED,
                       DAY_OF_WEEK_NOT_SPECIFIED);
  //---
  private final int year; // = YEAR_NOT_SPECIFIED;
  private final int month; // = MONTH_NOT_SPECIFIED;
  private final int dayOfMonth; // = DAY_OF_MONTH_NOT_SPECIFIED;
  private final int dayOfWeek; // = DAY_OF_WEEK_NOT_SPECIFIED;

  /**
   * Day of (the) week enumeration.
   * 
   */
  public enum DlmsDayOfWeek
  {
    MONDAY(1, "Mon"), TUESDAY(2, "Tue"), WEDNESDAY(3, "Wed"), THURSDAY(4, "Thu"), FRIDAY(5, "Fri"),
    SATURDAY(6, "Sat"), SUNDAY(7, "Sun"), NOT_SPECIFIED(DAY_OF_WEEK_NOT_SPECIFIED, "NA");

    private DlmsDayOfWeek(final int dlmsId, final String abbreviation)
    {
      this.dlmsId = dlmsId;
      this.abbreviation = abbreviation;
    }

    final int dlmsId;
    final String abbreviation;

    /**
     * A three letter day abbreviation.
     * <P>
     * 
     * @return Mon,Tue,Wed,Thu,Fri,Sat,Sun for the Days and NA for Not available (Not specified)
     */
    public String getAbbreviation()
    {
      return abbreviation;
    }

    /**
     * The day id used in DLMS 
     * 
     * @return  The day id  (1=Monday... 7=Sunday)
     */
    public int getDlmsId()
    {
      return dlmsId;
    }

    /**
     * Returns the {@link DlmsDayOfWeek} for the specified id
     * 
     * @param id The id see {@link #getDlmsId()}
     * @return The {@link DlmsDayOfWeek} for the id
     * @throws IndexOutOfBoundsException if the id is not valid.
     */
    public static DlmsDayOfWeek forDlmsId(final int id)
    {
      if (id == 255)
      {
        return NOT_SPECIFIED;
      }

      if (id < 1 || id > 7)
      {
        throw new IndexOutOfBoundsException("The must beween 1 and 7 (including) or 255");
      }

      return values()[id - 1];
    }

  }

  /**
   * Creates the DLMS date from the byte array.<P>
   *
   * @param value A byte array representing the date. The length must be 5.
   */
  public DlmsDate(final byte[] value)
  {
    if (value.length != 5)
    {
      throw new IllegalArgumentException("The length of the array must be 5.");
    }
    year = ((0xFF & value[0]) << 8) | (0xFF & value[1]);
    month = 0xFF & value[2];
    dayOfMonth = 0xFF & value[3];
    dayOfWeek = 0xFF & value[4];
  }

  /**
   * Create the DLMS date from the specified parameters.
   *
   * @param year The year or {@link #YEAR_NOT_SPECIFIED}
   * @param month The month (1..12, 1=January -not 0!), {@link #MONTH_NOT_SPECIFIED}, {@link #MONTH_DAYLIGHT_SAVINGS_BEGIN} or {@link #MONTH_DAYLIGHT_SAVINGS_END}.
   * @param dayOfMonth The day of month (1..31), {@link #DAY_OF_MONTH_NOT_SPECIFIED}, {@link #DAY_OF_MONTH_2ND_LAST_DAY_OF_MONTH} or {@link #DAY_OF_MONTH_LAST_DAY_OF_MONTH}.
   * @param dayOfWeek The day of the week (1=Monday) or {@link #DAY_OF_WEEK_NOT_SPECIFIED}
   *
   */
  public DlmsDate(final int year, final int month, final int dayOfMonth, final int dayOfWeek)
  {
    this.year = year;
    this.month = month;
    this.dayOfMonth = dayOfMonth;
    this.dayOfWeek = dayOfWeek;
  }

  /**
   * Create the DLMS date from the specified parameters.
   *
   * @param year The year or {@link #YEAR_NOT_SPECIFIED}
   * @param month The month (1..12, 1=January -not 0!), {@link #MONTH_NOT_SPECIFIED}, {@link #MONTH_DAYLIGHT_SAVINGS_BEGIN} or {@link #MONTH_DAYLIGHT_SAVINGS_END}.
   * @param dayOfMonth The day of month (1..31), {@link #DAY_OF_MONTH_NOT_SPECIFIED}, {@link #DAY_OF_MONTH_2ND_LAST_DAY_OF_MONTH} or {@link #DAY_OF_MONTH_LAST_DAY_OF_MONTH}.
   * @param dayOfWeek The day of the week
   *
   */
  public DlmsDate(final int year, final int month, final int dayOfMonth, final DlmsDayOfWeek dayOfWeek)
  {
    this.year = year;
    this.month = month;
    this.dayOfMonth = dayOfMonth;
    this.dayOfWeek = dayOfWeek.getDlmsId();
  }

  /**
   * Same as {@link #DlmsDate(int, int, int, int) } with {@code dayOfWeek=DAY_OF_WEEK_NOT_SPECIFIED}
   *
   */
  public DlmsDate(final int year, final int month, final int dayOfMonth)
  {
    this.year = year;
    this.month = month;
    this.dayOfMonth = dayOfMonth;
    this.dayOfWeek = DAY_OF_WEEK_NOT_SPECIFIED;
  }

  /**
   * Byte representation (see BB)
   * 
   * @return The byte representation.
   */
  public byte[] toBytes()
  {
    return new byte[]
            {
              (byte)(0xFF & (year >> 8)),
              (byte)(0xFF & year),
              (byte)month,
              (byte)dayOfMonth,
              (byte)dayOfWeek
            };
  }

  /**
   * The day of month.
   *
   * @return The day of month (1..31), {@link #DAY_OF_MONTH_NOT_SPECIFIED}, {@link #DAY_OF_MONTH_2ND_LAST_DAY_OF_MONTH} or {@link #DAY_OF_MONTH_LAST_DAY_OF_MONTH}.
   */
  public int getDayOfMonth()
  {
    return dayOfMonth;
  }

  /**
   * The day of week.
   * 
   * @return The day of week (1=Monday) or {@link #DAY_OF_WEEK_NOT_SPECIFIED}
   */
  public int getDayOfWeek()
  {
    return dayOfWeek;
  }
  
  
  /**
   * The day of week.
   * 
   * @return The day of week
   */
  public DlmsDayOfWeek getDlmsDayOfWeek()
  {
    return DlmsDayOfWeek.forDlmsId(dayOfWeek);
  }
  
  

  /**
   * The month.
   *
   * @return The month (1..12, 1=January -not 0!), {@link #MONTH_NOT_SPECIFIED}, {@link #MONTH_DAYLIGHT_SAVINGS_BEGIN} or {@link #MONTH_DAYLIGHT_SAVINGS_END}.
   */
  public int getMonth()
  {
    return month;
  }

  /**
   * The year.
   *
   * @return The year or {@link #YEAR_NOT_SPECIFIED}.
   */
  public int getYear()
  {
    return year;
  }

  /**
   * Returns true if the date is complete defined.
   * 
   * (The dayOfWeek field can be undefined).
   *
   * @return  True if the date is complete defined
   */
  public boolean isRegular()
  {
    return (year != YEAR_NOT_SPECIFIED && month != MONTH_NOT_SPECIFIED && dayOfMonth <= 31);
  }

  private final static TimeZone TZ = new SimpleTimeZone(0, "undef");

  /**
   * Returns the date.<P>
   *
   * @return The date.
   */
  public Date getLocalDate()
  {
    if (!isRegular())
    {
      return null;
    }
    final GregorianCalendar cal =
            new GregorianCalendar(TZ);

    cal.clear();
    cal.set(year, month - 1, dayOfMonth);

    return cal.getTime();
  }

  private static final String NOT_SPECIFIED_TEXT = "not spec.";

  @Override
  public String toString()
  {

    String strYear;

    switch (year)
    {
      case YEAR_NOT_SPECIFIED:
        strYear = NOT_SPECIFIED_TEXT;
        break;
      default:
        strYear = Integer.toString(year);
    }


    String strMonth;
    switch (month)
    {
      case MONTH_NOT_SPECIFIED:
        strMonth = NOT_SPECIFIED_TEXT;
        break;
      case MONTH_DAYLIGHT_SAVINGS_BEGIN:
        strMonth = "Daylight savings begin";
        break;
      case MONTH_DAYLIGHT_SAVINGS_END:
        strMonth = "Daylight savings end";
        break;
      default:
        strMonth = Integer.toString(month);
    }

    String strDayOfMonth;
    switch (dayOfMonth)
    {
      case DAY_OF_MONTH_NOT_SPECIFIED:
        strDayOfMonth = NOT_SPECIFIED_TEXT;
        break;
      case DAY_OF_MONTH_2ND_LAST_DAY_OF_MONTH:
        strDayOfMonth = "2nd last day of month";
        break;
      case DAY_OF_MONTH_LAST_DAY_OF_MONTH:
        strDayOfMonth = "Last day of month";
        break;
      default:
        strDayOfMonth = Integer.toString(dayOfMonth);
    }

    String strDayOfWeek;
    switch (dayOfWeek)
    {
      case DAY_OF_WEEK_NOT_SPECIFIED:
        strDayOfWeek = NOT_SPECIFIED_TEXT;
        break;
      default:
        strDayOfWeek = Integer.toString(dayOfWeek);
    }

    return "DlmsDate{" + "year=" + strYear + ", month=" + strMonth + ", dayOfMonth=" + strDayOfMonth
           + ", dayOfWeek(1=Monday)=" + strDayOfWeek + '}';
  }

  @Override
  public boolean equals(final Object obj)
  {
    if (obj == null)
    {
      return false;
    }
    if (getClass() != obj.getClass())
    {
      return false;
    }
    final DlmsDate other = (DlmsDate)obj;
    if (this.year != other.year)
    {
      return false;
    }
    if (this.month != other.month)
    {
      return false;
    }
    if (this.dayOfMonth != other.dayOfMonth)
    {
      return false;
    }
    if (this.dayOfWeek != other.dayOfWeek)
    {
      return false;
    }
    return true;
  }

  /**
   * String representation of this date.
   * <P>
   * 
   * Format: EEE,yyyy-MM-dd<br>
   * <br>
   * EEE: The day of the week (Mon,Tue,Wen,Thu,Fri,Sat or Sun). If the day of the week is undefined it will be omitted.<br>
   * yyyy: The year or "*" if the year is undefined.<br>
   * MM:  The month (01..12),"DST_begin" for daylight savings begin,"DST_end" for daylight savings end or "*" if the month is undefined<br>
   * dd:  The day (01..31),"last" for last day of month, "2nd_last" for second last day of month or "*" if the day is undefined<br><br>
   * 
   * @return The string representation
   */
  public String stringValue()
  {
    final DecimalFormat df2 = new DecimalFormat("00");
    final StringBuilder sb = new StringBuilder();

    if (dayOfWeek != DAY_OF_WEEK_NOT_SPECIFIED)
    {
      if (dayOfWeek >= 1 && dayOfWeek <= 7)
      {
        sb.append(DlmsDayOfWeek.forDlmsId(dayOfWeek).getAbbreviation());
      }
      else
      {
        sb.append("DoW").append(dayOfWeek);
      }
      sb.append(" ");
    }

    sb.append(year == DlmsDate.YEAR_NOT_SPECIFIED ? "*" : Integer.toString(year));
    sb.append("-");
    switch (month)
    {
      case MONTH_DAYLIGHT_SAVINGS_BEGIN:
        sb.append("DST_begin");
        break;
      case MONTH_DAYLIGHT_SAVINGS_END:
        sb.append("DST_end");
        break;
      case MONTH_NOT_SPECIFIED:
        sb.append("*");
        break;
      default:
        sb.append(df2.format(month));
    }
    sb.append("-");
    switch (dayOfMonth)
    {
      case DAY_OF_MONTH_LAST_DAY_OF_MONTH:
        sb.append("last");
        break;
      case DAY_OF_MONTH_2ND_LAST_DAY_OF_MONTH:
        sb.append("2nd_last");
        break;
      case DAY_OF_MONTH_NOT_SPECIFIED:
        sb.append("*");
        break;
      default:
        sb.append(df2.format(dayOfMonth));
    }
    return sb.toString();
  }

  public boolean equalsIgnoreDayOfWeek(final DlmsDate other)
  {
    if (this.year != other.year)
    {
      return false;
    }
    if (this.month != other.month)
    {
      return false;
    }
    if (this.dayOfMonth != other.dayOfMonth)
    {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode()
  {
    int hash = 3;
    hash = 73 * hash + this.year;
    hash = 73 * hash + this.month;
    hash = 73 * hash + this.dayOfMonth;
    hash = 73 * hash + this.dayOfWeek;
    return hash;
  }

}
