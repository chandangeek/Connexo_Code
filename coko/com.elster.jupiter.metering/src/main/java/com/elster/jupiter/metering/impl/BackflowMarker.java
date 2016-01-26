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
    public void backflowOccurred(CimChannel cimChannel, Instant timestamp, BigDecimal value, BigDecimal overflowValue) {
        ReadingQualityType backflowQuality = ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.REVERSEROTATION);
        cimChannel.createReadingQuality(backflowQuality, timestamp);
    }
}
