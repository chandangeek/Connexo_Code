/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.rest.impl;


import com.elster.jupiter.rest.util.IntervalInfo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown=true)
public class ReferenceChannelDataInfo {
    public String referenceUsagePoint;
    public long referencePurpose;
    public String readingType;
    public Instant startDate;
    public boolean allowSuspectData;
    public boolean completePeriod;
    public boolean projectedValue;
    public List<IntervalInfo> intervals;
    public List<OutputChannelDataInfo> editedReadings;
}
