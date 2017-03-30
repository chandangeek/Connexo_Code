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

public enum OverflowMarker implements OverflowListener {

    INSTANT;

    @Override
    public void overflowOccurred(QualityCodeSystem system, CimChannel cimChannel, Instant timestamp, BigDecimal value, BigDecimal overflowValue) {
        ReadingQualityType backflowQuality = ReadingQualityType.of(system, QualityCodeIndex.OVERFLOWCONDITIONDETECTED);
        cimChannel.createReadingQuality(backflowQuality, timestamp);
    }
}
