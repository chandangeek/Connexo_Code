/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.metering;

import com.elster.jupiter.metering.readings.BaseReading;

public class ReadingInfoType {
    private Meter meter;
    private UsagePoint usagePoint;
    private ReadingType readingType;
    private BaseReading reading;

    public Meter getMeter() {
        return meter;
    }

    public void setMeter(Meter meter) {
        this.meter = meter;
    }

    public UsagePoint getUsagePoint() {
        return usagePoint;
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
}