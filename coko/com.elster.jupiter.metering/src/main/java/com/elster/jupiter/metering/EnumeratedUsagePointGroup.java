package com.elster.jupiter.metering;

import com.elster.jupiter.util.time.Interval;

import java.util.Date;

public interface EnumeratedUsagePointGroup extends UsagePointGroup {

    void setName(String name);

    void setMRID(String mrid);

    void setDescription(String description);

    void setAliasName(String aliasName);

    void setType(String type);

    void endMembership(UsagePoint usagePoint, Date now);

    interface Entry {
        UsagePoint getUsagePoint();

        Interval getInterval();
    }

    Entry add(UsagePoint usagePoint, Interval interval);

    void remove(Entry entry);

    void save();
}
