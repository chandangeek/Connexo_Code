package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.rest.util.properties.PropertyInfo;
import java.time.Instant;
import java.util.List;

public class DeviceLifeCycleActionInfo extends LinkInfo {
    public String name;
    public Instant effectiveTimestamp;
    public List<PropertyInfo> properties;
    public long deviceVersion;
}