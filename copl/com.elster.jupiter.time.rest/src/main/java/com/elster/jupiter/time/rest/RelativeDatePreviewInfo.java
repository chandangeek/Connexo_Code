/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.time.rest;

import java.time.ZonedDateTime;

public class RelativeDatePreviewInfo {
    public Long date;
    public RelativeDateInfo relativeDateInfo;
    public Integer zoneOffset;

    public RelativeDatePreviewInfo() {}

    public RelativeDatePreviewInfo(ZonedDateTime dateTime) {
        date = dateTime.toInstant().toEpochMilli();
        zoneOffset = -dateTime.getOffset().getTotalSeconds()/60;
    }

    public int parseOffsetHours() {
        int hours = 0;
        if (zoneOffset != null) {
            hours = -(zoneOffset/60);
        }
        return hours;
    }

    public int parseOffsetMinutes() {
        int minutes = 0;
        if (zoneOffset != null) {
            minutes = Math.abs(zoneOffset%60);
        }
        return minutes;
    }
}
