package com.energyict.mdc.device.data.rest.impl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BulkRequestInfo {
    public String action;
    public String filter;
    public List<Long> deviceIds;
    public List<Long> scheduleIds;
    public long newDeviceConfiguration;
    public String strategy;
}
