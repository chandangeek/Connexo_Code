package com.energyict.mdc.device.data.importers.impl.deviceeventsimport;

import com.energyict.mdc.device.data.importers.impl.FileImportRecord;

import java.time.ZonedDateTime;

public class DeviceEventsImportRecord extends FileImportRecord {
    private String logbookOBIScode = "";
    private String eventCode = "";
    private String description = "";
    private String eventLogID = "";
    private String deviceCode;
    private ZonedDateTime dateTime;
    private ZonedDateTime readingDate;

    public void setLogbookOBIScode(String code) {
        logbookOBIScode = code;
    }

    public String getLogbookOBIScode() {
        return logbookOBIScode;
    }

    public String getEventCode() {
        return eventCode;
    }

    public void setEventCode(String eventCode) {
        this.eventCode = eventCode;
    }

    public ZonedDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(ZonedDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEventLogID() {
        return eventLogID;
    }

    public void setEventLogID(String eventLogID) {
        this.eventLogID = eventLogID;
    }

    public String getDeviceCode() {
        return deviceCode;
    }

    public void setDeviceCode(String deviceCode) {
        this.deviceCode = deviceCode;
    }

    public ZonedDateTime getReadingDate() {
        return readingDate;
    }

    public void setReadingDate(ZonedDateTime readingDate) {
        this.readingDate = readingDate;
    }
}
