/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.rest.impl;

import com.elster.jupiter.time.TimeDuration;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class TimeDurationInfo {
    public long count;
    public String timeUnit = TimeDuration.TimeUnit.SECONDS.getDescription();

    public TimeDurationInfo() {
    }

    public TimeDurationInfo(String bogus) {
        // Constructor to allow ExtJS empty string TimeDurations
    }

    public TimeDurationInfo(TimeDuration timeDuration) {
        this.count=timeDuration.getCount();
        this.timeUnit=TimeDuration.getTimeUnitDescription(timeDuration.getTimeUnitCode());
    }

    public TimeDurationInfo(long seconds) {
        this.count=seconds;
        this.timeUnit=TimeDuration.TimeUnit.SECONDS.getDescription();
    }

    public TimeDuration asTimeDuration() {
        return new TimeDuration(this.count+" "+this.timeUnit);
    }

}
