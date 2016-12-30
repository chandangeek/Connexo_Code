package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.metering.readings.beans.ReadingImpl;
import com.elster.jupiter.rest.util.IntervalInfo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Range;

import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown=true)
public class BillingOutputRegisterDataInfo extends NumericalOutputRegisterDataInfo {

    @JsonProperty("interval")
    public IntervalInfo interval;

    @Override
    public BaseReading createNew(ReadingType readingType) {
        ReadingImpl reading = ReadingImpl.of(readingType.getMRID(), this.value, this.timeStamp);
        if (this.interval != null) {
            reading.setTimePeriod(Range.openClosed(Instant.ofEpochMilli(this.interval.start), Instant.ofEpochMilli(this.interval.end)));
        }
        return reading;    }
}
