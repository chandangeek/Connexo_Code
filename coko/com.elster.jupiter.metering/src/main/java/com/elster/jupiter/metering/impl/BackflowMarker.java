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
        // TODO: refactor in scope of estimation/confirm/edit/import refactoring (CXO-1443/CXO-1447/CXO-1449)
        ReadingQualityType backflowQuality = ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeIndex.REVERSEROTATION);
        cimChannel.createReadingQuality(backflowQuality, timestamp);
    }
}
