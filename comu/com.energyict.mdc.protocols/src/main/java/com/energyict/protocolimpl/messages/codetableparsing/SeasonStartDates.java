/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.messages.codetableparsing;

import com.elster.jupiter.calendar.Period;

import java.time.DayOfWeek;

/**
 * Describes 1 Season of the CodeTable that is now supported
 * by a {@link Period} of a {@link com.elster.jupiter.calendar.Calendar}.
 * Each season contains a startDate and a seasonID.
 */
class SeasonStartDates {

    final int year;
    final int month;
    final int day;

    int weekProfileName;

    /**
     * Constructor with each field as variable.
     * We set the weekProfileName to -1 as default (in case the parser doesn't set the weekProfileName)
     *
     * @param year  the year-value
     * @param month the month-value
     * @param day   the day-value
     */
    SeasonStartDates(int year, int month, int day) {
        this.year = year;
        this.month = month;
        this.day = day;
        this.weekProfileName = -1;
    }

    SeasonStartDates(Period period) {
        this.year = -1;
        this.month = -1;
        this.day = DayOfWeek.MONDAY.getValue();
    }

    /**
     * Getter for the season YEAR
     *
     * @return the season YEAR
     */
    public int getYear() {
        return year;
    }

    /**
     * Getter for the season MONTH
     *
     * @return the season MONTH
     */
    public int getMonth() {
        return month;
    }

    /**
     * Getter for the season DAY
     *
     * @return the season DAY
     */
    public int getDay() {
        return day;
    }

    /**
     * Setter for the weekProfileName
     *
     * @param weekProfileName the new weekProfileName
     */
    public void setWeekProfileName(int weekProfileName) {
        this.weekProfileName = weekProfileName;
    }

    /**
     * Getter for the weekProfileName
     *
     * @return the weekProfileName for this season
     */
    public String getWeekName() {
        return String.valueOf(this.weekProfileName);
    }
}
