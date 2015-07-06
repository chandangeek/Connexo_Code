package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.rest.util.properties.PropertyInfo;
import java.time.Instant;
import java.util.List;

public class DeviceLifeCycleActionInfo {
    public long id;
    public String name;
    public Instant effectiveTimestamp;
    public boolean transitionNow = true;
    public List<PropertyInfo> properties;
}