package com.elster.jupiter.metering.groups.impl;

import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.EnumeratedEndDeviceGroup;
import com.elster.jupiter.metering.groups.QueryEndDeviceGroup;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

public abstract class AbstractEndDeviceGroup extends AbstractGroup implements EndDeviceGroup {
    // ORM inheritance map
    static final Map<String, Class<? extends EndDeviceGroup>> IMPLEMENTERS = ImmutableMap.<String, Class<? extends EndDeviceGroup>>of(QueryEndDeviceGroup.TYPE_IDENTIFIER, QueryEndDeviceGroupImpl.class, EnumeratedEndDeviceGroup.TYPE_IDENTIFIER, EnumeratedEndDeviceGroupImpl.class);

}
