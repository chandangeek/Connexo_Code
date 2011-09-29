/* File:        
 * $HeadURL: http://deosn1-svnsv1.kromschroeder.elster-group.com/svn/eWorkPad/trunk/Libraries/ElsterAgrImport/src/com/elster/agrimport/agrreader/AgrReaderUtils.java $
 * Version:     
 * $Id: AgrReaderUtils.java 1883 2010-08-16 09:20:17Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  22.07.2010 10:37:52
 */
package com.elster.agrimport.agrreader;

import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

/**
 * Utilities for the AgrReader
 *
 * @author osse
 */
public class AgrReaderUtils {
    private static Map<String, Locale> localeMap = null;
    private static TimeZone deviceTimeZone = new SimpleTimeZone(0, "unknownDeviceTimeZone");

    /**
     * Returns the an DateFormat to interpret AGR dates.
     * <p/>
     * The format of the locale name must be as described for {@code Locale.toString()}. If the locale name
     * is {@code null} or empty the default local will be used.
     * <p/>
     * The format can be provided be the {@code format} parameter. If the format is {@code null} or empty the
     * format will be determined from the locale.<P>
     * An time zone with an raw offset of 0 will be applied.
     * <p/>
     * If special am and the pm strings should be used they must be both provided. Otherwise they will be ignored.
     *
     * @param localeName        The name of the locale, an empty string or {@code null}
     * @param dateFormatPattern An format string for the date as described for {@code SimpleDateFormat }, an empty string or {@code null}
     * @param timeFormatPattern An format string for the time as described for {@code SimpleDateFormat }, an empty string or {@code null}
     * @param amString          The am string, an empty string or {@code null}
     * @param pmString          The pm string, an empty string or {@code null}
     * @return An DateFormat or {@code null } if the locale was not found.
     */
    public static DateFormat createDateFormat(String localeName, String dateFormatPattern,
                                              String timeFormatPattern, String amString,
                                              String pmString) {
        Locale locale = findLocale(localeName);

        if (locale == null) {
            return null;
        }

        //DateFormatSymbols dateFormatSymbols = (DateFormatSymbols) DateFormatSymbols.getInstance(locale).clone();
        DateFormatSymbols dateFormatSymbols = new DateFormatSymbols(locale);

        applyAmPm(dateFormatSymbols, amString, pmString);

        SimpleDateFormat dateFormat;
        SimpleDateFormat dateFormatDateOnly;

        if ((dateFormatPattern == null) || (dateFormatPattern.length() == 0)) {
            dateFormatPattern = buildExtendedDateformatPattern(locale);
        }

        if ((timeFormatPattern == null) || (timeFormatPattern.length() == 0)) {
            timeFormatPattern = ((SimpleDateFormat) DateFormat.getTimeInstance(DateFormat.MEDIUM,
                    locale)).toPattern();
        }

        dateFormat = new SimpleDateFormat(dateFormatPattern + " " + timeFormatPattern, dateFormatSymbols);
        dateFormatDateOnly = new SimpleDateFormat(dateFormatPattern, dateFormatSymbols);

        dateFormat.setTimeZone(deviceTimeZone);
        dateFormatDateOnly.setTimeZone(deviceTimeZone);

        return new AgrDateFormat(dateFormat, dateFormatDateOnly);
    }

    /**
     * Returns a (modified) date format pattern for the specified locale.<P>
     * If the date format pattern contains a two digit year it will be replaced by
     * an 4 digit year.
     *
     * @param locale The locale
     * @return The date format pattern.
     */
    private static String buildExtendedDateformatPattern(Locale locale) {
        String dateFormatPattern = ((SimpleDateFormat) DateFormat.getDateInstance(DateFormat.SHORT,
                locale)).toPattern();
        if (dateFormatPattern.contains("yy") && dateFormatPattern.indexOf("yy") == dateFormatPattern.
                lastIndexOf("yy")) {
            dateFormatPattern.replace("yy", "yyyy");
        }

        return dateFormatPattern;
    }

    /**
     * Returns the locale for an name.
     * <p/>
     * If {@code localeName} is null or empty the default locale will be returned.
     * <p/>
     * If {@code localeName} is specified and the locale was not found null will be returned
     *
     * @param localeName The name of the locale as described for {@code Locale.toString()}
     * @return The Locale or null.
     */
    private static synchronized Locale findLocale(String localeName) {
        Locale locale = null;

        //--- Locale festlegen ---
        if ((localeName != null) && (localeName.length() > 0)) {
            if (localeMap == null) {
                localeMap = new HashMap<String, Locale>();

                for (Locale l : Locale.getAvailableLocales()) {
                    localeMap.put(l.toString(), l);
                }
            }

            locale = localeMap.get(localeName);

            if (locale == null) {
                return null;
            }
        }

        if (locale == null) {
            locale = Locale.getDefault();
        }

        return locale;
    }

    /**
     * Applies the {@code amString} and the {@code pmString} to the {@code dateFormatSymbols}
     *
     * @param dateFormatSymbols The DateFormatSymbols change.
     * @param amString          The new am string. If the string is empty or null the am string in the DateFormatSymbols will not be changed.
     * @param pmString          The new pm string. If the string is empty or null the pm string in the DateFormatSymbols will not be changed.
     */
    private static void applyAmPm(DateFormatSymbols dateFormatSymbols, String amString, String pmString) {

        if (((amString == null) || (amString.length() == 0)) &&
                ((pmString == null) || (pmString.length() == 0))) {
            //no change required.
            return;
        }

        String[] ampm = new String[2];

        if (dateFormatSymbols.getAmPmStrings().length >= 2) {
            ampm[0] = dateFormatSymbols.getAmPmStrings()[0];
            ampm[1] = dateFormatSymbols.getAmPmStrings()[1];
        }

        if ((amString != null) && (amString.length() > 0)) {
            ampm[0] = amString;
        }

        if ((pmString != null) && (pmString.length() > 0)) {
            ampm[1] = pmString;
        }

        dateFormatSymbols.setAmPmStrings(ampm);
    }

    /**
     * DateFormat with special handling of the "00:00:00" timestamp<P>
     * At "00:00:00" the country specific format must be ignored, because it is written this
     * way to the AGR files..
     */
    private static class AgrDateFormat extends DateFormat {
        @Override
        public Object clone() {
            return new AgrDateFormat((DateFormat) dateAndTime.clone(), (DateFormat) dateOnly.clone());
        }

        private final DateFormat dateAndTime;
        private final DateFormat dateOnly;

        public AgrDateFormat(DateFormat dateAndTime, DateFormat dateOnly) {
            super();
            this.dateAndTime = dateAndTime;
            this.dateOnly = dateOnly;
            setCalendar(dateAndTime.getCalendar());
        }

        @Override
        public StringBuffer format(Date date, StringBuffer toAppendTo, FieldPosition fieldPosition) {
            return dateAndTime.format(date, toAppendTo, fieldPosition);
        }

        @Override
        public Date parse(String source, ParsePosition pos) {
            if (source.contains(" 00:00:00")) {
                int index = pos.getIndex();
                int errorIndex = pos.getErrorIndex();

                Date date = dateOnly.parse(source, pos);

                if (pos.getIndex() != 0 && source.indexOf(" 00:00:00") == pos.getIndex()) {
                    pos.setIndex(pos.getIndex() + 9);
                    return date;
                }
                //vorigen Zustand wieder herstellen.
                pos.setErrorIndex(errorIndex);
                pos.setIndex(index);
            }
            return dateAndTime.parse(source, pos);
        }

        @Override
        public void setTimeZone(TimeZone zone) {
            super.setTimeZone(zone);
            dateAndTime.setTimeZone(zone);
            dateOnly.setTimeZone(zone);
        }

        @Override
        public void setNumberFormat(NumberFormat newNumberFormat) {
            super.setNumberFormat(newNumberFormat);
            dateAndTime.setNumberFormat(newNumberFormat);
            dateOnly.setNumberFormat(newNumberFormat);
        }

        @Override
        public void setLenient(boolean lenient) {
            super.setLenient(lenient);
            dateAndTime.setLenient(lenient);
            dateOnly.setLenient(lenient);
        }

        @Override
        public void setCalendar(Calendar newCalendar) {
            super.setCalendar(newCalendar);
            dateAndTime.setCalendar(newCalendar);
            dateOnly.setCalendar(newCalendar);
        }

    }

}
