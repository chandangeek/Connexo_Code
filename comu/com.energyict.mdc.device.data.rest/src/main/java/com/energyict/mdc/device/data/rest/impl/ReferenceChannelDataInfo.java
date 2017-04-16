/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.metering.rest.ReadingTypeInfo;
import com.elster.jupiter.properties.rest.PropertyInfo;
import com.energyict.mdc.common.rest.IntervalInfo;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.List;

public class ReferenceChannelDataInfo {
    public String referenceDevice;
    public String readingType;
    public Instant startDate;
    public boolean allowSuspectData;
    public boolean completePeriod;
    public List<IntervalInfo> intervals;
}
