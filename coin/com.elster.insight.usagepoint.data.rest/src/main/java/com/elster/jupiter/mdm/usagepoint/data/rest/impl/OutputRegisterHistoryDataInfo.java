/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.rest.util.IntervalInfo;
import com.elster.jupiter.validation.ValidationAction;

import java.math.BigDecimal;

public class OutputRegisterHistoryDataInfo extends OutputRegisterDataInfo {

    public IntervalInfo interval;
    public String userName;
    public BigDecimal value;
    public boolean isConfirmed;
    public ValidationAction validationAction;

    @SuppressWarnings("unused")
    @Override
    public BaseReading createNew(ReadingType readingType) {
        return null;
    }
}
