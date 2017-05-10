/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.rest.util.IntervalInfo;
import com.elster.jupiter.validation.ValidationAction;
import com.elster.jupiter.validation.ValidationResult;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public class OutpitRegisterHistoryDataInfo {

    public IntervalInfo interval;
    public String userName;
    public List<ReadingQualityInfo> readingQualities;
    public Instant reportedDateTime;
    public Instant timeStamp;
    public BigDecimal value;
    public ValidationResult validationResult;
    public boolean dataValidated;
    public ValidationAction validationAction;
}
