package com.elster.jupiter.calendar.rest.impl;

public class PeriodInfo {
    public String name;
    public long fromMonth;
    public long fromDay;

    public PeriodInfo(String name, long fromMonth, long fromDay) {
        this.name = name;
        this.fromMonth = fromMonth;
        this.fromDay = fromDay;
    }
}
