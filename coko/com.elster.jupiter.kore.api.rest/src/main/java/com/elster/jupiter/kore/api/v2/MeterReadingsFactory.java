package com.elster.jupiter.kore.api.v2;

import java.math.BigDecimal;
import java.time.Instant;

public class MeterReadingsFactory {
    public MeterReadingInfo asInfo(String readingType, Instant timestamp, BigDecimal value) {
        MeterReadingInfo info = new MeterReadingInfo();
        info.readingType = readingType;
        info.timestamp = timestamp;
        info.value = value;
        return info;
    }
}
