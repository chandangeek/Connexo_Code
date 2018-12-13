/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.interval;

import com.energyict.mdc.common.SqlBuilder;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * represents a timestamp as used in the EIServer database.
 * A timestamp in EIServer is represented by two integers.
 * dstamp: YYYYMMDD
 * tstamp: HHMMds where ds is the DST indicator
 * (01 if DST is active for the given time, else 00).
 * As datetime are used to identify the end of interval,
 * its format is different from traditional date formats.
 * e.g. midnight is expressed as 24:00 h of the previous day,
 * not 00:00 h of the next day
 *
 * @author Karel
 */
public class DateTime implements Comparable, Serializable {

    private static ThreadLocal<Calendar> threadLocalCalendar = new ThreadLocal<>();
    private int dstamp;
    private int tstamp;
    private TimeZone timeZone;
    private transient Date cachedDate;
    private transient SimpleDateFormat dateFormatter = null;

    /**
     * Creates a new DateTime
     *
     * @param dstamp   date stamp
     * @param tstamp   time stamp
     * @param timeZone TimeZone of the new DateTime
     */
    public DateTime(int dstamp, long tstamp, TimeZone timeZone) {
        this(dstamp, (int) tstamp, timeZone);
        if (dstamp == 0) {
            this.dstamp = 0;
            this.tstamp = 0;
            Calendar calendar = getCalendar(timeZone);
            calendar.setTime(new Date(tstamp * 1000L));
            setDateAndTime(calendar);
        }
    }

    /**
     * Creates a new DateTime
     *
     * @param dstamp   date stamp
     * @param tstamp   time stamp
     * @param timeZone TimeZone of the new DateTime
     */
    public DateTime(int dstamp, int tstamp, TimeZone timeZone) {
        this.dstamp = dstamp;
        this.tstamp = tstamp;
        this.timeZone = timeZone;
    }

    /**
     * Creates a new DateTime
     *
     * @param date     java date
     * @param timeZone TimeZone of the new DateTime
     */
    public DateTime(Date date, TimeZone timeZone) {
        Calendar calendar = getCalendar(timeZone);
        calendar.setTime(date);
        setDateAndTime(calendar);
    }

    /**
     * Creates a new DateTime
     *
     * @param date     java date
     * @param timeZone TimeZone of the new DateTime
     * @param useDst   add supplementary second to tstamp to indicate a DST summer time
     */
    public DateTime(Date date, TimeZone timeZone, boolean useDst) {
        Calendar calendar = getCalendar(timeZone);
        calendar.setTime(date);
        setDateAndTime(calendar, useDst);
    }

    /**
     * creates a new DateTime
     *
     * @param time     UTC time in milliseconds since 1/1/1970
     * @param timeZone TimeZone of the new DateTime
     */
    public DateTime(long time, TimeZone timeZone) {
        this(new Date(time), timeZone);
    }

    /**
     * creates a new DateTime
     *
     * @param calendar to base the new DateTime on
     */
    public DateTime(Calendar calendar) {
        setDateAndTime((Calendar) calendar.clone());
    }

    /**
     * return the receiver's dstamp field
     *
     * @return an integer representing the receiver's date
     */
    public int getDstamp() {
        return dstamp;
    }

    /**
     * return the receiver's tstamp field
     *
     * @return an integer representing the receiver's time
     */
    public int getTstamp() {
        return tstamp;
    }

    /**
     * return the receiver's TimeZone field
     *
     * @return the receiver's TimeZone
     */
    public TimeZone getTimeZone() {
        return timeZone;
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * @param obj the reference object with which to compare.
     * @return true if this object is the same as the obj argument; false otherwise.
     */
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        try {
            return compareTo(obj) == 0;
        } catch (ClassCastException ex) {
            return false;
    }
    }

    /**
     * Returns a hash code value for the object.
     *
     * @return a hash code value for this object.
     */
    public int hashCode() {
        return getDate().hashCode();
    }

    /**
     * Compares this object with the specified object for order.
     * Returns a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than the specified object.
     *
     * @param obj the Object to be compared.
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than the specified object.
     */
    public int compareTo(java.lang.Object obj) {
        DateTime other = (DateTime) obj;
        return getDate().compareTo(other.getDate());
    }

    /**
     * return the java.util.Date corresponding to the receiver
     *
     * @return the java.util.Date
     */
    public Date getDate() {
        if (cachedDate == null) {
            cachedDate = doGetDate();
        }
        return cachedDate;
    }

    private Date doGetDate() {
        int year = dstamp / 10000;
        int remainder = dstamp % 10000;
        int month = remainder / 100;
        int day = remainder % 100;
        int hour = tstamp / 10000;
        remainder = tstamp % 10000;
        int minute = remainder / 100;
        int second = remainder % 100;
        boolean isDst = second == 1;
        if (isDst) {
            second = 0;
        }
        Calendar calendar = getCalendar(timeZone);
        calendar.clear();
        calendar.set(year, month - 1, day, hour, minute, second);
        Date date = calendar.getTime();
        // if we have winter time, but dst indicator , this date/time is
        // in the transition hour between summer time and winter time
        if (isDst && !timeZone.inDaylightTime(date)) {
            date = new Date(date.getTime() - 3600000);
        }
        // if we have no dst indicator and a one hour difference , this is
        // the transition moment between winter time and summer time
        if (!isDst && ((hour - calendar.get(Calendar.HOUR_OF_DAY)) == 1)) {
            date = new Date(date.getTime() + 3600000);
        }
        return date;
    }

    /**
     * return the java.util.Calendar corresponding to the receiver
     *
     * @return the java.util.Calendar
     */
    public Calendar getCalendar() {
        Calendar calendar = getCalendar(timeZone);
        calendar.setTime(getDate());
        return calendar;
    }


    /**
     * returns the time represented by the receiver in milliseconds since 1/1/1970, UTC
     *
     * @return the time in milliseconds
     */
    public long getTime() {
        return getDate().getTime();
    }

    /** */
    private void setDateAndTime(Calendar calendar) {
        setDateAndTime(calendar, true);
    }

    private void setDateAndTime(Calendar calendar, boolean useDst) {
        this.timeZone = calendar.getTimeZone();
        this.cachedDate = calendar.getTime();
        // go back 1 second into time to get previous period;
        Date readingDateMinusOne = new java.util.Date(calendar.getTime().getTime() - 1000);
        calendar.setTime(readingDateMinusOne);
        dstamp =
                calendar.get(Calendar.YEAR) * 10000 +
                        (calendar.get(Calendar.MONTH) + 1) * 100 +
                        calendar.get(Calendar.DAY_OF_MONTH);
        int seconds = (calendar.get(Calendar.SECOND) + 1) % 60;
        int minutes = calendar.get(Calendar.MINUTE);
        int hours = calendar.get(Calendar.HOUR_OF_DAY);
        if (seconds == 0) {
            minutes++;
            if (minutes == 60) {
                minutes = 0;
                hours++;
            }
        }
        tstamp = hours * 10000 + minutes * 100 + seconds;
        if (useDst) {
            if (calendar.getTimeZone().inDaylightTime(readingDateMinusOne)) {
                tstamp++;
            }
        }
    }

    /**
     * Returns a string representation of the receiver
     *
     * @return a string representation of the receiver
     */
    public String toString() {
        int year = dstamp / 10000;
        int remainder = dstamp % 10000;
        int month = remainder / 100;
        int day = remainder % 100;
        int hour = tstamp / 10000;
        remainder = tstamp % 10000;
        int minute = remainder / 100;
        int second = remainder % 100;
        return
                "" +
                        (day > 9 ? "" : "0") +
                        day +
                        "/" +
                        (month > 9 ? "" : "0") +
                        month +
                        "/" +
                        year +
                        " " +
                        (hour > 9 ? "" : "0") +
                        hour +
                        ":" +
                        (minute > 9 ? "" : "0") +
                        minute +
                        ":" +
                        (second > 9 ? "" : "0") +
                        second;
    }

    /**
     * Returns a string representation of the receiver
     * The representation for the last datetime for is set to date 24:00
     *
     * @return a string representation of the receiver
     */
    public String getDisplayString() {
        return getDateFormatter().format(getDate());
    }

    private SimpleDateFormat getDateFormatter() {
        if (dateFormatter == null) {
            dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        }
        return dateFormatter;
    }

    /**
     * return the week number of the week containing the receiver,
     * using monday as the first day of the week
     *
     * @return the week number
     */
    public int getWeek() {
        Calendar calendar = getCalendar(timeZone);
        int year = dstamp / 10000;
        int remainder = dstamp % 10000;
        int month = remainder / 100;
        int day = remainder % 100;
        calendar.set(year, month - 1, day);
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        int week = calendar.get(Calendar.WEEK_OF_YEAR);
        return week;
    }

    /**
     * Returns the receiver's month (1-12)
     *
     * @return the month (1-12)
     */
    public int getMonth() {
        return (dstamp % 10000) / 100;
    }

    /**
     * Returns the receiver's year
     *
     * @return the year
     */
    public int getYear() {
        return dstamp / 10000;
    }

    /**
     * Returns the receiver's day in month
     *
     * @return the day
     */
    public int getDay() {
        return dstamp % 100;
    }

    /**
     * Returns the receiver's hour (0-24)
     *
     * @return the hour (0-24)
     */

    public int getHour() {
        return tstamp / 10000;
    }

    /**
     * Returns the receiver's minute
     *
     * @return the minute
     */

    public int getMinute() {
        return (tstamp % 10000) / 100;
    }

    /**
     * Returns a boolean indicating if the receiver's is in DST
     *
     * @return true if the receiver is in DST, false otherwise
     */

    public boolean isDst() {
        switch (tstamp % 100) {
            case 0:
                return false;
            case 1:
                return true;
            default:
                return timeZone.inDaylightTime(getDate());
        }
    }

    // arithmetics

    /**
     * return a new DateTime, seconds later than the receiver
     *
     * @param seconds seconds to add to the receiver
     * @return the new DateTime
     */
    public DateTime addSeconds(int seconds) {
        Calendar cal = (Calendar) (getCalendar().clone());
        cal.add(Calendar.SECOND, seconds);
        return new DateTime(cal);
    }

    /**
     * tests if the receiver is in the transition hour from summer time
     * to winter time
     *
     * @return true if in transition hour
     */
    public boolean inDoubt() {
        if (!timeZone.useDaylightTime()) {
            return false;
        }
        Date date = getDate();
        if (timeZone.inDaylightTime(date)) {
            return !timeZone.inDaylightTime(new Date(date.getTime() + 3601L * 1000L));
        } else {
            return timeZone.inDaylightTime(new Date(date.getTime() - 3601L * 1000L));
        }
    }

    /**
     * extract dstamp value from the argument
     *
     * @param cal calendar to extract from
     * @return an integer dstamp
     */
    static public int extractDstamp(Calendar cal) {
        return extractDstamp(cal, true);
    }

    /**
     * extract dstamp value from the argument
     *
     * @param cal            calendar to extract from
     * @param intervalFormat if true use EIServer conventions,
     *                       else use Java conventions
     * @return an integer dstamp
     */
    static public int extractDstamp(Calendar cal, boolean intervalFormat) {
        if (intervalFormat) {
            return new DateTime(cal).getDstamp();
        } else {
            return
                    cal.get(Calendar.YEAR) * 10000 +
                            (cal.get(Calendar.MONTH) + 1) * 100 +
                            cal.get(Calendar.DAY_OF_MONTH);
        }
    }

    /**
     * extract tstamp value from the argument
     *
     * @param cal calendar to extract from
     * @return an integer tstamp
     */
    static public int extractTstamp(Calendar cal) {
        return extractTstamp(cal, true);
    }

    /**
     * extract tstamp value from the argument
     *
     * @param cal            calendar to extract from
     * @param intervalFormat if true use EIServer conventions,
     *                       else use Java conventions
     * @return an integer tstamp
     */
    static public int extractTstamp(Calendar cal, boolean intervalFormat) {
        if (intervalFormat) {
            return new DateTime(cal).getTstamp();
        } else {
            return
                    cal.get(Calendar.HOUR_OF_DAY) * 10000 +
                            cal.get(Calendar.MINUTE) * 100 +
                            cal.get(Calendar.SECOND);
        }
    }

    /**
     * returns the where clause to use
     *
     * @return the where clause to use
     * @deprecated use whereClause(DateTime from, DateTime to)
     */
    static public String whereClause() {
        return whereClause(null);
    }

    /**
     * returns the where clause to use (using an alias)
     *
     * @param alias the alias to use (alias.dstamp/alias.tstamp)
     * @return the where clause to use
     * @deprecated use whereClause(String alias, DateTime from, DateTime to)
     */
    static public String whereClause(String alias) {
        StringBuffer buffer = new StringBuffer();
        String prefix;
        if (alias == null || alias.length() == 0) {
            prefix = "";
        } else {
            prefix = alias + ".";
        }
        buffer.append("(");
        buffer.append(prefix);
        buffer.append("dstamp >= ? and ");
        buffer.append(prefix);
        buffer.append("dstamp <= ? and not (");
        buffer.append(prefix);
        buffer.append("dstamp = ? and ");
        buffer.append(prefix);
        buffer.append("tstamp <= ?) and not (");
        buffer.append(prefix);
        buffer.append("dstamp = ? and ");
        buffer.append(prefix);
        buffer.append("tstamp > ?)) ");
        return buffer.toString();
    }

    /**
     * returns the where clause to use
     *
     * @param from start of interval
     * @param to   end of interval
     * @return the where clause to use
     */
    static public String whereClause(DateTime from, DateTime to) {
        return whereClause(null, from, to);
    }

    /**
     * returns the where clause to use (using an alias)
     *
     * @param from  start of interval
     * @param to    end of interval
     * @param alias the alias to use (alias.dstamp/alias.tstamp)
     * @return the where clause to use
     */
    static public String whereClause(String alias, DateTime from, DateTime to) {
        StringBuffer buffer = new StringBuffer();
        String prefix;
        if (alias == null || alias.length() == 0) {
            prefix = "";
        } else {
            prefix = alias + ".";
        }
        buffer.append("(");
        buffer.append(prefix);
        buffer.append("dstamp >= ? and ");
        buffer.append(prefix);
        buffer.append("dstamp <= ? and not (");
        buffer.append(prefix);
        buffer.append("dstamp = ? and (");
        buffer.append(prefix);
        buffer.append("tstamp <= ?");
        if (from.inDoubt()) {
            if (from.isDst()) {
                buffer.append(" and mod(");
                buffer.append(prefix);
                buffer.append("tstamp,10) = 1");
            } else {
                buffer.append(" or mod(");
                buffer.append(prefix);
                buffer.append("tstamp,10) = 1");
            }
        }
        buffer.append(")) and not (");
        buffer.append(prefix);
        buffer.append("dstamp = ? and (");
        buffer.append(prefix);
        buffer.append("tstamp > ? ");
        if (to.inDoubt()) {
            if (to.isDst()) {
                buffer.append(" or mod(");
                buffer.append(prefix);
                buffer.append("tstamp,10) = 0");
            } else {
                buffer.append(" and mod(");
                buffer.append(prefix);
                buffer.append("tstamp,10) = 0");
            }
        }
        buffer.append(")))");
        return buffer.toString();
    }

    /**
     * binds the given parameters to the prepared statement
     *
     * @param statement the prepared statement
     * @param offset    the starting parameter index
     * @param from      the starting date
     * @param to        the end date
     * @return the parameter index to use further on
     * @throws SQLException when a database error occured
     */
    static public int bind(PreparedStatement statement, int offset, DateTime from, DateTime to) throws SQLException {
        statement.setInt(offset++, from.getDstamp());
        statement.setInt(offset++, to.getDstamp());
        statement.setInt(offset++, from.getDstamp());
        statement.setInt(offset++, from.getTstamp());
        statement.setInt(offset++, to.getDstamp());
        statement.setInt(offset++, to.getTstamp());
        return offset;
    }

    /**
     * returns the where clause to use
     *
     * @param from start of interval
     * @param to   end of interval
     * @return the where clause to use
     */
    static public String fastWhereClause(DateTime from, DateTime to) {
        return DateTime.fastWhereClause(null, from, to);
    }

    /**
     * returns the where clause to use (using an alias)
     *
     * @param from  start of interval
     * @param to    end of interval
     * @param alias the alias to use (alias.dstamp/alias.tstamp)
     * @return the where clause to use
     */
    static public String fastWhereClause(String alias, DateTime from, DateTime to) {
        if (from.getTstamp() < 240000 || to.getTstamp() < 240000) {
            return DateTime.whereClause(alias, from, to);
        }
        String prefix;
        if (alias == null || alias.length() == 0) {
            prefix = "";
        } else {
            prefix = alias + ".";
        }
        StringBuffer buffer = new StringBuffer(" (");
        buffer.append(prefix);
        buffer.append("dstamp > ? and ");
        buffer.append(prefix);
        buffer.append("dstamp <= ?) ");
        return buffer.toString();
    }

    /**
     * binds the given parameters to the prepared statement
     *
     * @param statement the prepared statement
     * @param offset    the starting parameter index
     * @param from      the starting date
     * @param to        the end date
     * @return the parameter index to use further on
     * @throws SQLException when a database error occured
     */
    static public int fastBind(PreparedStatement statement, int offset, DateTime from, DateTime to) throws SQLException {
        if (from.getTstamp() < 240000 || to.getTstamp() < 240000) {
            return DateTime.bind(statement, offset, from, to);
        }
        statement.setInt(offset++, from.getDstamp());
        statement.setInt(offset++, to.getDstamp());
        return offset;
    }

    /**
     * appends the fast where clause to the given sql builder
     *
     * @param builder the SQL builder
     * @param alias   the alias to use (alias.dstamp/alias.tstamp)
     * @param from    start of interval
     * @param to      end of interval
     */
    public static void appendFastWhereClause(SqlBuilder builder, String alias, DateTime from, DateTime to) {
        if (from.getTstamp() < 240000 || to.getTstamp() < 240000) {
            DateTime.appendSlowWhereClause(builder, alias, from, to);
            return;
        }
        String prefix;
        if (alias == null || alias.length() == 0) {
            prefix = "";
        } else {
            prefix = alias + ".";
        }
        builder.append(" (");
        builder.append(prefix);
        builder.append("dstamp > ? and ");
        builder.bindInt(from.getDstamp());
        builder.append(prefix);
        builder.append("dstamp <= ?) ");
        builder.bindInt(to.getDstamp());
    }

    private static Calendar getCalendar(TimeZone timeZone) {
        if (threadLocalCalendar.get() == null) {
            threadLocalCalendar.set(Calendar.getInstance());
        }
        Calendar calendar = threadLocalCalendar.get();
        calendar.setTimeZone(timeZone);
        return calendar;
    }
    /**
     * appends the slow where clause to the given sql builder
     *
     * @param builder the SQL builder
     * @param alias   the alias to use (alias.dstamp/alias.tstamp)
     * @param from    start of interval
     * @param to      end of interval
     */
    private static void appendSlowWhereClause(SqlBuilder builder, String alias, DateTime from, DateTime to) {
        String prefix;
        if (alias == null || alias.isEmpty()) {
            prefix = "";
        } else {
            prefix = alias + ".";
        }
        builder.append("(");
        builder.append(prefix);
        builder.append("dstamp >= ? and ");
        builder.bindInt(from.getDstamp());
        builder.append(prefix);
        builder.append("dstamp <= ? and not (");
        builder.bindInt(to.getDstamp());
        builder.append(prefix);
        builder.append("dstamp = ? and ");
        builder.bindInt(from.getDstamp());
        builder.append(prefix);
        builder.append("tstamp <= ?) and not (");
        builder.bindInt(from.getTstamp());
        builder.append(prefix);
        builder.append("dstamp = ? and ");
        builder.bindInt(to.getDstamp());
        builder.append(prefix);
        builder.append("tstamp > ?)) ");
        builder.bindInt(to.getTstamp());
    }

}
