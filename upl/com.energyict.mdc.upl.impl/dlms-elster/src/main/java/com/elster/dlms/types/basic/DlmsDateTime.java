/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/types/basic/DlmsDateTime.java $
 * Version:     
 * $Id: DlmsDateTime.java 4811 2012-07-10 14:17:58Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  20.05.2010 12:21:46
 */
package com.elster.dlms.types.basic;

import com.elster.coding.CodingUtils;
import java.text.DecimalFormat;
import java.util.*;

/**
 * This class implements the DLMS date-time data type.
 * <P>
 * Objects of this class are immutable and therefore thread-safe
 * <P>
 * (a. BB ed.10 p.27 ch.4.1.6.1 "Date and time formats")
 * @author osse
 */
public final class DlmsDateTime
{
  private final DlmsDate date;
  private final DlmsTime time;
  private final int deviation;
  private final int clockStatus;
  public static final int DEVIATION_NOT_SPECIFIED = 0x8000;
  public static final int CLOCK_STATUS_BIT_INVALID = 0x01;
  public static final int CLOCK_STATUS_BIT_DOUBTFUL = 0x01 << 1;
  public static final int CLOCK_STATUS_BIT_DIFFERENT_CLOCK_BASE = 0x01 << 2;
  public static final int CLOCK_STATUS_BIT_INVALID_CLOCK_STATUS = 0x01 << 3;
  public static final int CLOCK_STATUS_BIT_DAYLIGHT_SAVING_ACTIVE = 0x01 << 7;
  /**
   * A time zone with an offset of 0 and "undef_timezone" as id.
   */
  public static final TimeZone UNDEF_TIMEZONE = new SimpleTimeZone(0, "undef_timezone");
  
  /**
   * A DlmsDateTime with all fields set to "NOT_SPECIFIED" and 0 as clock status.
   * 
   */
  public static final DlmsDateTime NOT_SPECIFIED_DATE_TIME = new DlmsDateTime(DlmsDate.NOT_SPECIFIED_DATE,
                                                                              DlmsTime.NOT_SPECIFIED_TIME,
                                                                              DEVIATION_NOT_SPECIFIED, 0);

  /**
   * Enumeration for the clock status.
   */
  public static enum ClockStatus
  {
    INVALID_VALUE(CLOCK_STATUS_BIT_INVALID, "invalid value", "IV"),
    DOUBTFUL_VALUE(CLOCK_STATUS_BIT_DOUBTFUL, "doubtful value", "DV"),
    DIFFERENT_CLOCK_BASE(CLOCK_STATUS_BIT_DIFFERENT_CLOCK_BASE, "different clock base", "DCB"),
    INVALID_CLOCK_STATUS(CLOCK_STATUS_BIT_INVALID_CLOCK_STATUS, "invalid clock status", "ICS"),
    DAYLIGHT_SAVING_ACTIVE(CLOCK_STATUS_BIT_DAYLIGHT_SAVING_ACTIVE, "daylight saving active", "DST");
    private final int bitMask;
    private final String longName;
    private final String shortName;

    ClockStatus(final int bitMask, final String longName, final String shortName)
    {
      this.bitMask = bitMask;
      this.longName = longName;
      this.shortName = shortName;
    }

    /**
     * The bit mask for this clock status
     * 
     * @return The bit mask.
     */
    public int getBitMask()
    {
      return bitMask;
    }

    /**
     * The name according to the BB.
     * 
     * @return The name
     */
    public String getLongName()
    {
      return longName;
    }

    /**
     * A short name, used by {@link DlmsDateTime#stringValue()}
     * 
     * @return 
     */
    public String getShortName()
    {
      return shortName;
    }
  };

  /**
   * Creates an DlmsDateTime with the specified deviation from a (normal) UTC date object.
   * <P>
   * For example, if the {@code utcDate} represents "2020-06-31 12:00:00Z" and the 
   * {@code deviationInMinutes} is set to 120 the DlmsDateTime will represent "2020-06-31 14:00:00+02:00"
   * 
   * 
   * @param utcDate The date time (in UTC).
   * @param deviationInMinutes The deviation in minutes.
   * @return  The created DlmsDateTime.
   */
  public static DlmsDateTime createFromUtc(final Date utcDate,final int deviationInMinutes)
  {

    final GregorianCalendar cal = new GregorianCalendar(new SimpleTimeZone(deviationInMinutes * 60 * 1000,
                                                                     "Device UTC"
                                                                     + deviationInMinutes));
    cal.clear();
    cal.setTime(utcDate);
    return new DlmsDateTime(cal, false);
  }
  
  
   /**
   * Creates an DlmsDateTime with the specified deviation from a "local" date object.
   * <P>
   * The date parameter is regarded as local time. (For formating these local date in java the UTC time zone must 
   * be assumed.)
   * 
   * @param localDate The local date time.
   * @return  The created DlmsDateTime.
   */
  public static DlmsDateTime createFromLocal(final Date localDate)
  {
    return new DlmsDateTime(localDate, DEVIATION_NOT_SPECIFIED);
  }
  
  
  
  

  /**
   * Create the DLMS date time from the specified octet string.<P>
   * The octet string must have a length of 12 bytes.
   *
   * @param value
   */
  public DlmsDateTime(final byte[] value)
  {
    if (value.length != 12)
    {
      throw new IllegalArgumentException("The octet string for DLMS date time must have a length of 12");
    }
    date = new DlmsDate(CodingUtils.copyOfRange(value, 0, 5));
    time = new DlmsTime(CodingUtils.copyOfRange(value, 5, 9));

    int localDeviation;

    if (value[9] < 0)
    {
      localDeviation = 0xFFFF0000 | ((0xFF & value[9]) << 8) | (0xFF & value[10]); // neg.
    }
    else
    {
      localDeviation = ((0xFF & value[9]) << 8) | (0xFF & value[10]);
    }

    if (localDeviation == (0xFFFF0000 | DEVIATION_NOT_SPECIFIED))
    {
      localDeviation = DEVIATION_NOT_SPECIFIED;
    }

    deviation = localDeviation;


    clockStatus = 0xFF & value[11];
  }

  /**
   * Creates the DLMS date time with the specified parameters.
   *
   * @param date The date.
   * @param time The time
   * @param deviation Deviation to local time in minutes.
   * @param clockStatus The clock status byte
   */
  public DlmsDateTime(final DlmsDate date, final DlmsTime time, final int deviation, final int clockStatus)
  {
    this.date = date;
    this.time = time;
    this.deviation = deviation;
    this.clockStatus = clockStatus;
  }

  /**
   * Creates the DLMS date time with the specified parameters.
   *
   * @param date The date.
   * @param time The time
   * @param deviation Deviation to local time in minutes.
   * @param clockStatus The clock status as set.
   */
  public DlmsDateTime(final DlmsDate date,final DlmsTime time,final int deviation,final Set<ClockStatus> clockStatus)
  {
    this.date = date;
    this.time = time;
    this.deviation = deviation;

    int csByte = 0;

    for (ClockStatus cs : clockStatus)
    {
      csByte = csByte | cs.getBitMask();
    }
    this.clockStatus = csByte;
  }

  /**
   * Convenient  constructor for {@code new DlmsDateTime(DlmsDate(year, month, dayOfMonth), new DlmsTime(hour, minute, second, hundredths))}
   */
  public DlmsDateTime(final int year, final int month, final int dayOfMonth, final int hour, final int minute,
                      final int second, final int hundredths)
  {
    this(new DlmsDate(year, month, dayOfMonth), new DlmsTime(hour, minute, second, hundredths));
  }

  /**
   * Set the specified date and time. The deviation will be set to {@link #DEVIATION_NOT_SPECIFIED} and
   * the clock status to 0.
   * 
   * 
   * @param date The date to set
   * @param time  The time to set.
   */
  public DlmsDateTime(final DlmsDate date, final DlmsTime time)
  {
    this.date = date;
    this.time = time;
    this.deviation = DEVIATION_NOT_SPECIFIED;
    this.clockStatus = 0;
  }

  /**
   * Creates a DLMS-Timestamp from an date object (in UTC).<P>
   * The deviation will be set to 0.
   * The clock status will be set to 0.
   *
   * @param date
   */
  public DlmsDateTime(final Date date)
  {
    this(date, 0);
  }

  /**
   * Creates a DLMS-Timestamp from an date object.
   * <P>
   * The deviation will be set to the specified value.
   * The clock status will be set to 0.
   *
   * @param date
   */
  public DlmsDateTime(Date date, int deviation)
  {
    GregorianCalendar gregorianCalendar = new GregorianCalendar(UNDEF_TIMEZONE);
    gregorianCalendar.clear();
    gregorianCalendar.setTime(date);

    this.date = new DlmsDate(gregorianCalendar.get(GregorianCalendar.YEAR),
                             gregorianCalendar.get(GregorianCalendar.MONTH) + 1,
                             gregorianCalendar.get(GregorianCalendar.DAY_OF_MONTH));

    this.time = new DlmsTime(gregorianCalendar.get(GregorianCalendar.HOUR_OF_DAY),
                             gregorianCalendar.get(GregorianCalendar.MINUTE),
                             gregorianCalendar.get(GregorianCalendar.SECOND),
                             gregorianCalendar.get(GregorianCalendar.MILLISECOND) / 10);

    this.deviation = deviation;
    this.clockStatus = 0;
  }

  /**
   * Creates a DLMS-Timestamp from the calendar object.<P>
   * The deviation will be calculated using the ZONE_OFFSET and the DST_OFFSET from the calendar object.
   *
   * @param calendar
   */
  public DlmsDateTime(Calendar calendar, boolean setDstFlagIfDstActive)
  {

    this.date = new DlmsDate(calendar.get(GregorianCalendar.YEAR),
                             calendar.get(GregorianCalendar.MONTH) + 1, calendar.get(
            GregorianCalendar.DAY_OF_MONTH));

    this.time = new DlmsTime(calendar.get(GregorianCalendar.HOUR_OF_DAY),
                             calendar.get(GregorianCalendar.MINUTE), calendar.get(GregorianCalendar.SECOND),
                             calendar.get(GregorianCalendar.MILLISECOND) / 10);

    this.deviation = (calendar.get(Calendar.ZONE_OFFSET) + calendar.get(Calendar.DST_OFFSET)) / 1000 / 60;
    if (calendar.get(Calendar.DST_OFFSET) != 0 && setDstFlagIfDstActive)
    {
      this.clockStatus = CLOCK_STATUS_BIT_DAYLIGHT_SAVING_ACTIVE;
    }
    else
    {
      this.clockStatus = 0;
    }
  }

  public byte[] toBytes()
  {
    byte[] value = new byte[12];

    System.arraycopy(date.toBytes(), 0, value, 0, 5);
    System.arraycopy(time.toBytes(), 0, value, 5, 4);

    value[9] = (byte)(0xFF & (deviation >>> 8));
    value[10] = (byte)(0xFF & deviation);
    value[11] = (byte)clockStatus;

    return value;
  }

  /**
   * Returns true if the date is complete defined.<P>
   *
   * @return  True if the date is complete defined
   */
  public boolean isRegular()
  {
    return (date.isRegular() && time.isRegular());
  }

  /**
   * Returns true if the date is complete defined and the deviation is set.<P>
   *
   * @return  True if the date is complete defined and the deviation is set
   */
  public boolean isRegularUtc()
  {
    return isRegular() && deviation != DEVIATION_NOT_SPECIFIED;
  }

  /**
   * Returns the UTC date
   *
   * @return The UTC Date or {@code null} if this date is not regular or the deviation is to set.
   */
  public Date getUtcDate()
  {
    if (isRegular() && deviation != DEVIATION_NOT_SPECIFIED)
    {
      return getCalendar().getTime();
    }
    else
    {
      return null;
    }
  }

  /**
   * Returns the local date not respecting the time zone of the device.
   *
   * @return The local date or {@code null} if this date is not regular.
   */
  public Date getLocalDate()
  {
    if (isRegular())
    {
      return new Date((date.getLocalDate().getTime() + time.getLocalTime().getTime()));
    }
    else
    {
      return null;
    }
  }

  /**
   * Returns a calendar representing the date time.<P>
   * If the deviation is specified a appropriate time zone will be created.<P>
   * If the deviation is not specified {@link  #UNDEF_TIMEZONE} will be used as time zone.
   *
   *
   * @return
   */
  public Calendar getCalendar()
  {
    if (!isRegular())
    {
      return null;
    }

    if (deviation == DEVIATION_NOT_SPECIFIED)
    {
      GregorianCalendar cal = new GregorianCalendar(UNDEF_TIMEZONE);
      cal.clear();
      cal.setTime(getLocalDate());
      return cal;
    }
    else
    {
      GregorianCalendar cal = new GregorianCalendar(new SimpleTimeZone(deviation * 60 * 1000, "Device UTC"
                                                                                              + deviation));
      cal.clear();
      cal.set(date.getYear(), date.getMonth() - 1, date.getDayOfMonth(), time.getHour(), time.getMinute(),
              time.getSecond());
      if (time.getHundredths() != DlmsTime.NOT_SPECIFIED)
      {
        cal.set(Calendar.MILLISECOND, time.getHundredths() * 10);
      }

      return cal;
    }
  }

  public int getClockStatus()
  {
    return clockStatus;
  }

  public Set<ClockStatus> getClockStatusSet()
  {
    final EnumSet<ClockStatus> result =
            EnumSet.noneOf(ClockStatus.class);

    if (clockStatus != 0) //If 0 -> nothing to add.
    {
      for (ClockStatus s : ClockStatus.values())
      {
        if ((s.getBitMask() & clockStatus) != 0)
        {
          result.add(s);
        }
      }
    }

    return result;
  }

  public DlmsDate getDlmsDate()
  {
    return date;
  }

  public int getDeviation()
  {
    return deviation;
  }

  public DlmsTime getDlmsTime()
  {
    return time;
  }

  /**
   * String representation of this time.
   * <P>
   * 
   * Format: EEE,yyyy-MM-ddThh:mm:ss.SSzz[Flags]<br><br>
   * 
   * EEE: The day of the week (Mon,Tue,Wen,Thu,Fri,Sat or Sun). If the day of the week is undefined it will be omitted.<br>
   * yyyy: The year or "*" if the year is undefined.<br>
   * MM:  The month (01..12),"DST_begin" for daylight savings begin,"DST_end" for daylight savings end or "*" if the month is undefined<br>
   * dd:  The day (01..31),"last" for last day of month, "2nd_last" for second last day of month or "*" if the day is undefined<br><br>
   * 
   * hh: The hours (00..23)  or "*" if the hours are undefined<br>
   * mm: The minutes (00..59) or "*" if the minutes are undefined<br>
   * ss: The seconds (00..59) or "*" if the seconds are undefined<br>
   * SS: The hundredths (00..99). They will be omitted if they are undefined.<br><br>
   * 
   * zz: Deviation in the "+-hh:mm" Format. (Will be omitted if the deviation is undefined.<br><br>
   * 
   * Flags: a comma separated list of flags. If no flags are active the list inclusive "[]" will be omitted.
   * 
   * <P>
   * Flags:<br>
   * <br>
   * IV: invalid value<br>
   * DV: doubtful value<br>
   * DCB: different clock base<br>
   * ICS: invalid clock status<br>
   * DST: daylight saving active<br>
   * 
   * 
   * @return The string representation
   */
  public String stringValue()
  {
    final StringBuilder sb = new StringBuilder();
    sb.append(date.stringValue());
    sb.append(",");
    sb.append(time.stringValue());

    final DecimalFormat df2 = new DecimalFormat("00");

    if (deviation != DlmsDateTime.DEVIATION_NOT_SPECIFIED)
    {
      //sb.append(" ");
      int dev = deviation;
      if (dev >= 0)
      {
        sb.append("+");
      }
      else
      {
        sb.append("-");
        dev = -dev;
      }
      final int h = dev / 60;
      final int m = dev % 60;
      sb.append(df2.format(h));
      sb.append(":");
      sb.append(df2.format(m));
    }

    if (clockStatus != 0)
    {
      sb.append("[");
      boolean first = true;
      for (ClockStatus s : getClockStatusSet())
      {
        if (!first)
        {
          sb.append(",");
        }
        sb.append(s.getShortName());
        first = false;
      }
      sb.append("]");
    }

    return sb.toString();
  }

  @Override
  public String toString()
  {
    String strDeviation = deviation == DEVIATION_NOT_SPECIFIED ? "not spec." : Integer.toString(deviation);

    return "DlmsDateTime{" + "date=" + date + ", time=" + time + ", deviation=" + strDeviation
           + ", clockStatus=" + clockStatus + " " + getClockStatusSet().toString() + '}';
  }

  @Override
  public boolean equals(Object obj)
  {
    if (obj == null)
    {
      return false;
    }
    if (getClass() != obj.getClass())
    {
      return false;
    }
    final DlmsDateTime other = (DlmsDateTime)obj;
    if (this.date != other.date && (this.date == null || !this.date.equals(other.date)))
    {
      return false;
    }
    if (this.time != other.time && (this.time == null || !this.time.equals(other.time)))
    {
      return false;
    }
    if (this.deviation != other.deviation)
    {
      return false;
    }
    if (this.clockStatus != other.clockStatus)
    {
      return false;
    }
    return true;
  }

  public boolean equalsIgnoreDayOfWeekAndClockStatus(DlmsDateTime other)
  {
    if (this.date != other.date && (this.date == null || !this.date.equalsIgnoreDayOfWeek(other.date)))
    {
      return false;
    }
    if (this.time != other.time && (this.time == null || !this.time.equals(other.time)))
    {
      return false;
    }
    if (this.deviation != other.deviation)
    {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode()
  {
    int hash = 7;
    hash = 47 * hash + (this.date != null ? this.date.hashCode() : 0);
    hash = 47 * hash + (this.time != null ? this.time.hashCode() : 0);
    hash = 47 * hash + this.deviation;
    hash = 47 * hash + this.clockStatus;
    return hash;
  }

}
