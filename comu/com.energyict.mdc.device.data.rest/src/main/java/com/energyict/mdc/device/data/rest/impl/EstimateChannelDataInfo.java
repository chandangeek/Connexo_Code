/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.metering.rest.ReadingTypeInfo;
import com.elster.jupiter.properties.rest.PropertyInfo;
import com.energyict.mdc.common.rest.IntervalInfo;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class EstimateChannelDataInfo {
    @JsonProperty("estimatorImpl")
    public String estimatorImpl;

    @JsonProperty("properties")
    public List<PropertyInfo> properties;

    @JsonProperty("intervals")
    public List<IntervalInfo> intervals;

    @JsonProperty("estimateBulk")
    public boolean estimateBulk;

    @JsonProperty("readingType")
    public ReadingTypeInfo readingType;

    public EstimateChannelDataInfo() {

    }

}
