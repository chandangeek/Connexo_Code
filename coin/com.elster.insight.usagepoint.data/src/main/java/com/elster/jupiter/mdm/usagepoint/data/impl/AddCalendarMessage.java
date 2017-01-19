package com.elster.jupiter.mdm.usagepoint.data.impl;

class AddCalendarMessage {

    private long usagePointId;
    private long calendarId;
    private boolean immediately;
    private long startTime;

    public AddCalendarMessage() {
    }

    public AddCalendarMessage(long usagePointId, long calendarId, boolean immediately, long startTime) {
        this.usagePointId = usagePointId;
        this.calendarId = calendarId;
        this.immediately = immediately;
        this.startTime = startTime;
    }

    public long getUsagePointId() {
        return usagePointId;
    }

    public long getCalendarId() {
        return calendarId;
    }

    public long getStartTime() {
        return startTime;
    }

    public boolean isImmediately() { return immediately; }

    public void setImmediately(boolean immediately) { this.immediately = immediately;}

    public void setUsagePointId(long usagePointId) {
        this.usagePointId = usagePointId;
    }

    public void setCalendarId(long calendarId) {
        this.calendarId = calendarId;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }
}
