/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.metering.impl;

import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.obis.ObisCode;

enum MeasuringPeriodMapping {

    ONE_MINUTE(TimeAttribute.MINUTE1, 1),
    TWO_MINUTES(TimeAttribute.MINUTE2, 2),
    THREE_MINUTES(TimeAttribute.MINUTE3, 3),
    FIVE_MINUTES(TimeAttribute.MINUTE5, 5),
    TEN_MINUTES(TimeAttribute.MINUTE10, 10),
    FIFTEEN_MINUTES(TimeAttribute.MINUTE15, 15),
    TWENTY_MINUTES(TimeAttribute.MINUTE20, 20),
    THIRTY_MINUTES(TimeAttribute.MINUTE30, 30),
    SIXTY_MINUTES(TimeAttribute.MINUTE60, 60),
    TWO_HOURS(TimeAttribute.HOUR2, 60 * 2),
    THREE_HOURS(TimeAttribute.HOUR3, 60 * 3),
    FOUR_HOURS(TimeAttribute.HOUR4, 60 * 4),
    SIX_HOURS(TimeAttribute.HOUR6, 60 * 6),
    TWELVE_HOURS(TimeAttribute.HOUR12, 60 * 12),
    TWENTY_FOUR_HOURS(TimeAttribute.HOUR24, 60 * 24);

    private final TimeAttribute timeAttribute;
    private final int minutes;

    MeasuringPeriodMapping(TimeAttribute timeAttribute, int minutes) {
        this.timeAttribute = timeAttribute;
        this.minutes = minutes;
    }

    public static TimeAttribute getMeasuringPeriodFor(ObisCode obisCode, TimeDuration timeDuration) {
        if (obisCode != null && timeDuration != null) {
            for (MeasuringPeriodMapping mpm : values()) {
                if (mpm.minutes == getMinutes(timeDuration)) {
                    if (obisCode.getE() == 0 || mpm.minutes != 60 * 24) {
                        return mpm.timeAttribute;
                    }
                }
            }
        }
        return TimeAttribute.NOTAPPLICABLE;
    }

    private static int getMinutes(TimeDuration timeDuration) {
        return timeDuration.getSeconds() / 60;
    }

    TimeAttribute getTimeAttribute() {
        return timeAttribute;
    }

    int getMinutes() {
        return minutes;
    }
}
