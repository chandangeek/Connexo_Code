/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.time.rest;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.time.TimeDuration;

public class TimeDurationInfo {
    public long count;
    public String timeUnit = TimeDuration.TimeUnit.SECONDS.getDescription();
    public String localizedTimeUnit = TimeDuration.TimeUnit.SECONDS.getDescription();
    public int asSeconds;

    public TimeDurationInfo() {
    }

    public TimeDurationInfo(String bogus) {
        // Constructor to allow ExtJS empty string TimeDurations
    }

    public TimeDurationInfo(TimeDuration timeDuration) {
        this.count = timeDuration.getCount();
        this.timeUnit = TimeDuration.getTimeUnitDescription(timeDuration.getTimeUnitCode());
        this.localizedTimeUnit = TimeDuration.getTimeUnitDescription(timeDuration.getTimeUnitCode());
        this.asSeconds = timeDuration.getSeconds();
    }

    public TimeDurationInfo(TimeDuration timeDuration, Thesaurus thesaurus) {
        this.count = timeDuration.getCount();
        this.timeUnit = TimeDuration.getTimeUnitDescription(timeDuration.getTimeUnitCode());
        this.localizedTimeUnit = TimeDuration.getLocalizedTimeUnitDescription(timeUnit, thesaurus);
        this.asSeconds = timeDuration.getSeconds();
    }

    public TimeDurationInfo(long seconds) {
        this.count = seconds;
        this.timeUnit = TimeDuration.TimeUnit.SECONDS.getDescription();
        this.localizedTimeUnit = TimeDuration.TimeUnit.SECONDS.getDescription();
        this.asSeconds = (int) seconds;
    }

    public static TimeDurationInfo of(TimeDuration timeDurationOrNull) {
        return timeDurationOrNull == null ? null : new TimeDurationInfo(timeDurationOrNull);
    }

    public TimeDuration asTimeDuration() {
        return new TimeDuration(this.count + " " + this.timeUnit);
    }

}
