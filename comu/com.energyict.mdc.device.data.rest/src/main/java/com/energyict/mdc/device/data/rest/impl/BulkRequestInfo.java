package com.energyict.mdc.device.data.rest.impl;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BulkRequestInfo {
    public List<String> deviceMRIDs;
    public List<Long> scheduleIds;
}
