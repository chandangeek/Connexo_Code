/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.rest.impl;

import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.util.units.Quantity;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ReadingInfo {

    public long timeStamp;
    public Long recordTime;
    public List<BigDecimal> values;

    public ReadingInfo(BaseReadingRecord reading) {
        this.timeStamp = reading.getTimeStamp().toEpochMilli();
        this.recordTime = reading.getReportedDateTime() == null ? null : reading.getReportedDateTime().toEpochMilli();
        List<Quantity> quantities = reading.getQuantities();
        values = quantities
                .stream()
                .filter(Objects::nonNull)
                .map(Quantity::getValue)
                .collect(Collectors.toList());
    }

}