/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.time;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * These formats are used when back end code needs to format date/time information in the absence of an end user, e.g. in logging messages.
 * As there is no user to determine her preferences, formats have been chosen that are widely acceptable. see http://confluence.eict.vpdc/display/JDG/Date+and+time+format
 */
public enum DefaultDateTimeFormatters {
    ;
    public static final String ZONE_FORMAT = "zzz";

    public static DateTimeFormatterBuilder longDate() {
        return longDate(Locale.getDefault());
    }

    public static DateTimeFormatterBuilder mediumDate() {
        return mediumDate(Locale.getDefault());
    }

    public static DateTimeFormatterBuilder shortDate() {
        return shortDate(Locale.getDefault());
    }

    public static DateTimeFormatterBuilder longDate(Locale locale) {
        if (Locale.US.equals(locale)) {
            return new DateTimeFormatterBuilder(Date.LONG_US, locale);
        }
        return new DateTimeFormatterBuilder(Date.LONG, locale);
    }

    public static DateTimeFormatterBuilder mediumDate(Locale locale) {
        return new DateTimeFormatterBuilder(Date.MEDIUM, locale);
    }

    public static DateTimeFormatterBuilder shortDate(Locale locale) {
        if (Locale.US.equals(locale)) {
            return new DateTimeFormatterBuilder(Date.SHORT_US, locale);
        }
        return new DateTimeFormatterBuilder(Date.SHORT, locale);
    }

    public static TimeFormatterBuilder longTime() {
        return longTime(Locale.getDefault());
    }

    public static TimeFormatterBuilder mediumTime() {
        return mediumTime(Locale.getDefault());
    }

    public static TimeFormatterBuilder shortTime() {
        return shortTime(Locale.getDefault());
    }

    public static TimeFormatterBuilder longTime(Locale locale) {
        if (Locale.US.equals(locale)) {
            return new TimeFormatterBuilder(Time.LONG_US);
        }
        return new TimeFormatterBuilder(Time.LONG);
    }

    public static TimeFormatterBuilder mediumTime(Locale locale) {
        return new TimeFormatterBuilder(Time.MEDIUM);
    }

    public static TimeFormatterBuilder shortTime(Locale locale) {
        if (Locale.US.equals(locale)) {
            return new TimeFormatterBuilder(Time.SHORT_US);
        }
        return new TimeFormatterBuilder(Time.SHORT);
    }

    public static class DateTimeFormatterBuilder {
        private final Locale locale;

        private final StringBuilder builder = new StringBuilder();

        public DateTimeFormatterBuilder(String datePattern) {
            this(datePattern, Locale.getDefault());
        }

        public DateTimeFormatterBuilder(String datePattern, Locale locale) {
            this.locale = locale;
            builder.append(datePattern);
        }

        public TimeFormatterBuilder withLongTime() {
            if (Locale.US.equals(locale)) {
                return new TimeFormatterBuilder(builder.append(' ').append(Time.LONG_US).toString());
            }
            return new TimeFormatterBuilder(builder.append(' ').append(Time.LONG).toString());
        }

        public TimeFormatterBuilder withMediumTime() {
            return new TimeFormatterBuilder(builder.append(' ').append(Time.MEDIUM).append(' ').append(ZONE_FORMAT).toString());
        }

        public TimeFormatterBuilder withShortTime() {
            if (Locale.US.equals(locale)) {
                return new TimeFormatterBuilder(builder.append(' ').append(Time.SHORT_US).toString());
            }
            return new TimeFormatterBuilder(builder.append(' ').append(Time.SHORT).toString());
        }

        public DateTimeFormatter build() {
            return DateTimeFormatter.ofPattern(builder.toString());
        }
    }

    public static class TimeFormatterBuilder {
        private final String pattern;

        public TimeFormatterBuilder(String timePattern) {
            this.pattern = timePattern;
        }

        public DateTimeFormatter build() {
            return DateTimeFormatter.ofPattern(pattern);
        }
    }

    private static interface Date {
        String SHORT = "dd MMM ''yy";
        String MEDIUM = "EEE, d MMM yyyy";
        String LONG = "EEE dd MMM ''yy";
        String SHORT_US = "MMM-dd-''yy";
        String MEDIUM_US = "EEE, d MMM yyyy";
        String LONG_US = "EEE, MMM-dd-''yy";
    }

    private static interface Time {
        String SHORT = "HH:mm";
        String MEDIUM = "HH:mm";
        String LONG = "HH:mm:ss";
        String SHORT_US = "hh:mm a";
        String MEDIUM_US = "hh:mm a";
        String LONG_US = "hh:mm:ss a";
    }

}
