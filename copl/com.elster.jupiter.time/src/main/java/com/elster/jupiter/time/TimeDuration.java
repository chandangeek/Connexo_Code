/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.time;

import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.time.impl.MessageSeeds;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Period;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.IsoFields;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalUnit;
import java.util.Arrays;
import java.util.Calendar;

import static com.elster.jupiter.time.TimeDuration.TimeUnit.DAYS;
import static com.elster.jupiter.time.TimeDuration.TimeUnit.HOURS;
import static com.elster.jupiter.time.TimeDuration.TimeUnit.MILLISECONDS;
import static com.elster.jupiter.time.TimeDuration.TimeUnit.MINUTES;
import static com.elster.jupiter.time.TimeDuration.TimeUnit.MONTHS;
import static com.elster.jupiter.time.TimeDuration.TimeUnit.SECONDS;
import static com.elster.jupiter.time.TimeDuration.TimeUnit.WEEKS;
import static com.elster.jupiter.time.TimeDuration.TimeUnit.YEARS;

/**
 * represents a relative period in time
 * e.g. two months , three weeks.
 * TimeDuration takes into account that the exact length
 * of a duration specified in years,months,weeks or days is not fixed.
 *
 * @author Karel
 */
@XmlJavaTypeAdapter(TimeDurationXmlMarshallAdapter.class)
public class TimeDuration implements Comparable<TimeDuration>, Serializable {

    public static final TimeDuration NONE = TimeDuration.seconds(0);

    private static final String MILLISECONDS_STRING = "milliseconds";
    private static final String SECONDS_STRING = "seconds";
    private static final String MINUTES_STRING = "minutes";
    private static final String HOURS_STRING = "hours";
    private static final String DAYS_STRING = "days";
    private static final String WEEKS_STRING = "weeks";
    private static final String MONTHS_STRING = "months";
    private static final String YEARS_STRING = "years";

    private static final int SECONDS_PER_MINUTE = 60;
    private static final int SECONDS_PER_HOUR = 3600;
    private static final int HOURS_PER_DAY = 24;
    private static final int SECONDS_PER_DAY = 86400;
    private static final int SECONDS_PER_WEEK = SECONDS_PER_DAY * 7;
    private static final int DAYS_IN_MONTH = 31;
    private static final int SECONDS_IN_MONTH = SECONDS_PER_DAY * DAYS_IN_MONTH;
    private static final int DAYS_IN_YEAR = 365;
    private static final int SECONDS_IN_YEAR = SECONDS_PER_HOUR * HOURS_PER_DAY * DAYS_IN_YEAR;
    private static final int MILLIS_PER_SECOND = 1000;


    private int count;
    private TimeUnit timeUnit;
    private int timeUnitCode;

    /**
     * Creates a new TimeDuration of zero seconds.
     */
    private TimeDuration() {
        count = 0;
        timeUnitCode = -1;
    }

    /**
     * Creates a new TimeDuration.
     *
     * @param count        duration length, unit is determined by second argument.
     * @param timeUnitCode the code identifying the time unit for the new TimeDuration.
     */
    public TimeDuration(int count, int timeUnitCode) {
        this(count, TimeUnit.forCode(timeUnitCode));
    }

    public TimeDuration(int count, TimeUnit timeUnit) {
        this.count = count;
        this.timeUnit = timeUnit;
        this.timeUnitCode = timeUnit.getCode();
        //validate that the number of seconds doesn't cause an int overflow.
        if (causesIntOverflow(count, this.timeUnit)) {
            throw new IllegalArgumentException("Invalid time duration");
        }
    }

    public TemporalUnit getTemporalUnit() {
        return getTimeUnit().getTemporalUnit();
    }

    public TemporalField getTemporalField() {
        return getTimeUnit().getTemporalField();
    }

    public TemporalAmount asTemporalAmount() {
        switch(this.timeUnit) {
            case MILLISECONDS:
            case SECONDS:
            case MINUTES:
            case HOURS:
                return Duration.of(this.count, this.getTemporalUnit());
            case YEARS:
                return Period.ofYears(this.count);
            case MONTHS:
                return Period.ofMonths(this.count);
            case WEEKS:
                return Period.ofWeeks(this.count);
            case DAYS:
                return Period.ofDays(this.count);
            default: throw new IllegalArgumentException("Unsupported time unit");
        }
    }

    public enum TimeUnit {
        MILLISECONDS(Calendar.MILLISECOND, 0, MILLISECONDS_STRING, Integer.MAX_VALUE, ChronoUnit.MILLIS, ChronoField.MILLI_OF_SECOND),
        SECONDS(Calendar.SECOND, 1, SECONDS_STRING, Integer.MAX_VALUE, ChronoUnit.SECONDS, ChronoField.SECOND_OF_MINUTE),
        MINUTES(Calendar.MINUTE, SECONDS_PER_MINUTE, MINUTES_STRING, 35791394, ChronoUnit.MINUTES, ChronoField.MINUTE_OF_HOUR),
        HOURS(Calendar.HOUR_OF_DAY, SECONDS_PER_HOUR, HOURS_STRING, 596523, ChronoUnit.HOURS, ChronoField.HOUR_OF_DAY),
        DAYS(Calendar.DAY_OF_MONTH, SECONDS_PER_DAY, DAYS_STRING, 24855, ChronoUnit.DAYS, ChronoField.DAY_OF_MONTH),
        WEEKS(Calendar.WEEK_OF_YEAR, SECONDS_PER_WEEK, WEEKS_STRING, 3550, ChronoUnit.WEEKS, IsoFields.WEEK_OF_WEEK_BASED_YEAR) {
            public ZonedDateTime truncate(ZonedDateTime time) {
                ZonedDateTime result = time;
                for (TimeUnit timeUnit : values()) {
                    if (timeUnit.inSeconds < DAYS.inSeconds) {
                        result = result.with(timeUnit.temporalField, timeUnit.temporalField.range().getMinimum());
                    }
                }
                return result.with(ChronoField.DAY_OF_WEEK, DayOfWeek.MONDAY.getValue());
            }
        },
        MONTHS(Calendar.MONTH, SECONDS_IN_MONTH, MONTHS_STRING, 801, ChronoUnit.MONTHS, ChronoField.MONTH_OF_YEAR),
        YEARS(Calendar.YEAR, SECONDS_IN_YEAR, YEARS_STRING, 68, ChronoUnit.YEARS, ChronoField.YEAR);

        private final int code;
        private final int inSeconds;
        private final String description;
        private final int maxCount; // maximum count that does not cause int overflow when multiplied by the seconds pf the TimeUnit
        private final TemporalUnit temporalUnit;
        private final TemporalField temporalField;

        TimeUnit(int code, int inSeconds, String description, int maxCount, TemporalUnit temporalUnit, TemporalField temporalField) {
            this.code = code;
            this.inSeconds = inSeconds;
            this.description = description;
            this.maxCount = maxCount;
            this.temporalUnit = temporalUnit;
            this.temporalField = temporalField;
        }

        public TimeDuration during(int count) {
            return new TimeDuration(count, this);
        }

        public static TimeUnit forCode(int timeUnitCode) {
            return Arrays.stream(TimeUnit.values())
                    .filter(t -> t.code == timeUnitCode)
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException(String.valueOf(timeUnitCode) + " is not a supported time unit code"));
        }

        public static TimeUnit forDescription(String description) {
            return Arrays.stream(TimeUnit.values())
                    .filter(t -> t.description.equals(description))
                    .findFirst()
                    .orElseThrow(IllegalArgumentException::new);
        }

        public String getDescription() {
            return description;
        }

        public int getCode() {
            return code;
        }

        public TemporalUnit getTemporalUnit() {
            return temporalUnit;
        }

        public TemporalField getTemporalField() {
            return temporalField;
        }

        public ZonedDateTime truncate(ZonedDateTime time) {
            ZonedDateTime result = time;
            for (TimeUnit timeUnit : values()) {
                if (timeUnit.inSeconds <= this.inSeconds && timeUnit != WEEKS) {
                    result = result.with(timeUnit.temporalField, timeUnit.temporalField.range().getMinimum());
                }
            }
            return result;
        }
    }

    public static TimeDuration months(int count) {
        return new TimeDuration(count, MONTHS);
    }

    public static TimeDuration weeks(int count) {
        return new TimeDuration(count, WEEKS);
    }

    public static TimeDuration days(int count) {
        return new TimeDuration(count, DAYS);
    }

    public static TimeDuration hours(int count) {
        return new TimeDuration(count, HOURS);
    }

    public static TimeDuration minutes(int count) {
        return new TimeDuration(count, MINUTES);
    }

    public static TimeDuration seconds(int count) {
        return new TimeDuration(count, SECONDS);
    }

    public static TimeDuration millis(int count) {
        return new TimeDuration(count, MILLISECONDS);
    }

    /**
     * Returns the absolute value of this TimeDuration.
     * If this TimeDuration is non negative, this TimeDuration is returned.
     * If this TimeDuration is negative, the negation is returned (with the same unit).
     *
     * @return The absolute value of this TimeDuration
     */
    public TimeDuration abs () {
        if (this.count >= 0) {
            return this;
        }
        else {
            return new TimeDuration(-count, this.getTimeUnit());
        }
    }

    /**
     * Creates a new TimeDuration.
     * if seconds can be divided by 3600 * 24 * 365,
     * the arguments is divided by 3600 * 24 * 365 and interpreted as years.
     * if seconds can be divided by 3600 * 24 * 31,
     * the arguments is divided by 3600 * 24 * 31 and interpreted as months.
     * Otherwise it is interpreted as seconds.
     *
     * @param seconds the length of the new TimeDuration in seconds.
     */
    public TimeDuration(int seconds) {
        if (seconds == 0) {
            this.timeUnit = SECONDS;
            this.timeUnitCode = this.timeUnit.code;
            return;
        }
        timeUnit = Arrays.stream(TimeUnit.values())
                .filter(t -> t.inSeconds != 0)
                .filter(t -> seconds % t.inSeconds == 0)
                .findFirst()
                .orElse(SECONDS);
        count = seconds / timeUnit.inSeconds;
        timeUnitCode = timeUnit.getCode();
    }

    public TimeDuration(String stringValue) {
        String countAsString = stringValue.substring(0, stringValue.indexOf(" "));
        String timeUnitAsString = stringValue.substring(stringValue.indexOf(" ")+1);
        try {
            this.count = Integer.parseInt(countAsString);
        } catch (NumberFormatException ex) {
            throw new LocalizedFieldValidationException(MessageSeeds.INVALID_TIME_COUNT, "timeCount", countAsString);
        }
        switch (timeUnitAsString) {
            case MILLISECONDS_STRING: {
                this.timeUnit = MILLISECONDS;
                break;
            }
            case SECONDS_STRING: {
                this.timeUnit = SECONDS;
                break;
            }
            case MINUTES_STRING: {
                this.timeUnit = MINUTES;
                break;
            }
            case HOURS_STRING: {
                this.timeUnit = HOURS;
                break;
            }
            case DAYS_STRING: {
                this.timeUnit = DAYS;
                break;
            }
            case WEEKS_STRING: {
                this.timeUnit = WEEKS;
                break;
            }
            case MONTHS_STRING: {
                this.timeUnit = MONTHS;
                break;
            }
            case YEARS_STRING: {
                this.timeUnit = YEARS;
                break;
            }
            default: throw new LocalizedFieldValidationException(MessageSeeds.UNKNOWN_TIME_UNIT, "timeUnit", timeUnitAsString);
        }
        timeUnitCode = timeUnit.getCode();
    }

    /**
     * Returns the receiver's count field.
     *
     * @return the TimeDuration length.
     */
    public int getCount() {
        return count;
    }

    /**
     * Returns the receiver's time unit code.
     *
     * @return the receiver's time unit code
     */
    public int getTimeUnitCode() {
        return timeUnitCode;
    }

    public TimeUnit getTimeUnit() {
        if (timeUnit == null) {
            timeUnit = TimeUnit.forCode(timeUnitCode);
        }
        return timeUnit;
    }

    /**
     * Tests if the receiver length is zero.
     *
     * @return true if the receiver's length is zero
     */
    public boolean isEmpty() {
        return count == 0;
    }

    private int getCalendarField() {
        return timeUnitCode;
    }

    /**
     * Returns a string representing the time unit identified by the argument.
     *
     * @param timeUnitCode the time unit code.
     * @return a string representation.
     */
    public static String getTimeUnitDescription(int timeUnitCode) {
        return TimeUnit.forCode(timeUnitCode).description;
    }

    public static boolean isValidTimeUnitDescription(String timeUnit) {
        return Arrays.stream(TimeUnit.values())
                .anyMatch(t -> t.description.equals(timeUnit));
    }

    /**
     * Returns a String representation of the receiver.
     *
     * @return a string representation
     */
    public String toString() {
        return getCount() + " " + getTimeUnit().description;
    }

    /**
     * Adds the TimeDuration to the argument.
     *
     * @param calendar calendar to add TimeDuration to
     */
    public void addTo(Calendar calendar) {
        addTo(calendar, false);
    }

    public void addTo(Calendar calendar, boolean forceCalendarArithmetic) {
        // Assert : calendar must be lenient
        int calendarField = getCalendarField();
        if (forceCalendarArithmetic && calendarField == HOURS.code) {
            calendar.set(HOURS.code, calendar.get(HOURS.code) + getCount());
        } else {
            calendar.add(calendarField, getCount());
        }
    }

    /**
     * Substracts the TimeDuration from the argument.
     *
     * @param calendar calendar to substract TimeDuration from
     */
    public void substractFrom(Calendar calendar) {
        // Assert : calendar must be lenient
        int calendarField = getCalendarField();
        calendar.add(calendarField, -getCount());
    }

    /**
     * Truncates the argument to the receiver.
     * That is adjust the calendar argument so that it
     * represents the latest date that is an exact multiple
     * of the receiver and is no later than the original argument.
     *
     * @param calendar calendar to truncate
     */
    public void truncate(Calendar calendar) {
        if (!isEmpty()) {
            // clear fields smaller than time unit
            if (getTimeUnit() == MILLISECONDS || getTimeUnit() == SECONDS ||
                    getTimeUnit() == MINUTES || getTimeUnit() == HOURS) {
                long millis = calendar.getTimeInMillis();
                long intervalInMillis = this.getSeconds() * MILLIS_PER_SECOND;
                millis = (millis / intervalInMillis) * intervalInMillis;
                calendar.setTimeInMillis(millis);
            } else {
                truncate(calendar, getTimeUnit());
            }
        }
    }

    private void truncate(Calendar calendar, TimeUnit timeUnit) {
        // clear lower fields
        switch (timeUnit) {
            case YEARS:
                calendar.set(Calendar.MONTH, 0);
                // intentionally no break coded
            case MONTHS:
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                // intentionally no break coded
            case WEEKS:
                if (WEEKS.equals(timeUnit)) {
                    calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
                    // intentionally no break coded
                }
            case DAYS:
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                // intentionally no break coded
            case HOURS:
                calendar.set(Calendar.MINUTE, 0);
                // intentionally no break coded
            case MINUTES:
                calendar.set(Calendar.SECOND, 0);
                // intentionally no break coded
            case SECONDS:
                calendar.set(Calendar.MILLISECOND, 0);
        }
    }

    /**
     * Returns the number of seconds this TimeDuration represents.
     * If the time unit is <CODE>MONTHS</CODE> or <CODE>YEARS</CODE>,
     * the TimeDuration length is variable and is approximated using
     * 31 days / month and 365 days / year
     *
     * @return the number of seconds in the TimeDuration
     */
    public int getSeconds() {
        switch (getTimeUnit()) {
            case MILLISECONDS:
                return count / MILLIS_PER_SECOND;
            default:
                return getTimeUnit().inSeconds * count;
        }
    }

    /**
     * Returns the number of milliseconds this TimeDuration represents.
     * If the time unit is <CODE>MONTHS</CODE> or <CODE>YEARS</CODE>,
     * the TimeDuration length is variable and is approximated using
     * 31 days / month and 365 days / year
     *
     * @return the number of milliseconds in the TimeDuration
     */
    public long getMilliSeconds() {
        switch (getTimeUnit()) {
            case MILLISECONDS:
                return count;
            default:
                return (long)getSeconds() * MILLIS_PER_SECOND;
        }
    }

    /**
     * Compares this object with the specified object for order.  Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.<p>
     * <p/>
     * In the foregoing description, the notation
     * <tt>sgn(</tt><i>expression</i><tt>)</tt> designates the mathematical
     * <i>signum</i> function, which is defined to return one of <tt>-1</tt>,
     * <tt>0</tt>, or <tt>1</tt> according to whether the value of <i>expression</i>
     * is negative, zero or positive.
     * <p/>
     * The implementor must ensure <tt>sgn(x.compareTo(y)) ==
     * -sgn(y.compareTo(x))</tt> for all <tt>x</tt> and <tt>y</tt>.  (This
     * implies that <tt>x.compareTo(y)</tt> must throw an exception iff
     * <tt>y.compareTo(x)</tt> throws an exception.)<p>
     * <p/>
     * The implementor must also ensure that the relation is transitive:
     * <tt>(x.compareTo(y)&gt;0 &amp;&amp; y.compareTo(z)&gt;0)</tt> implies
     * <tt>x.compareTo(z)&gt;0</tt>.<p>
     * <p/>
     * Finally, the implementer must ensure that <tt>x.compareTo(y)==0</tt>
     * implies that <tt>sgn(x.compareTo(z)) == sgn(y.compareTo(z))</tt>, for
     * all <tt>z</tt>.<p>
     * <p/>
     * It is strongly recommended, but <i>not</i> strictly required that
     * <tt>(x.compareTo(y)==0) == (x.equals(y))</tt>.  Generally speaking, any
     * class that implements the <tt>Comparable</tt> interface and violates
     * this condition should clearly indicate this fact.  The recommended
     * language is "Note: this class has a natural ordering that is
     * inconsistent with equals."
     *
     * @param o the Object to be compared.
     * @return a negative integer, zero, or a positive integer as this object
     *         is less than, equal to, or greater than the specified object.
     */

    public int compareTo(TimeDuration o) {
        int thisSeconds = this.getSeconds();
        int otherSeconds = o.getSeconds();
        if (thisSeconds == otherSeconds) {
            return 0;
        } else {
            if (thisSeconds > otherSeconds) {
                return 1;
            }
            else {
                return -1;
            }
        }
    }

    /**
     * Tests if the receiver is equal to the argument.
     *
     * @param o object to test equality.
     * @return true if the receiver and the argument are equal, false otherwise.
     */
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (o instanceof TimeDuration) {
            TimeDuration other = (TimeDuration) o;
            return getSeconds() == other.getSeconds();
        }
        else {
            return false;
        }
    }

    public static boolean overflow(int a, int b) {
        try {
            int p = Math.multiplyExact(a, b);
            return false;
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Returns the receiver's hashCode.
     *
     * @return the hash code
     */
    public int hashCode() {
        return getSeconds();
    }

    private boolean causesIntOverflow(int count, TimeUnit timeUnit) {
        return count > getTimeUnit().maxCount;
    }

    // Expects a string in the form <count>-<unit code>
    // Eg. "5-12" being 5 minutes and "4-3" being 4 weeks

    private static String VALUE_UNIT_SEPARATOR = "-";

}
