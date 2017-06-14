/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.metering.ReadingType;

public enum ChannelPeriodType {

    INTERVAL("interval"),
    MONTHLY("monthly"),
    YEARLY("yearly"),
    OTHER("other");

    private String id;

    ChannelPeriodType(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }

    public static ChannelPeriodType of(ReadingType readingType) {
        if (!readingType.getMeasuringPeriod().equals(TimeAttribute.NOTAPPLICABLE)) {
            return INTERVAL;
        } else {
            MacroPeriod macroPeriod = readingType.getMacroPeriod();
            if (macroPeriod.equals(MacroPeriod.MONTHLY)) {
                return MONTHLY;
            } else if (macroPeriod.equals(MacroPeriod.YEARLY)) {
                return YEARLY;
            }
        }
        return OTHER;
    }
}
