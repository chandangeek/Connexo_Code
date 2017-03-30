/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.rest;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.time.TimeDuration;

import java.util.Optional;

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
        this.count=timeDuration.getCount();
        this.timeUnit=TimeDuration.getTimeUnitDescription(timeDuration.getTimeUnitCode());
        this.localizedTimeUnit=TimeDuration.getTimeUnitDescription(timeDuration.getTimeUnitCode());
        this.asSeconds = timeDuration.getSeconds();
    }

    public TimeDurationInfo(TimeDuration timeDuration, Thesaurus thesaurus) {
        this.count=timeDuration.getCount();
        this.timeUnit=TimeDuration.getTimeUnitDescription(timeDuration.getTimeUnitCode());
        this.localizedTimeUnit=thesaurus.getString(TimeDuration.getTimeUnitDescription(timeDuration.getTimeUnitCode()),TimeDuration.getTimeUnitDescription(timeDuration.getTimeUnitCode()));
        this.asSeconds = timeDuration.getSeconds();
    }

    public TimeDurationInfo(long seconds) {
        this.count=seconds;
        this.timeUnit=TimeDuration.TimeUnit.SECONDS.getDescription();
        this.localizedTimeUnit=TimeDuration.TimeUnit.SECONDS.getDescription();
        this.asSeconds = (int)seconds;
    }

    public static TimeDurationInfo of(TimeDuration timeDurationOrNull) {
        if (timeDurationOrNull == null) {
            return null;
        } else {
            return new TimeDurationInfo(timeDurationOrNull);
        }
    }

    public static TimeDurationInfo of(Optional<TimeDuration> timeDurationOrNull) {
        if (!timeDurationOrNull.isPresent()) {
            return null;
        } else {
            return new TimeDurationInfo(timeDurationOrNull.get());
        }
    }

    public TimeDuration asTimeDuration() {
        return new TimeDuration(this.count+" "+this.timeUnit);
    }

}
