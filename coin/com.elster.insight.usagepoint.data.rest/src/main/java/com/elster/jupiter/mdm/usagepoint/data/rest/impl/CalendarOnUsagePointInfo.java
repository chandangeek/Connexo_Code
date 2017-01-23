package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.calendar.rest.CalendarInfo;

public class CalendarOnUsagePointInfo {

    public long usagePointId;
    public CalendarInfo calendar;
    public long fromTime;
    public Long toTime;
    public boolean immediately;
    public CalendarOnUsagePointInfo next;
}
