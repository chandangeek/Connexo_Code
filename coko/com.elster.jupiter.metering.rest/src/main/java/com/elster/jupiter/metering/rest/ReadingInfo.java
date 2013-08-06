package com.elster.jupiter.metering.rest;

import com.elster.jupiter.metering.BaseReading;

import java.math.BigDecimal;
import java.util.List;

public class ReadingInfo {

    public long timeStamp;
    public Long recordTime;
    public List<BigDecimal> values;

    public ReadingInfo(BaseReading reading) {
        this.timeStamp = reading.getTimeStamp().getTime();
        this.recordTime = reading.getReportedDateTime() == null ? null : reading.getReportedDateTime().getTime();
        values = reading.getValues();
    }
}
