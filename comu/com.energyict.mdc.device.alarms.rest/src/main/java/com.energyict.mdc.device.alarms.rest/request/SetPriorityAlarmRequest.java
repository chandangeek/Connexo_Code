package com.energyict.mdc.device.alarms.rest.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SetPriorityAlarmRequest  extends BulkDeviceAlarmRequest {
    public String priority;
}
