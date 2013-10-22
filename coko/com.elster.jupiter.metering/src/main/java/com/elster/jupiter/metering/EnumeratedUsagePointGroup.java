package com.elster.jupiter.metering;

import com.elster.jupiter.util.time.Interval;

public interface EnumeratedUsagePointGroup extends UsagePointGroup {

    interface Entry {
        UsagePoint getUsagePoint();

        Interval getInterval();
    }

    Entry add(UsagePoint usagePoint, Interval interval);

    void remove(Entry entry);
}
