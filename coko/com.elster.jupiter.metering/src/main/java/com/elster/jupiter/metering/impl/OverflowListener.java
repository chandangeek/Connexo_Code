package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.CimChannel;

import java.math.BigDecimal;
import java.time.Instant;

public interface OverflowListener {

    void overflowOccurred(CimChannel cimChannel, Instant timestamp, BigDecimal value, BigDecimal overflowValue);
}
