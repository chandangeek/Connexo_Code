/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.properties.rest.PropertyInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BulkRequestInfo {
    public String action;
    public String filter;
    public List<Long> deviceIds;
    public List<Long> scheduleIds;
    public long newDeviceConfiguration;
    public long zoneId;
    public long zoneTypeId;
    public String strategy;
    public String loadProfileName;
    public long loadProfileLastReading;
    public String name;
    public String processId;
    public String version;
    public String deploymentId;
    public List<PropertyInfo> properties = new ArrayList<>();
}
