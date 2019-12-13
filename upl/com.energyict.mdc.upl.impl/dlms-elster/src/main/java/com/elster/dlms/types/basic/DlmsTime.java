/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/types/basic/DlmsTime.java $
 * Version:     
 * $Id: DlmsTime.java 6737 2013-06-12 07:16:31Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  20.05.2010 12:21:46
 */
package com.elster.dlms.types.basic;

import java.text.DecimalFormat;
import java.util.Date;

/**
 * This class implements the DLMS time data type.
 * <P>
 * Objects of this class are immutable and therefore thread-safe
 *
 * @author osse
 */
public final class DlmsTime
{
  public static final int NOT_SPECIFIED = 0xFF;
  private final int hour;
  private final int minute;
  private final int second;
  private final int hundredths;
  //--
  /**
   * Date with all attributes undefined.
   */
  public static final DlmsTime NOT_SPECIFIED_TIME =
          new DlmsTime(NOT_SPECIFIED, NOT_SPECIFIED, NOT_SPECIFIED, NOT_SPECIFIED);

  public DlmsTime(final byte[] value)
  {
    if (value.length != 4)
    {
      throw new IllegalArgumentException("The length of the array must be 4.");
    }

    hour = 0xFF & value[0];
    minute = 0xFF & value[1];
    second = 0xFF & value[2];
    hundredths = 0xFF & value[3];
  }

  public DlmsTime(final int hour, final int minute, final int second, final int hundredths)
  {
    this.hour = hour;
    this.minute = minute;
    this.second = second;
    this.hundredths = hundredths;
  }

  public byte[] toBytes()
  {
    return new byte[]
            {
              (byte)hour,
              (byte)minute,
              (byte)second,
              (byte)hundredths
            };
  }

  /**
   * Returns true if the date is complete defined.<P>
   * (The hundredths field can be undefined).
   * 
   * @return  True if the date is complete defined
   */
  public boolean isRegular()
  {
    return (hour != NOT_SPECIFIED && minute != NOT_SPECIFIED && second != NOT_SPECIFIED);
  }

  /**
   * Returns the local time not respecting the time zone of the device.
   *
   * @return The local time or {@code null} if this time is not regular.
   */
  public Date getLocalTime()
  {
    if (!isRegular())
    {
      return null;
    }
    else if (hundredths == NOT_SPECIFIED)
    {
      return new Date((long)hour * 60 * 60 * 1000 + minute * 60 * 1000 + second * 1000);
    }
    else
    {
      return new Date((long)hour * 60 * 60 * 1000 + minute * 60 * 1000 + second * 1000 + hundredths * 10);
    }
  }

  public int getHour()
  {
    return hour;
  }

  public int getHundredths()
  {
    return hundredths;
  }

  public int getMinute()
  {
    return minute;
  }

  public int getSecond()
  {
    return second;
  }

    /**
   * String representation of this time.
   * <P>
   * 
   * Format hh:mm:ss.SS<br><br>
   * 
   * hh: The hours (00..23)  or "*" if the hours are undefined<br>
   * mm: The minutes (00..59) or "*" if the minutes are undefined<br>
   * ss: The seconds (00..59) or "*" if the seconds are undefined<br>
   * SS: The hundredths (00..99). They will be omitted if they are undefined.<br>
   * 
   * @return The string representation
   */
  public String stringValue()
  {
    final DecimalFormat df2 = new DecimalFormat("00");
    final StringBuilder sb = new StringBuilder();

    sb.append(hour == DlmsTime.NOT_SPECIFIED ? "*" : df2.format(hour));
    sb.append(":");
    sb.append(minute == DlmsTime.NOT_SPECIFIED ? "*" : df2.format(minute));
    sb.append(":");
    sb.append(second == DlmsTime.NOT_SPECIFIED ? "*" : df2.format(second));

    if (hundredths != DlmsTime.NOT_SPECIFIED)
    {
      sb.append(".");
      sb.append(df2.format(hundredths));
    }
    return sb.toString();

  }

  @Override
  public String toString()
  {
    final String notSpec = "NOT_SPECIFIED";
    final String strHour = hour == NOT_SPECIFIED ? notSpec : Integer.toString(hour);
    final String strMinute = minute == NOT_SPECIFIED ? notSpec : Integer.toString(minute);
    final String strSecond = second == NOT_SPECIFIED ? notSpec : Integer.toString(second);
    final String strHundrets = hundredths == NOT_SPECIFIED ? notSpec : Integer.toString(hundredths);

    return "DlmsTime{" + "hour=" + strHour + ", minute=" + strMinute + ", second=" + strSecond
           + ", hundredths=" + strHundrets + '}';
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
    final DlmsTime other = (DlmsTime)obj;
    if (this.hour != other.hour)
    {
      return false;
    }
    if (this.minute != other.minute)
    {
      return false;
    }
    if (this.second != other.second)
    {
      return false;
    }
    if (this.hundredths != other.hundredths)
    {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode()
  {
    int hash = 7;
    hash = 97 * hash + this.hour;
    hash = 97 * hash + this.minute;
    hash = 97 * hash + this.second;
    hash = 97 * hash + this.hundredths;
    return hash;
  }

}
