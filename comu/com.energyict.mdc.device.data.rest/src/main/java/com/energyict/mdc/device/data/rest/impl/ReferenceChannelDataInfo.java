/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.common.rest.IntervalInfo;

import java.time.Instant;
import java.util.List;

public class ReferenceChannelDataInfo {
    public String referenceDevice;
    public String readingType;
    public Instant startDate;
    public boolean allowSuspectData;
    public boolean completePeriod;
    public List<IntervalInfo> intervals;
    public Long commentId;
    public String commentValue;
}
