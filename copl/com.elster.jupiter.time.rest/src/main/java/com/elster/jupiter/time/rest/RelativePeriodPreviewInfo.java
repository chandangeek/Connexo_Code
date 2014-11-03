package com.elster.jupiter.time.rest;

import java.time.ZonedDateTime;

public class RelativePeriodPreviewInfo {
    public RelativeDatePreviewInfo start;
    public RelativeDatePreviewInfo end;

    public RelativePeriodPreviewInfo(ZonedDateTime startDate, ZonedDateTime endDate) {
        start = new RelativeDatePreviewInfo(startDate);
        end =  new RelativeDatePreviewInfo(endDate);
    }

    public RelativePeriodPreviewInfo() {}
}
