package com.energyict.protocolimpl.messages.codetableparsing;

import com.energyict.mdc.upl.messages.legacy.TariffCalendarExtractor;

/**
 * Describes 1 Season of the CodeTable.
 * Each season contains a startDate and a seasonID
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

    /**
     * Constructor with a CodeCalendar as variable
     *
     * @param rule the CodeCalendar containing the necessary dates
     */
    SeasonStartDates(TariffCalendarExtractor.CalendarRule rule) {
        this.year = rule.year();
        this.month = rule.month();
        this.day = rule.day();
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
