package com.elster.jupiter.metering.groups;

import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.util.time.Interval;

import java.util.Date;

public interface EnumeratedUsagePointGroup extends UsagePointGroup {

    String TYPE_IDENTIFIER = "EUG";

    void endMembership(UsagePoint usagePoint, Date now);

    interface Entry {
        UsagePoint getUsagePoint();

        Interval getInterval();
    }

    Entry add(UsagePoint usagePoint, Interval interval);

    void remove(Entry entry);

}
