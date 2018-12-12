/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimation.impl;

import com.elster.jupiter.estimation.Estimatable;
import com.elster.jupiter.metering.BaseReadingRecord;

import java.math.BigDecimal;
import java.time.Instant;

class BaseReadingRecordEstimatable implements Estimatable {

    private final BaseReadingRecord baseReadingRecord;
    private BigDecimal estimation;

    BaseReadingRecordEstimatable(BaseReadingRecord baseReadingRecord) {
        this.baseReadingRecord = baseReadingRecord;
    }
    
    @Override
    public Instant getTimestamp() {
        return baseReadingRecord.getTimeStamp();
    }

    @Override
    public void setEstimation(BigDecimal value) {
        estimation = value;
    }

    @Override
    public BigDecimal getEstimation() {
        return estimation;
    }
}
