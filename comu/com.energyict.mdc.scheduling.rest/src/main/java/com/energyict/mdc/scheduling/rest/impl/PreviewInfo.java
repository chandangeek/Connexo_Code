/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.scheduling.rest.impl;

import com.energyict.mdc.scheduling.rest.TemporalExpressionInfo;

import java.util.Date;
import java.util.List;

/**
 * Simple struct to contain the information required to create a preview
 */
public class PreviewInfo {
    public TemporalExpressionInfo temporalExpression;
    public Date startDate;

    public List<Date> nextOccurrences;
}
