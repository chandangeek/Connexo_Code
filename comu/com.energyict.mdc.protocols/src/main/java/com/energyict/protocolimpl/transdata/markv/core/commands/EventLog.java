/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * EventLog.java
 *
 * Created on 11 augustus 2005, 17:18
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.transdata.markv.core.commands;

import java.util.Date;

/**
 *
 * @author koen
 */
public class EventLog {

    private int eventLogCode;
    private Date eventDate;
    private int eventAverageMagnitude;
    private long eventDuration;
    private int durationUnit;

    /** Creates a new instance of EventLog */
    public EventLog(int eventLogCode,Date eventDate,int eventAverageMagnitude,long eventDuration,int durationUnit) {
        this.setEventLogCode(eventLogCode);
        this.setEventDate(eventDate);
        this.setEventAverageMagnitude(eventAverageMagnitude);
        this.setEventDuration(eventDuration);
        this.setDurationUnit(durationUnit);
    }

    public String toString() {
        return "EventLog: eventLogCode="+getEventLogCode()+" ("+EventLogCodes.getEventLogMapping(getEventLogCode())+"), eventDate="+getEventDate()+", eventAverageMagnitude="+getEventAverageMagnitude()+", eventDuration="+getEventDuration()+", durationUnit="+getDurationUnit();
    }

    public int getEventLogCode() {
        return eventLogCode;
    }

    public Date getEventDate() {
        return eventDate;
    }

    public int getEventAverageMagnitude() {
        return eventAverageMagnitude;
    }

    public long getEventDuration() {
        return eventDuration;
    }

    public int getDurationUnit() {
        return durationUnit;
    }

    private void setEventLogCode(int eventLogCode) {
        this.eventLogCode = eventLogCode;
    }

    private void setEventDate(Date eventDate) {
        this.eventDate = eventDate;
    }

    private void setEventAverageMagnitude(int eventAverageMagnitude) {
        this.eventAverageMagnitude = eventAverageMagnitude;
    }

    private void setEventDuration(long eventDuration) {
        this.eventDuration = eventDuration;
    }

    private void setDurationUnit(int durationUnit) {
        this.durationUnit = durationUnit;
    }

}
