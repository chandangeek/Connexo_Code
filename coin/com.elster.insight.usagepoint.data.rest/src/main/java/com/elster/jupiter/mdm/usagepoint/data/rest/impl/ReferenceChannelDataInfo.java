/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.rest.impl;


import com.elster.jupiter.rest.util.IntervalInfo;

import java.time.Instant;
import java.util.List;

public class ReferenceChannelDataInfo {
    public String referenceUsagePoint;
    public long referencePurpose;
    public String readingType;
    public Instant startDate;
    public boolean allowSuspectData;
    public boolean completePeriod;
    public Long commentId;
    public String commentValue;
    public boolean projectedValue;
    public List<IntervalInfo> intervals;
}
