package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.rest.util.properties.PropertyInfo;

import java.time.Instant;
import java.util.List;

public class DeviceLifeCycleActionInfo {
    public long id;
    public String name;
    public Instant effectiveTimestamp;
    public List<PropertyInfo> properties;
    public long deviceVersion;
}