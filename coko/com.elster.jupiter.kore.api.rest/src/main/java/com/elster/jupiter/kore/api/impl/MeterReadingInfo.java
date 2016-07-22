package com.elster.jupiter.kore.api.impl;

import com.elster.jupiter.metering.readings.BaseReading;

import java.math.BigDecimal;
import java.time.Instant;

public class MeterReadingInfo {
    public String readingType;
    public Instant timestamp;
    public BigDecimal value;


}
