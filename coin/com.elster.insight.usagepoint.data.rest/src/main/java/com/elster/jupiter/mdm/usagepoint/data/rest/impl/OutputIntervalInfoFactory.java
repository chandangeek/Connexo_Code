/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.time.TimeDuration;

import java.time.temporal.Temporal;
import java.time.temporal.TemporalAmount;

/**
 * Created by aeryomin on 11.05.2017.
 */
public class OutputIntervalInfoFactory {

    public OutputIntervalInfoFactory() {
    }

    public OutputIntervalInfo asIntervalInfo(ReadingType readingType) {
        MacroPeriod macroPeriod = readingType.getMacroPeriod();
        TimeAttribute measuringPeriod = readingType.getMeasuringPeriod();
        TimeDuration timeDuration = null;
        switch (readingType.getMacroPeriod()) {
            case DAILY:
                timeDuration = TimeDuration.days(1);
                return new OutputIntervalInfo(macroPeriod.getId(), macroPeriod.getDescription(), timeDuration);
            case MONTHLY:
                timeDuration = TimeDuration.months(1);
                return new OutputIntervalInfo(macroPeriod.getId(), macroPeriod.getDescription(), timeDuration);
            case YEARLY:
                timeDuration = TimeDuration.years(1);
                return new OutputIntervalInfo(macroPeriod.getId(), macroPeriod.getDescription(), timeDuration);
            case WEEKLYS:
                timeDuration = TimeDuration.weeks(1);
                return new OutputIntervalInfo(macroPeriod.getId(), macroPeriod.getDescription(), timeDuration);
            default:
                timeDuration = TimeDuration.minutes(measuringPeriod.getMinutes());
                return new OutputIntervalInfo(measuringPeriod.getId(), measuringPeriod.getDescription(), timeDuration);
        }

    }
}
