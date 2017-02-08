/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.alarms.rest.request;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AssignSingleDeviceAlarmRequest extends SingleDeviceAlarmRequest {
    public AssigneeReference assignee;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AssigneeReference {
        public long userId;
        public long workGroupId;

    }
}
