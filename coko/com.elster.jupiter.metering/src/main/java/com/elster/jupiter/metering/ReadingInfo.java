/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.metering;

import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.metering.readings.ReadingQuality;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class ReadingInfo {
    private Meter meter;
    private UsagePoint usagePoint;
    private ReadingType readingType;
    private BaseReading reading;
    private List<? extends ReadingQuality> readingQualities = Collections.emptyList();

    public Optional<Meter> getMeter() {
        return Optional.ofNullable(meter);
    }

    public void setMeter(Meter meter) {
        this.meter = meter;
    }

    public Optional<UsagePoint> getUsagePoint() {
        return Optional.ofNullable(usagePoint);
    }

    public void setUsagePoint(UsagePoint usagePoint) {
        this.usagePoint = usagePoint;
    }

    public ReadingType getReadingType() {
        return readingType;
    }

    public void setReadingType(ReadingType readingType) {
        this.readingType = readingType;
    }

    public BaseReading getReading() {
        return reading;
    }

    public void setReading(BaseReading reading) {
        this.reading = reading;
    }

    public List<? extends ReadingQuality> getReadingQualities() {
        return readingQualities;
    }

    public void setReadingQualities(List<? extends ReadingQuality> readingQualities) {
        this.readingQualities = readingQualities;
    }
}