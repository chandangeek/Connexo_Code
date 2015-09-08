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
    public String deviceMRID;

}
