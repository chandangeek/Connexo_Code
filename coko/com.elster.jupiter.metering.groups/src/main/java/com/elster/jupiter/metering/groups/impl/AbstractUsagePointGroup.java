package com.elster.jupiter.metering.groups.impl;

import com.elster.jupiter.metering.groups.EnumeratedUsagePointGroup;
import com.elster.jupiter.metering.groups.QueryUsagePointGroup;
import com.elster.jupiter.metering.groups.UsagePointGroup;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

abstract class AbstractUsagePointGroup extends AbstractGroup implements UsagePointGroup {
    // ORM inheritance map
    static final Map<String, Class<? extends UsagePointGroup>> IMPLEMENTERS = ImmutableMap.of(QueryUsagePointGroup.TYPE_IDENTIFIER, QueryUsagePointGroupImpl.class, EnumeratedUsagePointGroup.TYPE_IDENTIFIER, EnumeratedUsagePointGroupImpl.class);

    public boolean isDynamic() {
        return false;
    }

}