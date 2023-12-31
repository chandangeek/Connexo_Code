package com.elster.jupiter.usagepoint.lifecycle.rest;
/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

import com.elster.jupiter.properties.rest.PropertyInfo;
import com.elster.jupiter.usagepoint.lifecycle.rest.impl.UsagePointStateChangeRequestInfo;

import java.time.Instant;
import java.util.List;

public class UsagePointTransitionInfo {
    public long id;
    public String name;
    public Instant effectiveTimestamp;
    public boolean transitionNow = true;
    public List<PropertyInfo> properties;
    public UsagePointStateChangeRequestInfo.UsagePointInfo usagePoint;
}