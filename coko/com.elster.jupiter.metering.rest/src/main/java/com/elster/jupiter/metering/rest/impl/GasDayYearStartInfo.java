package com.elster.jupiter.metering.rest.impl;

import com.elster.jupiter.util.time.DayMonthTime;

public class GasDayYearStartInfo {
    public int month;
    public int day;
    public int hours;
    public int minutes;
    public int seconds;

    public GasDayYearStartInfo() {
        super();
    }

    public GasDayYearStartInfo(DayMonthTime yearStart) {
        this();
        if (yearStart!=null) {
            this.month = yearStart.getMonthValue();
            this.day = yearStart.getDayOfMonth();
            this.hours = yearStart.getHour();
            this.minutes = yearStart.getMinute();
            this.seconds = yearStart.getSecond();
        }
    }
}
