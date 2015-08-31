package com.elster.insight.common.rest;

import com.elster.jupiter.time.TimeDuration;
import java.util.Optional;

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

    public static TimeDurationInfo of(TimeDuration timeDurationOrNull) {
        if (timeDurationOrNull == null) {
            return null;
        }
        else {
            return new TimeDurationInfo(timeDurationOrNull);
        }
    }

    public static TimeDurationInfo of(Optional<TimeDuration> timeDurationOrNull) {
        if (!timeDurationOrNull.isPresent()) {
            return null;
        }
        else {
            return new TimeDurationInfo(timeDurationOrNull.get());
        }
    }

    public TimeDuration asTimeDuration() {
        return new TimeDuration(this.count+" "+this.timeUnit);
    }

}
