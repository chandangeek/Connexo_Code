package com.energyict.mdc.device.alarms.rest.request;

import com.elster.jupiter.issue.rest.request.EntityReference;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BulkDeviceAlarmRequest {
    public boolean allAlarms = false;
    public List<EntityReference> alarms;
    public String comment;
}

