/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimation.impl;

import com.elster.jupiter.estimation.Estimatable;

import java.math.BigDecimal;
import java.time.Instant;

class MissingReadingRecordEstimatable implements Estimatable {
    private final Instant timestamp;
    private BigDecimal estimation;

    MissingReadingRecordEstimatable(Instant timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public Instant getTimestamp() {
        return timestamp;
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
