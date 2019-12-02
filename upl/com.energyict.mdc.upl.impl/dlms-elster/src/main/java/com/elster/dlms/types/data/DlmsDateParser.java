/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/types/data/DlmsDateParser.java $
 * Version:     
 * $Id: DlmsDateParser.java 4024 2012-02-17 13:48:12Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  Apr 28, 2011 11:21:58 AM
 */
package com.elster.dlms.types.data;

import com.elster.dlms.types.basic.DlmsDate;
import com.elster.dlms.types.basic.DlmsDateTime;
import com.elster.dlms.types.basic.DlmsTime;
import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

/**
 * Parser for {@code DlmsDate} objects.
 *
 *
 * @author osse
 */
public final class DlmsDateParser
{
  private static final DecimalFormat DF4 = new DecimalFormat("0000");
  //private static final DecimalFormat DF3 = new DecimalFormat("000");
  private static final DecimalFormat DF2 = new DecimalFormat("00");
  private static final DlmsDateParser INSTANCE = new DlmsDateParser();
  private final DateFormatSymbols dateFormatSymbols;

  private DlmsDateParser()
  {
    dateFormatSymbols = new DateFormatSymbols();
  }

  private DlmsDateParser(final Locale locale)
  {
    dateFormatSymbols = new DateFormatSymbols(locale);
  }

  /**
   * Returns the parser instance.
   *
   */
  public static DlmsDateParser getInstance()
  {
    return INSTANCE;
  }

  public static DlmsDateParser getInstance(final Locale locale)
  {
    return new DlmsDateParser(locale);
  }

  /**
   * Formates the specified date.<P>
   *
   *
   * @param date The date
   * @return A string representing the date.
   */
  public String formatDlmsDate(final DlmsDate date)
  {
    final StringBuilder sb = new StringBuilder();


    if (date.getDayOfWeek() != DlmsDate.DAY_OF_WEEK_NOT_SPECIFIED)
    {
      sb.append(dayOfWeekToString(date.getDayOfWeek()));
      sb.append(", ");
    }

    switch (date.getYear())
    {
      case DlmsDate.YEAR_NOT_SPECIFIED:
        sb.append("____");
        break;
      default:
        sb.append(DF4.format(date.getYear()));
    }

    sb.append("-");

    switch (date.getMonth())
    {
      case DlmsDate.MONTH_DAYLIGHT_SAVINGS_BEGIN:
        sb.append(" daylight savings begin ");
        break;
      case DlmsDate.MONTH_DAYLIGHT_SAVINGS_END:
        sb.append(" daylight savings end ");
        break;
      case DlmsDate.MONTH_NOT_SPECIFIED:
        sb.append("__");
        break;
      default:
        sb.append(DF2.format(date.getMonth()));
    }
    sb.append("-");


    switch (date.getDayOfMonth())
    {
      case DlmsDate.DAY_OF_MONTH_2ND_LAST_DAY_OF_MONTH:
        sb.append(" 2nd last day of month ");
        break;
      case DlmsDate.DAY_OF_MONTH_LAST_DAY_OF_MONTH:
        sb.append(" last day of month ");
        break;
      case DlmsDate.DAY_OF_MONTH_NOT_SPECIFIED:
        sb.append("__");
        break;
      default:
        sb.append(DF2.format(date.getDayOfMonth()));
    }

    return sb.toString();
  }

  public String formatDlmsDateTime(final DlmsDateTime dateTime)
  {
    final StringBuilder sb = new StringBuilder();

    sb.append(formatDlmsDate(dateTime.getDlmsDate()));
    sb.append(" ");
    sb.append(formatDlmsTime(dateTime.getDlmsTime()));
    if (dateTime.getDeviation() != DlmsDateTime.DEVIATION_NOT_SPECIFIED)
    {
      //sb.append(" ");
      int dev = dateTime.getDeviation();
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
      sb.append(DF2.format(h));
      sb.append(":");
      sb.append(DF2.format(m));
    }

    return sb.toString();
  }

  public String formatDlmsTime(final DlmsTime time)
  {
    final StringBuilder sb = new StringBuilder();

    switch (time.getHour())
    {
      case DlmsTime.NOT_SPECIFIED:
        sb.append("__");
        break;
      default:
        sb.append(DF2.format(time.getHour()));
    }

    sb.append(":");

    switch (time.getMinute())
    {
      case DlmsTime.NOT_SPECIFIED:
        sb.append("__");
        break;
      default:
        sb.append(DF2.format(time.getMinute()));
    }


    sb.append(":");

    switch (time.getSecond())
    {
      case DlmsTime.NOT_SPECIFIED:
        sb.append("__");
        break;
      default:
        sb.append(DF2.format(time.getSecond()));
    }

    if (time.getHundredths() != DlmsTime.NOT_SPECIFIED)
    {
      sb.append(".");
      sb.append(DF2.format(time.getHundredths()));

    }

    return sb.toString();
  }

  /**
   * Return a string with the name of the weekday in the current locale.
   *
   * @param dayOfWeek The day of the week (1=Monday)
   * @return The name for day of the week.
   */
  public String dayOfWeekToString(final int dayOfWeek)
  {
    //final String[] weekdays = DateFormatSymbols.getInstance().getWeekdays();
    final String[] weekdays = dateFormatSymbols.getShortWeekdays();
    String result;
    switch (dayOfWeek)
    {
      case 1:
        result = weekdays[Calendar.MONDAY];
        break;
      case 2:
        result = weekdays[Calendar.TUESDAY];
        break;
      case 3:
        result = weekdays[Calendar.WEDNESDAY];
        break;
      case 4:
        result = weekdays[Calendar.THURSDAY];
        break;
      case 5:
        result = weekdays[Calendar.FRIDAY];
        break;
      case 6:
        result = weekdays[Calendar.SATURDAY];
        break;
      case 7:
        result = weekdays[Calendar.SUNDAY];
        break;
      case DlmsDate.DAY_OF_WEEK_NOT_SPECIFIED:
        result = "-";
        break;
      default:
        result = "???";
    }
    return result;
  }

  public DlmsDate parseDate(final String date, final boolean mustBeRegular) throws ParseException
  {
    final StringTokenizer stringTokenizer = new StringTokenizer(date, "-");
    if (stringTokenizer.countTokens() != 3)
    {
      throw new ParseException("The date must be delimited with '-': " + date, 0);
    }

    final String yStr = stringTokenizer.nextToken().trim();
    final String mStr = stringTokenizer.nextToken().trim();
    final String dStr = stringTokenizer.nextToken().trim();

    final int y = stringToInt(yStr, DlmsDate.YEAR_NOT_SPECIFIED);
    final int m = stringToInt(mStr, DlmsDate.MONTH_NOT_SPECIFIED);
    final int d = stringToInt(dStr, DlmsDate.DAY_OF_MONTH_NOT_SPECIFIED);

    //todo: check for special months an days

    final DlmsDate result = new DlmsDate(y, m, d);

    if (mustBeRegular && !result.isRegular())
    {
      throw new ParseException("The date is not a regular date: " + date, 0);
    }
    return result;
  }

  public DlmsTime parseTime(final String time, final boolean mustBeRegular) throws ParseException
  {
    final StringTokenizer stringTokenizer = new StringTokenizer(time, ":.,");
    if (stringTokenizer.countTokens() < 3 || stringTokenizer.countTokens() > 4)
    {
      throw new ParseException("Wrong time format: " + time, 0);
    }

    final String hStr = stringTokenizer.nextToken().trim();
    final String mStr = stringTokenizer.nextToken().trim();
    final String sStr = stringTokenizer.nextToken().trim();

    String hundretsStr = null;

    if (stringTokenizer.hasMoreTokens())
    {
      hundretsStr = stringTokenizer.nextToken().trim();
    }

    final int h = stringToInt(hStr, DlmsTime.NOT_SPECIFIED);
    final int m = stringToInt(mStr, DlmsTime.NOT_SPECIFIED);
    final int s = stringToInt(sStr, DlmsTime.NOT_SPECIFIED);

    int hundrets = DlmsTime.NOT_SPECIFIED;

    if (hundretsStr != null)
    {
      hundrets = stringToInt(hundretsStr, DlmsTime.NOT_SPECIFIED);
    }

    //todo: check for special months and days

    final DlmsTime result = new DlmsTime(h, m, s, hundrets);

    if (mustBeRegular && !result.isRegular())
    {
      throw new ParseException("The date is not a regular date: " + time, 0);
    }
    return result;
  }

  public DlmsDateTime parseDateTime(String dateTime, final boolean mustBeRegular) throws ParseException
  {
    final String[] splitDay = dateTime.split(",");

    if (splitDay.length == 2)
    {
      //String dayOfWeek= splitDay[0].trim();
      //TODO: do something with the dayOfWeek.
      dateTime = splitDay[1].trim();
    }

    final String[] splitDateTime = dateTime.split("\\s");

    if (splitDateTime.length < 2)
    {
      throw new ParseException("Wrong date time format: " + dateTime, 0);
    }
    final String datePart = splitDateTime[0];
    final String timePartAndDeviation = dateTime.substring(datePart.length());

//    if (timePartAndDeviation.contains("+"))
//    {
//      
//    
//    
//    }

    int deviation = DlmsDateTime.DEVIATION_NOT_SPECIFIED;
    String timePart = timePartAndDeviation;
    final String[] splitTime = timePartAndDeviation.split("[\\+-]");
    if (splitTime.length == 2)
    {
      timePart = splitTime[0];

      final String[] hoursAndMintues = splitTime[1].split(":");

      if (hoursAndMintues.length == 0 || hoursAndMintues.length > 2)
      {
        throw new ParseException("Error in deviation: " + splitTime[1], 0);
      }
      final int h = Integer.parseInt(hoursAndMintues[0]);
      final int m = hoursAndMintues.length < 2 ? 0 : Integer.parseInt(hoursAndMintues[1]);

      deviation = h * 60 + m; //Integer.parseInt(splitTime[1]);

      if (timePartAndDeviation.contains("-"))
      {
        deviation = -1 * deviation;
      }
    }

    return new DlmsDateTime(parseDate(datePart, mustBeRegular), parseTime(timePart, mustBeRegular), deviation,
                            0);
  }

  private static final Pattern UNDEF_PATTERN = Pattern.compile("_+");
  private static final Pattern NUMBER_PATTERN = Pattern.compile("\\d{1,4}");

  private static int stringToInt(final String value, final int undefValue) throws ParseException
  {
    if (UNDEF_PATTERN.matcher(value).matches())
    {
      return undefValue;
    }
    else if (NUMBER_PATTERN.matcher(value).matches())
    {
      return Integer.parseInt(value);
    }
    else
    {
      throw new ParseException("Number not valid: " + value, 0);
    }
  }

}
