package com.elster.jupiter.metering.groups.impl;

import java.util.Map;

import com.elster.jupiter.metering.groups.EnumeratedUsagePointGroup;
import com.elster.jupiter.metering.groups.QueryUsagePointGroup;
import com.elster.jupiter.metering.groups.UsagePointGroup;
import com.google.common.collect.ImmutableMap;

public abstract class AbstractUsagePointGroup extends AbstractGroup implements UsagePointGroup {
    // ORM inheritance map
    static final Map<String, Class<? extends UsagePointGroup>> IMPLEMENTERS = ImmutableMap.<String, Class<? extends UsagePointGroup>>of(QueryUsagePointGroup.TYPE_IDENTIFIER, QueryUsagePointGroupImpl.class, EnumeratedUsagePointGroup.TYPE_IDENTIFIER, EnumeratedUsagePointGroupImpl.class);
    
    public boolean isDynamic() {
        return this instanceof QueryUsagePointGroup;
    }
}
