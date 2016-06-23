package com.elster.jupiter.metering.groups;

import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.orm.associations.Effectivity;

import aQute.bnd.annotation.ProviderType;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.List;

@ProviderType
public interface EnumeratedUsagePointGroup extends UsagePointGroup {

    String TYPE_IDENTIFIER = "EEG";

    void endMembership(UsagePoint usagePoint, Instant when);

    interface Entry extends Effectivity {
        UsagePoint getUsagePoint();
    }

    Entry add(UsagePoint usagePoint, Range<Instant> range);

    void remove(Entry entry);

    List<? extends Entry> getEntries();

}