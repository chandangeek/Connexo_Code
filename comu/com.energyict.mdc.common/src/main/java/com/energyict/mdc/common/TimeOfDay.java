/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * TimeOfDay.java
 *
 * Created on 14 oktober 2005, 13:04
 */

package com.energyict.mdc.common;

import java.io.Serializable;

/**
 * This class represents the time part (hours-minutes-seconds) of a date
 * <p/>
 * Examples:
 * 00:00:00 = 0 hours, 0 minutes and 0 seconds = 0 seconds in total
 * 12:15:45 = 12 hours, 15 minutes and 45 seconds = 44145 seconds in total
 * 22:24:26 = 22 hours, 24 minutes and 26 seconds = 80666 seconds in total
 *
 * @author Geert
 */
public class TimeOfDay implements Comparable, Serializable {

    private int seconds;

    /**
     * Creates a new instance of TimeOfDay
     */
    public TimeOfDay() {
        seconds = 0;
    }

    /**
     * Creates a new instance of TimeOfDay
     *
     * @param sec the time part of a date expressed in seconds
     */
    public TimeOfDay(int sec) {
        seconds = sec;
    }

    /**
     * returns the hours part of the time part of a date
     *
     * @return the hours part of the time part of a date
     */
    public int getHours() {
        return (seconds / 3600);
    }

    /**
     * returns the minutes part of the time part of a date
     *
     * @return the minutes part of the time part of a date
     */
    public int getMinutes() {
        return (seconds - getHours() * 3600) / 60;
    }

    /**
     * returns the time part of a date expressed in seconds
     *
     * @return the time part of a date expressed in seconds
     */
    public int getSeconds() {
        return seconds;
    }

    /**
     * sets the time part of a date
     *
     * @param sec the time part of a date expressed in seconds
     */
    public void setSeconds(int sec) {
        seconds = sec;
    }

    /**
     * see Comparable
     *
     * @param o object to compare with
     * @return see Comparable
     */
    public int compareTo(Object o) {
        TimeOfDay other = (TimeOfDay) o;
        return getSeconds() - other.getSeconds();
    }

    /**
     * see Object
     *
     * @param o object to compare with
     * @return see Object
     */
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        try {
            TimeOfDay other = (TimeOfDay) o;
            return getSeconds() == other.getSeconds();
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
        return getSeconds();
    }

    /**
     * Returns a String representation of the receiver.
     *
     * @return a string representation
     */
    public String toString() {
        int hours = getHours();
        int minutes = getMinutes();
        return (hours < 10 ? "0" : "") + hours +
                (minutes < 10 ? ":0" : ":") + minutes;
    }
}
