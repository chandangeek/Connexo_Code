package com.energyict.mdc.engine.offline.model;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * TimePeriod represents a fixed period in Time.
 * A TimePeriod is an half open (open,closed) interval,
 * in other words the from date is excluded,
 * the to date is included
 *
 * @author Karel
 */

// immutable

//@XmlJavaTypeAdapter(TimePeriodXmlMarshallAdapter.class)
public class TimePeriod implements Serializable {

    private final Date from;
    private final Date to;

    /**
     * Creates a new instance of TimePeriod.
     *
     * @param from start date
     * @param to   end date
     */
    public TimePeriod(Date from, Date to) {
        this.from = from;
        this.to = to;
    }

    /**
     * get the from field
     *
     * @return the timePeriod's start date
     */
    public Date getFrom() {
        return from;
    }

    /**
     * get the to field
     *
     * @return the TimePeriod's end date
     */
    public Date getTo() {
        return to;
    }

    /**
     * return the TimePeriod's length in milliseconds
     *
     * @return length in milliseconds
     */
    public long getLength() {
        if (to == null || from == null) {
            return Long.MAX_VALUE;
        } else {
            return to.getTime() - from.getTime();
        }
    }

    /**
     * test if the arguments falls within the receiver's boundaries
     *
     * @param test test Date
     * @return if the receiver equals or is later than the receiver's from date,
     *         and earlier than the receiver's to date
     */
    public boolean includes(Date test) {
        if (from != null && from.after(test)) {
            return false;
        }
        if (to != null && !to.after(test)) {
            return false;
        }
        return true;
    }

    /**
     * Tests whether this period contains the end of interval
     *
     * @param intervalEnd end of interval
     * @return true if included , false otherwise
     */
    public boolean includesIntervalEnd(Date intervalEnd) {
        if ((from != null) && ((intervalEnd.before(from) || intervalEnd.equals(from)))) {
            return false;
        }
        if ((to != null) && (intervalEnd.after(to))) {
            return false;
        }
        return true;
    }

    /**
     * Tests if the receiver represent an empty time period.
     *
     * @return true if the receiver is empty, false otherwise.
     */
    public boolean isEmpty() {
        return getLength() <= 0;
    }

    /**
     * test if the arguments equals the receiver's period
     *
     * @param o Object
     * @return if the receiver from/to dates equal the arguments from/to dates,
     */
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        try {
            TimePeriod other = (TimePeriod) o;
            return equals(other.getFrom(), other.getTo());
        } catch (ClassCastException ex) {
            return false;
        }
    }

    /**
     * test whether the receiver represents the same period as the arguments.
     * Equivalent to <CODE>equals(new TimePeriod(start,end))</CODE>
     *
     * @param start start date of the test period.
     * @param end   end date of the test period.
     * @return true if the receiver and the test period are equals,
     *         false otherwise.
     */
    public boolean equals(Date start, Date end) {
        return Equality.equalityHoldsFor(from).and(start) && Equality.equalityHoldsFor(to).and(end);
    }

    /**
     * Returns the receiver's hashCode.
     *
     * @return the hash code.
     */
    public int hashCode() {
        return
                (from == null ? 0 : from.hashCode()) ^
                        (to == null ? 0 : to.hashCode());
    }

    /**
     * Tests if the receiver overlaps with the argument.
     *
     * @param period test period.
     * @return true if the receiver and the test period overlap,
     *         false otherwise.
     */
    public boolean overlaps(TimePeriod period) {
        return overlaps(period.getFrom(), period.getTo());
    }

    /**
     * Tests if the receiver overlaps the test period.
     * Equivalent to <CODE>overlaps(new TimeDuration(start,end))</CODE>
     *
     * @param start start date of the test period.
     * @param end   end date of the test period.
     * @return true if the receiver and the test period overlap,
     *         false otherwise.
     */
    public boolean overlaps(Date start, Date end) {
        if ((from == null && to == null) || (start == null && end == null)) {
            return true;
        }
        if (Equality.equalityHoldsFor(from).and(to) || Equality.equalityHoldsFor(start).and(end)) {
            return false;
        }
        return compareTo(start, end) == 0;
    }

    /**
     * Compares the receiver to the argument.
     * Returns <UL>
     * <LI> -2 if the receiver is before the argument and not adjacent </LI>
     * <LI> -1 if the receiver is before the argument and adjacent </LI>
     * <LI> 0 if the receiver overlaps with the argument</LI>
     * <LI> 1 if the receiver is after the argument and adjacent </LI>
     * <LI> 2 if the receiver is after the argument and not adjacent </LI>
     * </UL>
     *
     * @param period test period.
     * @return -2,-1,0,1 or 2.
     */

    public int compareTo(TimePeriod period) {
        return compareTo(period.getFrom(), period.getTo());
    }

    /**
     * Compares the receiver to the test period represented by the arguments.
     * Equivalent to <CODE>compareTo(new TimeDuration(start,end))</CODE>
     *
     * @param start start date of the test period.
     * @param end   end date of the test period.
     * @return -2,-1,0,1 or 2.
     */

    public int compareTo(Date start, Date end) {
        if (equals(start, end)) {
            return 0;
        }
        if ((start == null) && (from == null)) {
            return 0;
        }
        if ((end == null) && (to == null)) {
            return 0;
        }
        if (start == null || (from != null && start.before(from))) {
            if (end == null) {
                return 0;
            } else {
                int compareResult = end.compareTo(from);
                if (compareResult < 0) {
                    return 2;
                }
                if (compareResult == 0) {
                    return 1;
                }
                return 0;
            }
        } else {
            if (to == null) {
                return 0;
            } else {
                int compareResult = start.compareTo(to);
                if (compareResult < 0) {
                    return 0;
                }
                if (compareResult == 0) {
                    return -1;
                }
                return -2;
            }
        }
    }


    /**
     * Tests if the receiver starts before the argument
     *
     * @param testDate test date.
     * @return true if the receiver starts before the test date,
     *         false otherwise.
     */

    public boolean startsBefore(Date testDate) {
        if (from == null) {
            return testDate != null;
        }
        return testDate == null ? false : from.before(testDate);
    }

    /**
     * Tests if the receiver starts after the argument
     *
     * @param testDate test date.
     * @return true if the receiver starts after test date,
     *         false otherwise.
     */

    public boolean startsAfter(Date testDate) {
        if (from == null) {
            return false;
        }
        return testDate == null ? true : from.after(testDate);
    }

    /**
     * Tests if the receiver ends before the argument
     *
     * @param testDate test date.
     * @return true if the receiver ends before test date,
     *         false otherwise.
     */

    public boolean endsBefore(Date testDate) {
        if (to == null) {
            return false;
        }
        return testDate == null ? true : to.before(testDate);
    }

    /**
     * Tests if the receiver ends after the argument
     *
     * @param testDate test date.
     * @return true if the receiver ends after test date,
     *         false otherwise.
     */

    public boolean endsAfter(Date testDate) {
        if (to == null) {
            return testDate != null;
        }
        return testDate == null ? false : to.after(testDate);
    }

    /**
     * Tests if the receiver is included in the argument
     *
     * @param period test period.
     * @return true if the receiver is included in the test period,
     *         false otherwise.
     */

    public boolean isIncludedIn(TimePeriod period) {
        return isIncludedIn(period.getFrom(), period.getTo());
    }

    /**
     * Tests if the receiver is included in the test period defined by the arguments.
     * equivalent to <CODE>isIncludedIn(new TimePeriod(start,end))</CODE>
     *
     * @param start start date of the test period.
     * @param end   end date of the test period.
     * @return true if the receiver is included in the test period,
     *         false otherwise.
     */

    public boolean isIncludedIn(Date start, Date end) {
        return !(startsBefore(start) || endsAfter(end));
    }

    /**
     * Tests if the receiver includes the argument
     *
     * @param period test period.
     * @return true if the receiver includes the test period,
     *         false otherwise.
     */

    public boolean includes(TimePeriod period) {
        return includes(period.getFrom(), period.getTo());
    }

    /**
     * Tests if the receiver includes in the test period defined by the arguments.
     * equivalent to <CODE>includes(new TimePeriod(start,end))</CODE>
     *
     * @param start start date of the test period.
     * @param end   end date of the test period.
     * @return true if the receiver includes the test period,
     *         false otherwise.
     */

    public boolean includes(Date start, Date end) {
        return !(startsAfter(start) || endsBefore(end));
    }

    /**
     * Returns the intersection of the receiver and the argument
     *
     * @param other period to intersect with.
     * @return the intersection.
     */

    public TimePeriod intersection(TimePeriod other) {
        return intersection(other.getFrom(), other.getTo());
    }

    /**
     * Returns the intersection of the receiver and the period defined by the arguments.
     * equivalent to <CODE>intersection(new TimePeriod(start,end))</CODE>
     *
     * @param start start date of the other period.
     * @param end   end date of the other period.
     * @return the intersection
     */

    public TimePeriod intersection(Date start, Date end) {
        Date resultStart = start;
        if (from != null && (start == null || from.after(start))) {
            resultStart = from;
        }
        Date resultEnd = end;
        if (to != null && (end == null || to.before(end))) {
            resultEnd = to;
        }
        TimePeriod result = new TimePeriod(resultStart, resultEnd);
        return result.isEmpty() ? null : result;
    }

    /**
     * return a string representation of the receiver
     *
     * @return a string representation
     */
    public String toString() {
        return
                (from == null ? "" : ("From " + from)) +
                        (to == null ? "" : (" to " + to));
    }

    /**
     * Tests if this period is finite
     *
     * @return true if finite, false otherwise.
     */
    public boolean isFinite() {
        if (from == null) {
            return false;
        }
        if (to == null) {
            return false;
        }
        return !isEmpty();
    }

    public int getNumberOfDays() {
        if (from == null) {
            return -1;
        }
        if (to == null) {
            return -1;
        }
        return (int) Math.round(((double) to.getTime() - (double) from.getTime()) / 86400000.0);
    }

    public TimePeriod getPreviousPeriod(TimeZone tz) {
        if (from == null || to == null) {
            return this;
        }
        Calendar fromCalendar = Calendar.getInstance(tz);
        fromCalendar.setTime(from);
        Calendar toCalendar = Calendar.getInstance(tz);
        toCalendar.setTime(to);
        if (fromCalendar.get(Calendar.DAY_OF_MONTH) == toCalendar.get(Calendar.DAY_OF_MONTH)) {
            int months = toCalendar.get(Calendar.YEAR) * 12 + toCalendar.get(Calendar.MONTH) - fromCalendar.get(Calendar.YEAR) * 12 - fromCalendar.get(Calendar.MONTH);
            if (months > 0) {
                fromCalendar.add(Calendar.MONTH, -months);
                toCalendar.add(Calendar.MONTH, -months);
                return new TimePeriod(fromCalendar.getTime(), toCalendar.getTime());
            }
        }
        return new TimePeriod(new Date(from.getTime() - getLength()), from);
    }

    public TimePeriod getNextPeriod(TimeZone tz) {
        if (from == null || to == null) {
            return this;
        }
        Calendar fromCalendar = Calendar.getInstance(tz);
        fromCalendar.setTime(from);
        Calendar toCalendar = Calendar.getInstance(tz);
        toCalendar.setTime(to);
        if (fromCalendar.get(Calendar.DAY_OF_MONTH) == toCalendar.get(Calendar.DAY_OF_MONTH)) {
            int months = toCalendar.get(Calendar.YEAR) * 12 + toCalendar.get(Calendar.MONTH) - fromCalendar.get(Calendar.YEAR) * 12 - fromCalendar.get(Calendar.MONTH);
            if (months > 0) {
                fromCalendar.add(Calendar.MONTH, months);
                toCalendar.add(Calendar.MONTH, months);
                return new TimePeriod(fromCalendar.getTime(), toCalendar.getTime());
            }
        }
        return new TimePeriod(to, new Date(to.getTime() + getLength()));
    }

    public TimePeriod shift(int field, int amount) {
        return shift(TimeZone.getDefault(), field, amount);
    }

    public TimePeriod shift(TimeZone tz, int field, int amount) {
        return new TimePeriod(shift(from, tz, field, amount), shift(to, tz, field, amount));
    }

    private Date shift(Date in, TimeZone tz, int field, int amount) {
        if (in == null) {
            return null;
        }
        Calendar cal = Calendar.getInstance(tz);
        cal.setTime(in);
        cal.add(field, amount);
        return cal.getTime();
    }


    public boolean isAdjacent(TimePeriod itemPeriod) {
        return (getTo() != null && itemPeriod.getFrom() != null && getTo().equals(itemPeriod.getFrom())) ||
                (getFrom() != null && itemPeriod.getTo() != null && getFrom().equals(itemPeriod.getTo()));
    }

    public TimePeriod withFrom(Date from) {
        return new TimePeriod(from, this.to);
    }

    public TimePeriod withTo(Date to) {
        return new TimePeriod(this.from, to);
    }
}
