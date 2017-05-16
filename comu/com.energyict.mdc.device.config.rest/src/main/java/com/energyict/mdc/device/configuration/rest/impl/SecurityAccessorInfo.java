/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.rest.util.VersionInfo;
import com.elster.jupiter.time.rest.TimeDurationInfo;
import com.energyict.mdc.device.configuration.rest.ExecutionLevelInfo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SecurityAccessorInfo {
    public long id;
    public String name;
    public String description;
    public KeyTypeInfo keyType;
    public String storageMethod;
    public long trustStoreId;
    public TimeDurationInfo duration;
    public VersionInfo<String> parent;
    public List<ExecutionLevelInfo> editLevels = new ArrayList<>();
    public List<ExecutionLevelInfo> defaultEditLevels = new ArrayList<>();
    public List<ExecutionLevelInfo> viewLevels = new ArrayList<>();
    public List<ExecutionLevelInfo> defaultViewLevels = new ArrayList<>();
}
