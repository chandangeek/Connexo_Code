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
 * Created by bvn on 8/1/14.
 */
public class ChannelDataInfo {
    @JsonProperty("interval")
    public IntervalInfo interval;
    @JsonProperty("readingTime")
    public Instant readingTime;
//    @JsonProperty("reportedDateTime")
//    public Instant reportedDateTime;
    @JsonProperty("intervalFlags")
    public List<String> intervalFlags;
    @JsonProperty("value")
    @XmlJavaTypeAdapter(BigDecimalAsStringAdapter.class)
    public BigDecimal value;
//    @XmlJavaTypeAdapter(BigDecimalAsStringAdapter.class)
//    public BigDecimal collectedValue;
//    public boolean isBulk;
    public String deviceMRID;

//    @JsonProperty("validationStatus")
//    public Boolean validationStatus;

//    @JsonProperty("validationInfo")
//    public VeeReadingInfo validationInfo;

    public BaseReading createNew() {
        return IntervalReadingImpl.of(Instant.ofEpochMilli(this.interval.end), this.value);
    }

//    public BaseReading createNewBulk() {
//        return IntervalReadingImpl.of(Instant.ofEpochMilli(this.interval.end), this.collectedValue);
//    }

    public BaseReading createConfirm() {
        return IntervalReadingImpl.of(Instant.ofEpochMilli(this.interval.end), null);
    }
}
