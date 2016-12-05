package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.properties.rest.PropertyInfo;
import com.elster.jupiter.rest.api.util.v1.hypermedia.LinkInfo;

import java.time.Instant;
import java.util.Collection;

public class LifeCycleActionInfo extends LinkInfo<Long> {
    public String name;
    public Instant effectiveTimestamp;
    public Collection<PropertyInfo> properties;
    public LinkInfo device;
}