/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.metering.rest.ReadingTypeInfo;
import com.elster.jupiter.properties.rest.PropertyInfo;
import com.elster.jupiter.rest.util.IntervalInfo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown=true)
public class EstimateChannelDataInfo {

    public String estimatorImpl;

    public List<PropertyInfo> properties;

    public List<IntervalInfo> intervals;

    public ReadingTypeInfo readingType;

    @JsonProperty("commentId")
    public long commentId;

    @JsonProperty("commentValue")
    public String commentValue;

    @JsonProperty("markAsProjected")
    public boolean markAsProjected;

    public List<OutputChannelDataInfo> editedReadings;
}
