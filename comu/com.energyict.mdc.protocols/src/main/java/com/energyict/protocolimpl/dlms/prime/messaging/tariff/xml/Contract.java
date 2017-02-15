/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.dlms.prime.messaging.tariff.xml;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Contract implements Serializable {

    protected List<Season> season;

    protected List<Week> week;

    protected List<Day> day;

    protected List<SpecialDays> specialDays;

    protected String actDate;

    protected String calendarName;

    protected int calendarType;

    protected int c;

    public List<Season> getSeason() {
        if (season == null) {
            season = new ArrayList<Season>();
        }
        return this.season;
    }

    public void addSeason(Season season) {
        getSeason().add(season);
    }

    public void addWeek(Week week) {
        getWeek().add(week);
    }

    public void addDay(Day day) {
        getDay().add(day);
    }

    public List<Week> getWeek() {
        if (week == null) {
            week = new ArrayList<Week>();
        }
        return this.week;
    }

    public List<Day> getDay() {
        if (day == null) {
            day = new ArrayList<Day>();
        }
        return this.day;
    }

    public List<SpecialDays> getSpecialDays() {
        if (specialDays == null) {
            specialDays = new ArrayList<SpecialDays>();
        }
        return this.specialDays;
    }

    public void addSpecialDay(SpecialDays specialDay) {
        getSpecialDays().add(specialDay);
    }

    /**
     * Gets the value of the actDate property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getActDate() {
        return actDate;
    }

    /**
     * Sets the value of the actDate property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setActDate(String value) {
        this.actDate = value;
    }

    /**
     * Gets the value of the calendarName property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getCalendarName() {
        return calendarName;
    }

    /**
     * Sets the value of the calendarName property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setCalendarName(String value) {
        this.calendarName = value;
    }

    /**
     * Gets the value of the calendarType property.
     */
    public int getCalendarType() {
        return calendarType;
    }

    /**
     * Sets the value of the calendarType property.
     */
    public void setCalendarType(int value) {
        this.calendarType = value;
    }

    /**
     * Gets the value of the c property.
     */
    public int getC() {
        return c;
    }

    /**
     * Sets the value of the c property.
     */
    public void setC(int value) {
        this.c = value;
    }

}
