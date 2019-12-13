package com.energyict.mdc.engine.offline.core;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * contains format preferences
 *
 * @author Karel
 */
public class FormatPreferences implements Serializable {

    private DecimalFormatSymbols decimalFormatSymbols;
    private int digitGrouping = 0;
    private char listSeparator;
    private String dateFormatPattern;
    private String shortTimeFormatPattern;
    private String longTimeFormatPattern;

    /**
     * creates a new FormatPreferences object
     */
    public FormatPreferences() {
        this(new DecimalFormatSymbols());
    }

    /**
     * creates a new FormatPreferences object
     * with the given decimal format symbols
     *
     * @param decimalFormatSymbols the decimal format symbols to use
     */
    public FormatPreferences(DecimalFormatSymbols decimalFormatSymbols) {
        this.decimalFormatSymbols = decimalFormatSymbols;
        if (decimalFormatSymbols.getDecimalSeparator() == ',') {
            listSeparator = ';';
        } else {
            listSeparator = ',';
        }
    }

    /**
     * initializes the receiver
     *
     * @param digitGrouping          the digit grouping symbol
     * @param listSeparator          the list separator symbol
     * @param dateFormatPattern      the date format pattern
     * @param shortTimeFormatPattern the short time format pattern
     * @param longTimeFormatPattern  the long time format pattern
     */
    public void init(int digitGrouping, char listSeparator, String dateFormatPattern, String shortTimeFormatPattern, String longTimeFormatPattern) {
        this.digitGrouping = digitGrouping;
        this.listSeparator = listSeparator;
        this.dateFormatPattern = dateFormatPattern;
        this.shortTimeFormatPattern = shortTimeFormatPattern;
        this.longTimeFormatPattern = longTimeFormatPattern;
    }

    /**
     * Returns the decimal format symbols
     *
     * @return the decimal format symbols
     */
    public DecimalFormatSymbols getDecimalFormatSymbols() {
        return decimalFormatSymbols;
    }

    /**
     * Returns the digit grouping count
     *
     * @return the digit grouping count
     */
    public int getDigitGrouping() {
        return digitGrouping;
    }

    /**
     * Returns the list separator
     *
     * @return the list separator
     */
    public char getListSeparator() {
        return listSeparator;
    }

    /**
     * Returns the date format for the default locale
     *
     * @return the data format
     */
    public DateFormat getDateFormat() {
        return getDateFormat(null);
    }

    /**
     * Returns the date format for the given <code>Locale</code>
     *
     * @param locale the <code>Locale</code> to use
     * @return the data format
     */
    public DateFormat getDateFormat(Locale locale) {
        if (locale == null) {
            return dateFormatPattern == null ?
                    DateFormat.getDateInstance() :
                    new SimpleDateFormat(dateFormatPattern);
        } else {
            return dateFormatPattern == null ?
                    DateFormat.getDateInstance(DateFormat.MEDIUM, locale) :
                    new SimpleDateFormat(dateFormatPattern, locale);
        }
    }

    /**
     * Returns the time format
     *
     * @param includeSeconds true if seconds must be formatted
     * @return the time format
     */
    public DateFormat getTimeFormat(boolean includeSeconds) {
        String pattern = includeSeconds ? longTimeFormatPattern : shortTimeFormatPattern;
        return
                pattern == null ?
                        DateFormat.getTimeInstance(includeSeconds ? DateFormat.MEDIUM : DateFormat.SHORT) :
                        new SimpleDateFormat(pattern);
    }

    /**
     * Returns the date and time format for the default <code>Locale</code>
     *
     * @param includeSeconds true if seconds must be included
     * @return the date and time format
     */
    public DateFormat getDateTimeFormat(boolean includeSeconds) {
        return getDateTimeFormat(includeSeconds, null);
    }

    /**
     * Returns the date and time format for the given <code>Locale</code>
     *
     * @param includeSeconds true if seconds must be included
     * @param locale         the <code>Locale</code> to use     *
     * @return the date and time format
     */
    public DateFormat getDateTimeFormat(boolean includeSeconds, Locale locale) {
        String pattern = includeSeconds ? longTimeFormatPattern : shortTimeFormatPattern;
        if (locale == null) {
            return
                    (dateFormatPattern == null || pattern == null) ?
                            DateFormat.getDateTimeInstance() :
                            new SimpleDateFormat(dateFormatPattern + " " + pattern);
        } else {
            return
                    (dateFormatPattern == null || pattern == null) ?
                            DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, locale) :
                            new SimpleDateFormat(dateFormatPattern + " " + pattern, locale);
        }
    }

    /**
     * Returns the decimal format
     *
     * @param scale maximum number of digits after decimal point
     * @param fill  indicates if scale is the exact or the maximum
     *              number of digits after the decimal point
     * @return a decimal format
     */
    public DecimalFormat getNumberFormat(int scale, boolean fill) {
        StringBuffer buffer = getIntegerNumberPattern();
        if (scale > 0) {
            buffer.append(".");
            char fraction = fill ? '0' : '#';
            for (int i = 0; i < scale; i++) {
                buffer.append(fraction);
            }
        }
        return new DecimalFormat(buffer.toString(), getDecimalFormatSymbols());
    }

    /**
     * Returns the default decimal format
     *
     * @return a decimal format
     */
    public DecimalFormat getNumberFormat() {
        StringBuffer buffer = getIntegerNumberPattern();
        buffer.append(".###############");
        return new DecimalFormat(buffer.toString(), getDecimalFormatSymbols());
    }

    /**
     * Returns a decimal format
     *
     * @param pattern decimal format pattern
     * @return a decimal format
     */
    public DecimalFormat getNumberFormat(String pattern) {
        return new DecimalFormat(pattern, getDecimalFormatSymbols());
    }

    protected StringBuffer getIntegerNumberPattern() {
        StringBuffer buffer = new StringBuffer();
        if (digitGrouping == 0) {
            buffer.append("0");
        } else {
            buffer.append("#,");
            for (int i = 0; i < (digitGrouping - 1); i++) {
                buffer.append("#");
            }
            buffer.append("0");
        }
        return buffer;
    }

}
