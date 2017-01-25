package com.energyict.mdc.device.alarms.rest.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CloseDeviceAlarmRequest extends BulkDeviceAlarmRequest {
    public String status;
}
