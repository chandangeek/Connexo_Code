package com.elster.jupiter.metering.impl;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.metering.CimChannel;

import java.math.BigDecimal;
import java.time.Instant;

public interface BackflowListener {

    void backflowOccurred(QualityCodeSystem system, CimChannel cimChannel, Instant timestamp,
                          BigDecimal value, BigDecimal overflowValue);
}
