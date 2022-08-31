package com.energyict.mdc.protocol.inbound.mbus.parser.telegrams.util;


public class DateCalculator {

    private static int SECOND_MASK = 0x3F;		// 0011 1111
    private static int MINUTE_MASK = DateCalculator.SECOND_MASK;	// 0011 1111
    private static int HOUR_MASK = 0x1F;		// 0001 1111
    private static int DAY_MASK = 0x1F;			// 0001 1111
    private static int MONTH_MASK = 0x0F;		// 0000 1111
    private static int YEAR_MASK = 0xE0;		// 1110 0000
    private static int YEAR_MASK_2 = 0xF0;		// 1111 0000
    private static int HUNDERT_YEAR_MASK = 0xC0;// 1100 0000
    private static int WEEK_DAY = 0xE0;			// 1110 0000
    private static int WEEK = 0x3F;				// 0011 1111
    private static int TIME_INVALID = 0x80;		// 1000 0000
    private static int SOMMERTIME = 0x40;		// 0100 0000
    private static int LEAP_YEAR = 0x80;		// 1000 0000
    private static int DIF_SOMMERTIME = 0xC0;	// 1100 0000

    public static String getTimeWithSeconds(String secondValue, String minuteValue, String hourValue) {
        String time = DateCalculator.getTime(minuteValue, hourValue);
        time = time + ":" + DateCalculator.getSeconds(Converter.hexToInt(secondValue));

        return time;
    }

    public static String getTime(String minuteValue, String hourValue) {
        String time = String.valueOf(DateCalculator.getHour(Converter.hexToInt(hourValue)));
        time = time + ":" + DateCalculator.getMinutes(Converter.hexToInt(minuteValue));

        return time;
    }

    public static String getDate(String dayValue, String monthValue, boolean calcHundertYear) {
        String date = String.valueOf(DateCalculator.getDay(Converter.hexToInt(dayValue)));
        date = date + "." + DateCalculator.getMonth(Converter.hexToInt(monthValue));
        date = date + "." + DateCalculator.getYear(Converter.hexToInt(dayValue), Converter.hexToInt(monthValue), 0, false);

        return date;
    }

    public static String getDateTime(String minuteValue, String hourValue, String dayValue, String monthValue, boolean calcHundertYear) {
        String date = DateCalculator.getDate(dayValue, monthValue, calcHundertYear);
        date = date + " " + DateCalculator.getTime(minuteValue, hourValue);

        return date;
    }

    public static String getDateTimeWithSeconds(String seconds, String minuteValue, String hourValue, String dayValue, String monthValue, boolean calcHundertYear) {
        String date = DateCalculator.getDate(dayValue, monthValue, calcHundertYear);
        date = date + " " + DateCalculator.getTimeWithSeconds(seconds, minuteValue, hourValue);

        return date;
    }

    public static int getSeconds(int secondValue) {
        int seconds = secondValue & DateCalculator.SECOND_MASK;
        return seconds;
    }

    public static int getMinutes(int minuteValue) {
        int minutes = minuteValue & DateCalculator.MINUTE_MASK;
        return minutes;
    }

    public static int getHour(int hourValue) {
        int hour = hourValue & DateCalculator.HOUR_MASK;
        return hour;
    }

    public static int getDay(int dayValue) {
        int day = dayValue & DateCalculator.DAY_MASK;
        return day;
    }

    public static int getMonth(int monthValue) {
        int month = monthValue & DateCalculator.MONTH_MASK;
        return month;
    }

    public static int getYear(int yearValue1, int yearValue2, int hundertYearValue, boolean calcHundertYear) {
        int year1 = yearValue1 & DateCalculator.YEAR_MASK;
        int year2 = yearValue2 & DateCalculator.YEAR_MASK_2;
        int hundertYear = 1;

        // we move the bits of year1 value 4 bits to the right
        // and concat (or) them with year2. Afterwards we have
        // to move the result one bit to the right so that it
        // is at the right position (0xxx xxxx).
        int year = (year2 | (year1 >> 4)) >> 1;
        // to be compatible with older meters it is recommended to interpret the
        // years 0 to 80 as 2000 to 2080. Only year values in between 0 and 99
        // should be used

        // another option is to calculate the hundert year value (in new meters)
        // from a third value the hundert year is generated and calculated
        // the year is then calculated according to following formula:
        // year = 1900 + 100 * hundertYear + year;
        if(calcHundertYear == true) {
            // We have to load the hundert year format as well
            hundertYear = (hundertYearValue & DateCalculator.HUNDERT_YEAR_MASK) >> 6;
            year = 1900 + (100 * hundertYear) + year;
        }
        else {
            if(year < 81) {
                year = 2000 + year;
            }
            else {
                year = 1900 + year;
            }
        }

        return year;
    }
}