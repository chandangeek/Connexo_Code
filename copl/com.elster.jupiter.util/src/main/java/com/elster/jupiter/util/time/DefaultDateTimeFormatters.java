package com.elster.jupiter.util.time;

import java.time.format.DateTimeFormatter;

/**
 * These formats are used when back end code needs to format date/time information in the absence of an end user, e.g. in logging messages.
 * As there is no user to determine her preferences, formats have been chosen that are widely acceptable. see http://confluence.eict.vpdc/display/JDG/Date+and+time+format
 */
public enum DefaultDateTimeFormatters {
    ;
    public static final String ZONE_FORMAT = "zzz";

    public static DateTimeFormatterBuilder longDate() {
        return new DateTimeFormatterBuilder(Date.LONG);
    }

    public static DateTimeFormatterBuilder mediumDate() {
        return new DateTimeFormatterBuilder(Date.MEDIUM);
    }

    public static DateTimeFormatterBuilder shortDate() {
        return new DateTimeFormatterBuilder(Date.SHORT);
    }

    public static TimeFormatterBuilder longTime() {
        return new TimeFormatterBuilder(Time.LONG);
    }

    public static TimeFormatterBuilder mediumTime() {
        return new TimeFormatterBuilder(Time.MEDIUM);
    }

    public static TimeFormatterBuilder shortTime() {
        return new TimeFormatterBuilder(Time.SHORT);
    }

    public static class DateTimeFormatterBuilder {
        private final StringBuilder builder = new StringBuilder();

        public DateTimeFormatterBuilder(String datePattern) {
            builder.append(datePattern);
        }

        public TimeFormatterBuilder withLongTime() {
            return new TimeFormatterBuilder(builder.append(' ').append(Time.LONG).append(' ').append(ZONE_FORMAT).toString());
        }

        public TimeFormatterBuilder withMediumTime() {
            return new TimeFormatterBuilder(builder.append(' ').append(Time.MEDIUM).append(' ').append(ZONE_FORMAT).toString());
        }

        public TimeFormatterBuilder withShortTime() {
            return new TimeFormatterBuilder(builder.append(' ').append(Time.SHORT).append(' ').append(ZONE_FORMAT).toString());
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
        String SHORT = "yyyy-MM-dd";
        String MEDIUM = "EEE, d MMM yyyy";
        String LONG = "EEEE, d MMMM yyyy";
    }

    private static interface Time {
        String SHORT = "HH:mm";
        String MEDIUM = "HH:mm";
        String LONG = "HH:mm:ss";
    }

}
