/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.properties.rest.PropertyInfo;

import java.time.Instant;
import java.util.List;

public class DeviceLifeCycleActionInfo {
    public long id;
    public String name;
    public Instant effectiveTimestamp;
    public boolean transitionNow = true;
    public List<PropertyInfo> properties;
    public DeviceInfo device;
}