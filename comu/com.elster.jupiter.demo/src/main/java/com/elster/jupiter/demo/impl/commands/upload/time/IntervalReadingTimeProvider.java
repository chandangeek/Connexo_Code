/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.commands.upload.time;

import com.elster.jupiter.demo.impl.UnableToCreate;
import com.elster.jupiter.metering.ReadingType;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class IntervalReadingTimeProvider implements TimeProvider {
    @Override
    public Instant getTimeForReading(ReadingType readingType, Instant startDate, String controlValue) {
        if (controlValue.length() != 9){
            throw new UnableToCreate("Incorrect control value for importing data. Should be 000-00:00");
        }
        startDate = startDate.plus(Integer.valueOf(controlValue.substring(0, 3)), ChronoUnit.DAYS);
        startDate = startDate.plus(Integer.valueOf(controlValue.substring(4, 6)), ChronoUnit.HOURS);
        return startDate.plus(Integer.valueOf(controlValue.substring(7, 9)), ChronoUnit.MINUTES);
    }
}
