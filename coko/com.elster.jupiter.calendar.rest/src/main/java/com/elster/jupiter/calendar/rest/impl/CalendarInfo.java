package com.elster.jupiter.calendar.rest.impl;

import java.util.List;

public class CalendarInfo {
    public long id;
    public String name;
    public String category;
    public String mRID;
    public String description;
    public String timeZone;
    public long startYear;
    public List<EventInfo> events;
    public List<DayTypeInfo> dayTypes;
    public List<PeriodInfo> periods;
}
