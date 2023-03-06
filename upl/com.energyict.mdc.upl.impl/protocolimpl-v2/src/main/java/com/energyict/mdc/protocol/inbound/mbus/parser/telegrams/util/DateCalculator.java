package com.energyict.mdc.protocol.inbound.mbus.parser.telegrams.util;


import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

public class DateCalculator {

    private static final long EPOCH_2013_UNIX = 1356998400; // January 1, 2013

    private static final int SECOND_MASK = 0x3F;		// 0011 1111
    private static final int MINUTE_MASK = DateCalculator.SECOND_MASK;	// 0011 1111
    private static final int HOUR_MASK = 0x1F;		    // 0001 1111
    private static final int DAY_MASK = 0x1F;			// 0001 1111
    private static final int MONTH_MASK = 0x0F;		    // 0000 1111
    private static final int YEAR_MASK = 0xE0;		    // 1110 0000
    private static final int YEAR_MASK_2 = 0xF0;		// 1111 0000
    private static final int HUNDRED_YEAR_MASK = 0xC0;  // 1100 0000
    private static final int WEEK_DAY = 0xE0;			// 1110 0000
    private static final int WEEK = 0x3F;				// 0011 1111
    private static final int TIME_INVALID = 0x80;		// 1000 0000
    private static final int SUMMERTIME = 0x40;		    // 0100 0000
    private static final int LEAP_YEAR = 0x80;		    // 1000 0000
    private static final int DIF_SUMMERTIME = 0xC0;	    // 1100 0000

    public static Instant getTimeWithSeconds(String secondValue, String minuteValue, String hourValue) {
        Instant timeInstant = DateCalculator.getTime(minuteValue, hourValue);
        int seconds = DateCalculator.getSeconds(Converter.hexToInt(secondValue));

        return timeInstant.atZone(ZoneOffset.UTC)
                .withSecond(seconds)
                .toInstant();
    }

    public static Instant getTime(String minuteValue, String hourValue) {
        int hour = DateCalculator.getHour(Converter.hexToInt(hourValue));
        int minutes = DateCalculator.getMinutes(Converter.hexToInt(minuteValue));

        return Instant.now().atZone(ZoneOffset.UTC)
                .withHour(hour)
                .withMinute(minutes)
                .withSecond(0)
                .withNano(0)
                .toInstant();
    }

    public static Instant getDate(String dayValue, String monthValue, boolean calcHundertYear) {
        int day = DateCalculator.getDay(Converter.hexToInt(dayValue));
        int month = DateCalculator.getMonth(Converter.hexToInt(monthValue));
        int year = DateCalculator.getYear(Converter.hexToInt(dayValue), Converter.hexToInt(monthValue), 0, false);

        return Instant.now().atZone(ZoneOffset.UTC)
                .withDayOfMonth(day)
                .withMonth(month)
                .withYear(year)
                .withHour(0)
                .withMinute(0)
                .withSecond(0)
                .withNano(0)
                .toInstant();
    }

    public static Instant getDateTime(String minuteValue, String hourValue, String dayValue, String monthValue, boolean calcHundertYear) {
        Instant dateInstant = DateCalculator.getDate(dayValue, monthValue, calcHundertYear);

        Instant timeInstant = DateCalculator.getTime(minuteValue, hourValue);
        LocalDateTime time = LocalDateTime.ofInstant(timeInstant, ZoneId.of("UTC"));

        return dateInstant.atZone(ZoneOffset.UTC)
                .withHour(time.getHour())
                .withMinute(time.getMinute())
                .withSecond(time.getSecond())
                .toInstant();

    }

    public static Instant getDateTimeWithSeconds(String seconds, String minuteValue, String hourValue, String dayValue, String monthValue, boolean calcHundertYear) {
        Instant dateInstant = DateCalculator.getDate(dayValue, monthValue, calcHundertYear);

        Instant timeInstant = DateCalculator.getTimeWithSeconds(seconds, minuteValue, hourValue);
        LocalDateTime time = LocalDateTime.ofInstant(timeInstant, ZoneId.of("UTC"));

        return dateInstant.atZone(ZoneOffset.UTC)
                .withHour(time.getHour())
                .withMinute(time.getMinute())
                .withSecond(time.getSecond())
                .toInstant();
    }

    public static int getSeconds(int secondValue) {
        return secondValue & DateCalculator.SECOND_MASK;
    }

    public static int getMinutes(int minuteValue) {
        return minuteValue & DateCalculator.MINUTE_MASK;
    }

    public static int getHour(int hourValue) {
        return hourValue & DateCalculator.HOUR_MASK;
    }

    public static int getDay(int dayValue) {
        return dayValue & DateCalculator.DAY_MASK;
    }

    public static int getMonth(int monthValue) {
        return monthValue & DateCalculator.MONTH_MASK;
    }

    public static int getYear(int yearValue1, int yearValue2, int hundertYearValue, boolean calcHundertYear) {
        int year1 = yearValue1 & DateCalculator.YEAR_MASK;
        int year2 = yearValue2 & DateCalculator.YEAR_MASK_2;
        int hundredYear;

        // we move the bits of year1 value 4 bits to the right
        // and concat (or) them with year2. Afterwards we have
        // to move the result one bit to the right so that it
        // is at the right position (0xxx xxxx).
        int year = (year2 | (year1 >> 4)) >> 1;
        // to be compatible with older meters it is recommended to interpret the
        // years 0 to 80 as 2000 to 2080. Only year values in between 0 and 99
        // should be used

        // another option is to calculate the hundred-year value (in new meters)
        // from a third value the hundred year is generated and calculated
        // the year is then calculated according to following formula:
        // year = 1900 + 100 * hundredYear + year;
        if (calcHundertYear) {
            // We have to load the hundred-year format as well
            hundredYear = (hundertYearValue & DateCalculator.HUNDRED_YEAR_MASK) >> 6;
            year = 1900 + (100 * hundredYear) + year;
        } else {
            if (year < 81) {
                year = 2000 + year;
            } else {
                year = 1900 + year;
            }
        }

        return year;
    }

    /** Epoch time used in Daily payload
     unsigned long  int  Date=drvrtc_getEpochTimestamp();

     pui8buffer[DRVNB1_NBIoT_DATE_AND_TIME]       = 0xE5;
     pui8buffer[DRVNB1_NBIoT_DATE_AND_TIME + 1]   = (unsigned char )Date;
     pui8buffer[DRVNB1_NBIoT_DATE_AND_TIME + 2]   = (unsigned char ) (Date>>8);
     pui8buffer[DRVNB1_NBIoT_DATE_AND_TIME + 3]   = (unsigned char ) (Date>>16);
     pui8buffer[DRVNB1_NBIoT_DATE_AND_TIME + 4]   = (unsigned char ) (Date>>24);
     pui8buffer[DRVNB1_NBIoT_DATE_AND_TIME + 5]   = 0x20;
     */
    public static Instant getEpochTime(String byte0, String byte1, String byte2, String byte3, String byte4, String byte5) {
        int header = Converter.hexToInt(byte0); // expect 0xE5
        int date1 = Converter.hexToInt(byte1);
        int date2 = Converter.hexToInt(byte2);
        int date3 = Converter.hexToInt(byte3);
        int date4 = Converter.hexToInt(byte4);
        int tail = Converter.hexToInt(byte5); // expect 0x20

        int epochTimeShort = date4 * 0x1000000 + date3 * 0x10000 + date2 * 0x100 + date1;
        long epochTime = EPOCH_2013_UNIX + epochTimeShort;

        return Instant.ofEpochMilli(epochTime * 1000);
    }
}