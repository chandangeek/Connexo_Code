/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest;

import com.elster.jupiter.properties.rest.PropertyInfo;

import com.energyict.mdc.device.configuration.rest.impl.KeyTypeInfo;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@ProviderType
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
    public boolean editable = true;
    public boolean serviceKey = false;

    public String defaultServiceKey;
    public String keyType;
}
