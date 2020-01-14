package com.energyict.protocolimplv2.dlms.a2.profile;

import java.util.Calendar;

public class SelectiveEntryFilter {

    private Calendar actualCalendar;
    private Calendar fromCalendar;
    private Calendar toCalendar;

    public SelectiveEntryFilter(Calendar fromCalendar, Calendar toCalendar, Calendar actualCalendar) {
        this.fromCalendar = fromCalendar;
        this.toCalendar = toCalendar;
        this.actualCalendar = actualCalendar;
    }

    public int getFromIndex() {
        return (int)((actualCalendar.getTimeInMillis() - toCalendar.getTimeInMillis())/(1000L*3600L)+1);
    }


    public int getToIndex() {
        return (int)((actualCalendar.getTimeInMillis() - fromCalendar.getTimeInMillis())/(1000L*3600L));
    }
}
