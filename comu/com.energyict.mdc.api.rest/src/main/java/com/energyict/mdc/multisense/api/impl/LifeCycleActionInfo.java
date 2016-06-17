package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.rest.util.hypermedia.LinkInfo;
import com.elster.jupiter.rest.util.properties.PropertyInfo;

import java.time.Instant;
import java.util.Collection;

public class LifeCycleActionInfo extends LinkInfo<Long> {
    public String name;
    public Instant effectiveTimestamp;
    public Collection<PropertyInfo> properties;
    public LinkInfo device;
}