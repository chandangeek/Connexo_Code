/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.commands.upload.time;

import com.elster.jupiter.demo.impl.UnableToCreate;
import com.elster.jupiter.metering.ReadingType;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

public class NoneIntervalReadingTimeProvider implements TimeProvider{
    @Override
    public Instant getTimeForReading(ReadingType readingType, Instant startDate, String controlValue) {
        int counter = 0;
        try {
            counter = Integer.parseInt(controlValue);
        } catch (NumberFormatException ex){
            throw new UnableToCreate("Incorrect control value for importing data. Should be a simple number");
        }
        ZonedDateTime local = ZonedDateTime.ofInstant(startDate, ZoneId.systemDefault());
        switch (readingType.getMacroPeriod()) {
            case MONTHLY:
                local = local.plus(1 * counter, ChronoUnit.MONTHS);
                break;
            case DAILY:
                local = local.plus(1 * counter, ChronoUnit.DAYS);
                break;
            default:
                throw new UnableToCreate("Unknown measurement period (Only daily and monthly are allowed). ");
        }
        return local.toInstant();
    }
}
