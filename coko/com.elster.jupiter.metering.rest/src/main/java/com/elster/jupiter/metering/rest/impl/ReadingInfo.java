package com.elster.jupiter.metering.rest.impl;

import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.util.units.Quantity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ReadingInfo {

    public long timeStamp;
    public Long recordTime;
    public List<BigDecimal> values;

    public ReadingInfo(BaseReadingRecord reading) {
        this.timeStamp = reading.getTimeStamp().getTime();
        this.recordTime = reading.getReportedDateTime() == null ? null : reading.getReportedDateTime().getTime();
        List<Quantity> quantities = reading.getQuantities();
        values = new ArrayList<>(quantities.size());
        for (Quantity quantity : quantities) {
        	if (quantity != null) {
        		values.add(quantity.getValue());
        	}
        }
    }
    
}
