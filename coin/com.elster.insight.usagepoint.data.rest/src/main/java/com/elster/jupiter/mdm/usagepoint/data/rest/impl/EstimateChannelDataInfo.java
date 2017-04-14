/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.metering.rest.ReadingTypeInfo;
import com.elster.jupiter.properties.rest.PropertyInfo;
import com.elster.jupiter.rest.util.IntervalInfo;

import java.util.List;

public class EstimateChannelDataInfo {

    public String estimatorImpl;

    public List<PropertyInfo> properties;

    public List<IntervalInfo> intervals;

    public ReadingTypeInfo readingType;
}
