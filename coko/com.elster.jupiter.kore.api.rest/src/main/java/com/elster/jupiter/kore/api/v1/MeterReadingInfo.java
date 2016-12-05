package com.elster.jupiter.kore.api.v1;

import java.math.BigDecimal;
import java.time.Instant;

public class MeterReadingInfo {
    public String readingType;
    public Instant timestamp;
    public BigDecimal value;
}
