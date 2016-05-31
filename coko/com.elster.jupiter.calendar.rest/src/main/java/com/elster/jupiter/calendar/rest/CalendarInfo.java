package com.elster.jupiter.calendar.rest;

import com.elster.jupiter.calendar.rest.impl.DayInfo;
import com.elster.jupiter.calendar.rest.impl.DayTypeInfo;
import com.elster.jupiter.calendar.rest.impl.DaysPerTypeInfo;
import com.elster.jupiter.calendar.rest.impl.EventInfo;
import com.elster.jupiter.calendar.rest.impl.PeriodInfo;

import java.util.List;

public class CalendarInfo {
    public long id;
    public String name;
    public String category;
    public String mRID;
    public String description;
    public String timeZone;
    public int startYear;
    public List<EventInfo> events;
    public List<DayTypeInfo> dayTypes;
    public List<PeriodInfo> periods;
    public List<DayInfo> weekTemplate;
    public List<DaysPerTypeInfo> daysPerType;
    public boolean inUse = true;

    public CalendarInfo() {

    }
}
