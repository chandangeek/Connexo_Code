/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.metering.readings.beans.ReadingImpl;
import com.elster.jupiter.rest.util.IntervalInfo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Range;
import com.sun.org.apache.regexp.internal.RE;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.math.BigDecimal;
import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown=true)
public class NumericalOutputRegisterDataInfo extends OutputRegisterDataInfo {

    @JsonProperty("value")
    @XmlJavaTypeAdapter(BigDecimalAsStringAdapter.class)
    public BigDecimal value;

    @JsonProperty("calculatedValue")
    @XmlJavaTypeAdapter(BigDecimalAsStringAdapter.class)
    public BigDecimal calculatedValue;

    @JsonProperty("interval")
    public IntervalInfo interval;

    public Boolean isConfirmed;

    @JsonProperty("deltaValue")
    @XmlJavaTypeAdapter(BigDecimalAsStringAdapter.class)
    public BigDecimal deltaValue;

    @JsonProperty("eventDate")
    public Instant eventDate;

    @Override
    public BaseReading createNew(ReadingType readingType) {
        if(interval != null && interval.start != null && interval.end != null){
            ReadingImpl reading = ReadingImpl.of(readingType.getMRID(), this.value, this.timeStamp);
            if (this.interval != null) {
                reading.setTimePeriod(Range.openClosed(Instant.ofEpochMilli(this.interval.start), Instant.ofEpochMilli(this.interval.end)));
            }
            return reading;
        } else {
            return ReadingImpl.of(readingType.getMRID(), this.value, this.timeStamp);
        }
    }
}
