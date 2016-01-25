package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.CimChannel;

import java.math.BigDecimal;
import java.time.Instant;

public interface BackflowListener {

    void backflowOccurred(CimChannel cimChannel, Instant timestamp, BigDecimal value, BigDecimal overflowValue);
}
