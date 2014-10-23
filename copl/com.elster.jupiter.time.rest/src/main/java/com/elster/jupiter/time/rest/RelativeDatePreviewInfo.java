package com.elster.jupiter.time.rest;

import java.time.ZonedDateTime;

public class RelativeDatePreviewInfo {
    public Long referenceDate;
    public RelativeDateInfo relativeDateInfo;
    public Integer zoneOffset;

    public RelativeDatePreviewInfo() {}

    public RelativeDatePreviewInfo(ZonedDateTime dateTime) {
        referenceDate = dateTime.toInstant().toEpochMilli();
    }

    public int getOffsetHours() {
        int hours = 0;
        if (zoneOffset != null) {
            hours = -(zoneOffset/60);
        }
        return hours;
    }

    public int getOffsetMinutes() {
        int minutes = 0;
        if (zoneOffset != null) {
            minutes = Math.abs(zoneOffset%60);
        }
        return minutes;
    }
}
