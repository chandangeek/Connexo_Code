package com.energyict.mdc.device.data.rest.impl;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BulkRequestInfo {
    public List<String> deviceMRIDs;
    public List<Long> scheduleIds;
}
