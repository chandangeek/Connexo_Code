/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.interval;

import com.elster.jupiter.time.TimeDuration;

import org.joda.time.DateTimeConstants;

import java.text.ChoiceFormat;
import java.text.MessageFormat;
import java.util.Calendar;

/**
 * Models a partial specification within the datetime continuum
 * that only knows about the time aspects within a day.
 * It is independent of TimeZones and/or daylight savings time.
 * It uses milli seconds as a basis and these represent
 * the number of millis seconds after midnight.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-16 (12:57)
 */
public class PartialTime implements Comparable<PartialTime> {

    private static final int MIDNIGHT = 0;

    private int millis = MIDNIGHT;

    /**
     * Creates a new PartialTime that represents
     * the time since midnight on any day counted
     * in the number of milliseconds.
     *
     * @param millis The number of milliseconds
     * @return The PartialTime
     */
    public static PartialTime fromMilliSeconds (int millis) {
        return new PartialTime(millis);
    }

    /**
     * Creates a new PartialTime that represents
     * the time since midnight on any day counted
     * in the number of seconds.
     *
     * @param seconds The number of seconds
     * @return The PartialTime
     */
    public static PartialTime fromSeconds (int seconds) {
        return new PartialTime(seconds * DateTimeConstants.MILLIS_PER_SECOND);
    }

    /**
     * Creates a new PartialTime that represents
     * the time since midnight on any day counted
     * in the number of seconds.
     *
     * @param minutes The number of minutes
     * @return The PartialTime
     */
    public static PartialTime fromMinutes (int minutes) {
        return fromSeconds(minutes * DateTimeConstants.SECONDS_PER_MINUTE);
    }

    /**
     * Creates a new PartialTime that represents
     * the time since midnight on any day counted
     * in the number of hours.
     *
     * @param hours The number of hours
     * @return The PartialTime
     */
    public static PartialTime fromHours (int hours) {
        return fromMinutes(hours * DateTimeConstants.MINUTES_PER_HOUR);
    }

    /**
     * Creates a new PartialTime that represents
     * the time since midnight on any day counted
     * in the number of hours and minutes.
     *
     * @param hours The number of hours
     * @param minutes The number of minutes
     * @return The PartialTime
     */
    public static PartialTime fromHoursAndMinutes (int hours, int minutes) {
        return fromHours(hours).plus(fromMinutes(minutes));
    }

    /**
     * Creates a new PartialTime that represents
     * the time since midnight on any day counted
     * in the number of hours, minutes and minutes.
     *
     * @param hours The number of hours
     * @param minutes The number of minutes
     * @return The PartialTime
     */
    public static PartialTime fromHoursMinutesAndSeconds (int hours, int minutes, int seconds) {
        return fromHours(hours).plus(fromMinutes(minutes)).plus(fromSeconds(seconds));
    }

    /**
     * Adds the other PartialTime to this PartialTime.
     * Time that overflows to anohter day is discarded, i.e.
     * if you add 12 hours to a PartialTime that represent 4 pm
     * the the result will be a PartialTime that represent 4 am.
     *
     * @param other The other PartialTime that is added
     * @return The sum of the two PartialTimes modulo
     */
    public PartialTime plus (PartialTime other) {
        return fromMilliSeconds(((this.getMillis() + other.getMillis()) % DateTimeConstants.MILLIS_PER_DAY));
    }

    @Override
    public boolean equals (Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject instanceof PartialTime) {
            PartialTime other = (PartialTime) otherObject;
            return this.getMillis() == other.getMillis();
        }
        else {
            return false;
        }
    }

    @Override
    public int hashCode () {
        return millis;
    }

    @Override
    public String toString () {
        MessageFormat messageFormat = new MessageFormat("{0,number,00}:{1,number,00}{2}");
        double[] zeroOrNot = {0, ChoiceFormat.nextDouble(0)};
        String[] zeroOrNotPatterns = {"", ":{2,number,00}"};
        ChoiceFormat secondsFormat = new ChoiceFormat(zeroOrNot, zeroOrNotPatterns);
        messageFormat.setFormatByArgumentIndex(2, secondsFormat);
        Integer[] messageParts = {this.getHours(), this.getMinutes(), this.getSeconds()};
        return messageFormat.format(messageParts);
    }

    private int getHours () {
        return this.getRawSeconds() / DateTimeConstants.SECONDS_PER_HOUR;
    }

    private int getMinutes () {
        return (this.getRawSeconds() % DateTimeConstants.SECONDS_PER_HOUR) / DateTimeConstants.SECONDS_PER_MINUTE;
    }

    private int getSeconds () {
        return this.getRawSeconds() % DateTimeConstants.SECONDS_PER_MINUTE;
    }

    private int getRawSeconds () {
        return this.getMillis() / DateTimeConstants.MILLIS_PER_SECOND;
    }

    @Override
    public int compareTo (PartialTime other) {
        return new Integer(this.getMillis()).compareTo(other.getMillis());
    }

    /**
     * Tests if this PartialTime is strictly after the other PartialTime.
     *
     * @param other The other PartialTime
     * @return A flag that indicates if this PartialTime is strictly after the other PartialTime
     */
    public boolean after (PartialTime other) {
        return this.compareTo(other) > 0;
    }

    /**
     * Tests if this PartialTime is strictly before the other PartialTime.
     *
     * @param other The other PartialTime
     * @return A flag that indicates if this PartialTime is strictly before the other PartialTime
     */
    public boolean before (PartialTime other) {
        return this.compareTo(other) < 0;
    }

    /**
     * Gets the number of millisseconds after midnight of every day.
     *
     * @return The number of milliseconds
     */
    public int getMillis () {
        return this.millis;
    }

    private PartialTime (int millis) {
        super();
        this.millis = millis;
    }

    /**
     * Copies this PartialTime to the Calendar,
     * setting the time information of the Calendar
     * to this PartialTime.
     * As an example:
     * Consider a calendar set to 2013-01-09 14:04:00:00 (UTC)
     * and a PartialTime.fromHours(3)
     * then copying that PartialTime to the Calendar will
     * modify the Calendar to 2013-01-09 03:00:00:00 (UTC)
     *
     * @param calendar The Calendar
     */
    public void copyTo (Calendar calendar) {
        new TimeDuration(1, TimeDuration.TimeUnit.DAYS).truncate(calendar);
        calendar.add(Calendar.MILLISECOND, this.getMillis());
    }

}
