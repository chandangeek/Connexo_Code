package com.elster.jupiter.metering.groups;

import java.time.Instant;

import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.orm.associations.Effectivity;
import com.google.common.collect.Range;

public interface EnumeratedUsagePointGroup extends UsagePointGroup {

    String TYPE_IDENTIFIER = "EEG";

    void endMembership(UsagePoint usagePoint, Instant when);

    interface Entry extends Effectivity {
        UsagePoint getUsagePoint();
    }

    Entry add(UsagePoint usagePoint, Range<Instant> range);

    void remove(Entry entry);

}
