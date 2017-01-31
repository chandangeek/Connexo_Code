/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.time.rest;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class RelativePeriodPreviewInfo {
    public RelativeDatePreviewInfo start;
    public RelativeDatePreviewInfo end;

    public RelativePeriodPreviewInfo(ZonedDateTime startDate, ZonedDateTime endDate) {
        start = new RelativeDatePreviewInfo(startDate);
        end =  new RelativeDatePreviewInfo(endDate);
    }

    public RelativePeriodPreviewInfo(Instant startDate, Instant endDate, ZoneId zoneId) {
        this(startDate.atZone(zoneId), endDate.atZone(zoneId));
    }

    public RelativePeriodPreviewInfo() {}
}
