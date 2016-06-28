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
    public void overflowOccurred(CimChannel cimChannel, Instant timestamp, BigDecimal value, BigDecimal overflowValue) {
        // TODO: refactor in scope of estimation/confirm/edit/import refactoring (CXO-1443/CXO-1447/CXO-1449)
        ReadingQualityType backflowQuality = ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeIndex.OVERFLOWCONDITIONDETECTED);
        cimChannel.createReadingQuality(backflowQuality, timestamp);
    }
}
