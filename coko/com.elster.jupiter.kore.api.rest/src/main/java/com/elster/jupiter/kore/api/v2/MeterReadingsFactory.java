/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.kore.api.v2;

import java.math.BigDecimal;
import java.time.Instant;

public class MeterReadingsFactory {
    public NumericalMeterReadingInfo asInfo(String readingType, Instant timestamp, BigDecimal value) {
        NumericalMeterReadingInfo info = new NumericalMeterReadingInfo();
        info.readingType = readingType;
        info.timestamp = timestamp;
        info.value = value;
        return info;
    }

    public TextMeterReadingInfo asInfo(String readingType, Instant timestamp, String value) {
        TextMeterReadingInfo info = new TextMeterReadingInfo();
        info.readingType = readingType;
        info.timestamp = timestamp;
        info.value = value;
        return info;
    }
}
