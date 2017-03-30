/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.metering.CimChannel;
import com.elster.jupiter.metering.ReadingQualityType;

import java.math.BigDecimal;
import java.time.Instant;

enum BackflowMarker implements BackflowListener {

    INSTANT;

    @Override
    public void backflowOccurred(QualityCodeSystem system, CimChannel cimChannel, Instant timestamp, BigDecimal value, BigDecimal overflowValue) {
        ReadingQualityType backflowQuality = ReadingQualityType.of(system, QualityCodeIndex.REVERSEROTATION);
        cimChannel.createReadingQuality(backflowQuality, timestamp);
    }
}
