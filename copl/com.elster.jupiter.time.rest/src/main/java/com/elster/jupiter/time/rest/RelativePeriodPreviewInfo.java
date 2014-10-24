package com.elster.jupiter.time.rest;

import java.time.ZonedDateTime;

public class RelativePeriodPreviewInfo {
    public Long start;
    public Long end;

    public RelativePeriodPreviewInfo(ZonedDateTime startDate, ZonedDateTime endDate) {
        start = startDate.toInstant().toEpochMilli();
        end =  endDate.toInstant().toEpochMilli();
    }

    public RelativePeriodPreviewInfo() {}
}
