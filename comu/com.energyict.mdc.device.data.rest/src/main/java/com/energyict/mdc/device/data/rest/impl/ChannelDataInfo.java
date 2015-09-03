package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.metering.readings.beans.IntervalReadingImpl;
import com.energyict.mdc.common.rest.IntervalInfo;
import com.energyict.mdc.device.data.rest.BigDecimalAsStringAdapter;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

/**
 * Created by bvn on 8/1/14.
 */
public class ChannelDataInfo {
    @JsonProperty("interval")
    public IntervalInfo interval;
    @JsonProperty("readingTime")
    public Instant readingTime;
    @JsonProperty("reportedDateTime")
    public Instant reportedDateTime;
    @JsonProperty("intervalFlags")
    public List<String> intervalFlags;
    @JsonProperty("value")
    @XmlJavaTypeAdapter(BigDecimalAsStringAdapter.class)
    public BigDecimal value;
    @XmlJavaTypeAdapter(BigDecimalAsStringAdapter.class)
    public BigDecimal collectedValue;
    public boolean isBulk;

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
