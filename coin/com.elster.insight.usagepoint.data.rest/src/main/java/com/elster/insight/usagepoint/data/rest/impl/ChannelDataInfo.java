package com.elster.insight.usagepoint.data.rest.impl;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.elster.insight.common.rest.IntervalInfo;
import com.elster.insight.usagepoint.data.rest.BigDecimalAsStringAdapter;
import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.metering.readings.beans.IntervalReadingImpl;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 */
public class ChannelDataInfo {
    @JsonProperty("interval")
    public IntervalInfo interval;
    @JsonProperty("readingTime")
    public Instant readingTime;
    @JsonProperty("intervalFlags")
    public List<String> intervalFlags;
    @JsonProperty("value")
    @XmlJavaTypeAdapter(BigDecimalAsStringAdapter.class)
    public BigDecimal value;
    @XmlJavaTypeAdapter(BigDecimalAsStringAdapter.class)
    public BigDecimal collectedValue;

    @JsonProperty("validationStatus")
    public Boolean validationStatus;

    @JsonProperty("dataValidated")
    public Boolean dataValidated;

    @JsonProperty("mainValidationInfo")
    public MinimalVeeReadingValueInfo mainValidationInfo;

    @JsonProperty("bulkValidationInfo")
    public MinimalVeeReadingValueInfo bulkValidationInfo;

    public BaseReading createNew() {
        return IntervalReadingImpl.of(Instant.ofEpochMilli(this.interval.end), this.value);
    }

    public BaseReading createNewBulk() {
        return IntervalReadingImpl.of(Instant.ofEpochMilli(this.interval.end), this.collectedValue);
    }

    public BaseReading createConfirm() {
        return IntervalReadingImpl.of(Instant.ofEpochMilli(this.interval.end), null);
    }
    
}
