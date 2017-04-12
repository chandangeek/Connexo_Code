/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.properties.rest.PropertyInfo;

import java.time.Instant;
import java.util.List;

public class SecurityAccessorInfo {
    public long id;
    public String name;
    public String description;
    public boolean swapped;
    public long version;
    public Instant expirationTime;
    public Instant lastReadDate;
    public List<PropertyInfo> currentProperties;
    public List<PropertyInfo> tempProperties;
}
