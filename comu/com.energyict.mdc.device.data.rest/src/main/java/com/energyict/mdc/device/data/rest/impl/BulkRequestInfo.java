/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BulkRequestInfo {
    public String action;
    public String filter;
    public List<Long> deviceIds;
    public List<Long> scheduleIds;
    public long newDeviceConfiguration;
    public String strategy;

    String name;
    String processId;
    String version;
    String deploymentId;
    Map<String,Object> properties;

}
