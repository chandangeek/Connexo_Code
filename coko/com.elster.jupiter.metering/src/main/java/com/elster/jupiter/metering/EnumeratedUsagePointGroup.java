package com.elster.jupiter.metering;

import com.elster.jupiter.util.time.Interval;

import java.util.Date;

public interface EnumeratedUsagePointGroup extends UsagePointGroup {

    void endMembership(UsagePoint usagePoint, Date now);

    interface Entry {
        UsagePoint getUsagePoint();

        Interval getInterval();
    }

    Entry add(UsagePoint usagePoint, Interval interval);

    void remove(Entry entry);

}
