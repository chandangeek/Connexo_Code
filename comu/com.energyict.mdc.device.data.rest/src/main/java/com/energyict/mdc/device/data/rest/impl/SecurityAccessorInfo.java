/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.properties.rest.PropertyInfo;
import com.energyict.mdc.device.configuration.rest.ExecutionLevelInfo;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class SecurityAccessorInfo {
    public Long id;
    public String name;
    public String description;
    public Boolean swapped;
    public Long version;
    public Instant expirationTime;
    public Instant lastReadDate;
    public Instant modificationDate;
    public String status;
    public Boolean canGeneratePassiveKey;
    public Boolean hasTempValue;
    public Boolean hasActualValue;
    public List<PropertyInfo> currentProperties;
    public List<PropertyInfo> tempProperties;
    public List<ExecutionLevelInfo> editLevels = new ArrayList<>();
    public List<ExecutionLevelInfo> viewLevels = new ArrayList<>();
}
