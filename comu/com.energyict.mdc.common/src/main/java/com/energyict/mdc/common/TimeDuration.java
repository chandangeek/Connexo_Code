/*
 * TimeDuration.java
 *
 * Created on 21 februari 2003, 12:05
 */

package com.energyict.mdc.common;

import org.joda.time.DateTimeConstants;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.Calendar;

/**
 * represents a relative period in time
 * e.g. two months , three weeks.
 * TimeDuration takes into account that the exact length
 * of a duration specified in years,months,weeks or days is not fixed.
 *
 * @author Karel
 */
@XmlJavaTypeAdapter(TimeDurationXmlMarshallAdapter.class)
public class TimeDuration implements Comparable, java.io.Serializable {

    private static final String MILLISECONDS_STRING = "milliseconds";
    private static final String SECONDS_STRING = "seconds";
    private static final String MINUTES_STRING = "minutes";
    private static final String HOURS_STRING = "hours";
    private static final String DAYS_STRING = "days";
    private static final String WEEKS_STRING = "weeks";
    private static final String MONTHS_STRING = "months";
    private static final String YEARS_STRING = "years";

    /**
     * milliseconds
     */
    public static final int MILLISECONDS = Calendar.MILLISECOND;
    /**
     * seconds
     */
    public static final int SECONDS = Calendar.SECOND;
    /**
     * minutes
     */
    public static final int MINUTES = Calendar.MINUTE;
    /**
     * hours
     */
    public static final int HOURS = Calendar.HOUR_OF_DAY;
    /**
     * days
     */
    public static final int DAYS = Calendar.DATE;
    /**
     * weeks
     */
    public static final int WEEKS = Calendar.WEEK_OF_YEAR;
    /**
     * months
     */
    public static final int MONTHS = Calendar.MONTH;
    /**
     * years
     */
    public static final int YEARS = Calendar.YEAR;
    private static final int DAYS_IN_WEEK = 7;
    private static final int SECONDS_IN_WEEK = DateTimeConstants.SECONDS_PER_HOUR * DateTimeConstants.HOURS_PER_DAY * DAYS_IN_WEEK;
    private static final int DAYS_IN_MONTH = 31;
    private static final int SECONDS_IN_MONTH = DateTimeConstants.SECONDS_PER_HOUR * DateTimeConstants.HOURS_PER_DAY * DAYS_IN_MONTH;
    private static final int DAYS_IN_YEAR = 365;
    private static final int SECONDS_IN_YEAR = DateTimeConstants.SECONDS_PER_HOUR * DateTimeConstants.HOURS_PER_DAY * DAYS_IN_YEAR;

    private int count;
    private int timeUnitCode;

    /**
     * Creates a new TimeDuration of zero seconds.
     */
    public TimeDuration() {
        this.count = 0;
        this.timeUnitCode = SECONDS;
    }

    /**
     * Creates a new TimeDuration.
     *
     * @param count        duration length, unit is determined by second argument.
     * @param timeUnitCode the code identifying the time unit for the new TimeDuration.
     */
    public TimeDuration(int count, int timeUnitCode) {
        this.count = count;
        this.timeUnitCode = timeUnitCode;
        //validate that the number of seconds doesn't cause an int overflow.
        if (causesIntOverflow(count, timeUnitCode)) {
            String invalidTimeDuration = UserEnvironment.getDefault().getErrorMsg("invalidTimeDuration");
            if (invalidTimeDuration.startsWith(MultiBundleTranslator.MISSING_RESOURCE_PREFIX)) {
                invalidTimeDuration = "Invalid time duration";
            }
            throw new IllegalArgumentException(invalidTimeDuration);
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
            return new TimeDuration(-count, this.timeUnitCode);
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
            this.timeUnitCode = SECONDS;
            return;
        }

        int[] testValues = {
                SECONDS_IN_YEAR,
                SECONDS_IN_MONTH,
                SECONDS_IN_WEEK,
                DateTimeConstants.SECONDS_PER_DAY,
                DateTimeConstants.SECONDS_PER_HOUR,
                DateTimeConstants.SECONDS_PER_MINUTE
        };
        int[] timeUnitCodes = {
                YEARS,
                MONTHS,
                WEEKS,
                DAYS,
                HOURS,
                MINUTES
        };
        for (int i = 0; i < testValues.length; i++) {
            if (seconds % testValues[i] == 0) {
                this.count = seconds / testValues[i];
                this.timeUnitCode = timeUnitCodes[i];
                return;
            }
        }
        this.count = seconds;
        this.timeUnitCode = SECONDS;
    }

    public TimeDuration(String stringValue) {
        String countAsString = stringValue.substring(0, stringValue.indexOf(" "));
        String timeUnitAsString = stringValue.substring(stringValue.indexOf(" ")+1);
        this.count = Integer.parseInt(countAsString);
        if (MILLISECONDS_STRING.equals(timeUnitAsString)) {
            this.timeUnitCode = MILLISECONDS;
        } else if (SECONDS_STRING.equals(timeUnitAsString)) {
            this.timeUnitCode = SECONDS;
        } else if (MINUTES_STRING.equals(timeUnitAsString)) {
            this.timeUnitCode = MINUTES;
        } else if (HOURS_STRING.equals(timeUnitAsString)) {
            this.timeUnitCode = HOURS;
        } else if (DAYS_STRING.equals(timeUnitAsString)) {
            this.timeUnitCode = DAYS;
        } else if (WEEKS_STRING.equals(timeUnitAsString)) {
            this.timeUnitCode = WEEKS;
        } else if (MONTHS_STRING.equals(timeUnitAsString)) {
            this.timeUnitCode = MONTHS;
        } else if (YEARS_STRING.equals(timeUnitAsString)) {
            this.timeUnitCode = YEARS;
        }
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
        switch (timeUnitCode) {
            case MILLISECONDS:
                return MILLISECONDS_STRING;
            case SECONDS:
                return SECONDS_STRING;
            case MINUTES:
                return MINUTES_STRING;
            case HOURS:
                return HOURS_STRING;
            case DAYS:
                return DAYS_STRING;
            case WEEKS:
                return WEEKS_STRING;
            case MONTHS:
                return MONTHS_STRING;
            case YEARS:
                return YEARS_STRING;
        }
        // should not happen
        return "";
    }

    /**
     * Returns a String representation of the receiver.
     *
     * @return a string representation
     */
    public String toString() {
        String tuDescription = getTimeUnitDescription(getTimeUnitCode());
        if (!tuDescription.isEmpty()) {
            return getCount() + " " + tuDescription;
        } else {
            return "";
        }
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
        if (forceCalendarArithmetic && calendarField == TimeDuration.HOURS) {
            calendar.set(TimeDuration.HOURS, calendar.get(TimeDuration.HOURS) + getCount());
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
            if (timeUnitCode == MILLISECONDS || timeUnitCode == SECONDS ||
                    timeUnitCode == MINUTES || timeUnitCode == HOURS) {
                long millis = calendar.getTimeInMillis();
                long intervalInMillis = this.getSeconds() * DateTimeConstants.MILLIS_PER_SECOND;
                millis = (millis / intervalInMillis) * intervalInMillis;
                calendar.setTimeInMillis(millis);
            } else {
                truncate(calendar, getTimeUnitCode());
            }
        }
    }

    private void truncate(Calendar calendar, int timeUnitCode) {
        // clear lower fields
        switch (timeUnitCode) {
            case YEARS:
                calendar.set(Calendar.MONTH, 0);
                // intentionally no break coded
            case MONTHS:
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                // intentionally no break coded
            case WEEKS:
                if (timeUnitCode == WEEKS) {
                    calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
                }
                // intentionally no break coded
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
        switch (timeUnitCode) {
            case YEARS:
                return count * SECONDS_IN_YEAR;
            case MONTHS:
                return count * SECONDS_IN_MONTH;
            case WEEKS:
                return count * SECONDS_IN_WEEK;
            case DAYS:
                return count * DateTimeConstants.SECONDS_PER_DAY;
            case HOURS:
                return count * DateTimeConstants.SECONDS_PER_HOUR;
            case MINUTES:
                return count * DateTimeConstants.SECONDS_PER_MINUTE;
            case SECONDS:
                return count;
            case MILLISECONDS:
                return count / DateTimeConstants.MILLIS_PER_SECOND;
        }
        return 0;
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
        switch (timeUnitCode) {
            case MILLISECONDS:
                return count;
            default:
                return (long)getSeconds() * DateTimeConstants.MILLIS_PER_SECOND;
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

    public int compareTo(Object o) {
        int thisSeconds = this.getSeconds();
        int otherSeconds = ((TimeDuration) o).getSeconds();
        if (thisSeconds == otherSeconds) {
            return 0;
        } else {
            return thisSeconds > otherSeconds ? 1 : -1;
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

    /**
     * Returns the receiver's hashCode.
     *
     * @return the hash code
     */
    public int hashCode() {
        return getSeconds();
    }

    /**
     * This method should only be used by the XML deserialisation process
     * For all practical reasons , TimeDuration objects should be immutable
     *
     * @param value new count value
     * @deprecated should be immutable
     */
    public void setCount(int value) {
        this.count = value;
    }

    /**
     * This method should only be used by the XML deserialisation process
     * For all practical reasons , TimeDuration objects should be immutable
     *
     * @param value new unit code
     * @deprecated should be immutable
     */
    public void setTimeUnitCode(int value) {
        this.timeUnitCode = value;
    }

    private boolean causesIntOverflow(int count, int timeUnitCode) {
        try {
            switch (timeUnitCode) {
                case YEARS:
                    MathUtils.safeMultiply(new int[]{count, SECONDS_IN_YEAR});
                    break;
                case MONTHS:
                    MathUtils.safeMultiply(new int[]{count, SECONDS_IN_MONTH});
                    break;
                case WEEKS:
                    MathUtils.safeMultiply(new int[]{count, SECONDS_IN_WEEK});
                    break;
                case DAYS:
                    MathUtils.safeMultiply(new int[]{count, DateTimeConstants.SECONDS_PER_DAY});
                    break;
                case HOURS:
                    MathUtils.safeMultiply(new int[]{count, DateTimeConstants.SECONDS_PER_HOUR});
                    break;
                case MINUTES:
                    MathUtils.safeMultiply(new int[]{count, DateTimeConstants.SECONDS_PER_MINUTE});
            }
            return false;
        } catch (ArithmeticException e) {
            return true;
        }
    }

    // Expects a string in the form <count>-<unit code>
    // Eg. "5-12" being 5 minutes and "4-3" being 4 weeks

    private static String VALUE_UNIT_SEPARATOR = "-";

    public static TimeDuration fromSystemParameterString(String timeDurationSystemParameterString) {
        if (timeDurationSystemParameterString.indexOf(VALUE_UNIT_SEPARATOR)==-1) {
            // Previously the number of seconds were stored, so
            return new TimeDuration(Integer.valueOf(timeDurationSystemParameterString), TimeDuration.SECONDS);
        }
        String count = timeDurationSystemParameterString.substring(0, timeDurationSystemParameterString.indexOf(VALUE_UNIT_SEPARATOR));
        String unitCode = timeDurationSystemParameterString.substring(timeDurationSystemParameterString.indexOf(VALUE_UNIT_SEPARATOR)+1);
        return new TimeDuration(Integer.valueOf(count), Integer.valueOf(unitCode));
    }

    public static String asSystemParameterString(TimeDuration duration) {
        return duration.getCount() + VALUE_UNIT_SEPARATOR + duration.getTimeUnitCode();
    }
}
