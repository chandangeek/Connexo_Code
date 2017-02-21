package com.energyict.mdc.device.alarms.rest.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AssignDeviceAlarmRequest extends BulkDeviceAlarmRequest {
    public AssigneeReference assignee;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AssigneeReference {
        public long userId;
        public long workGroupId;
        public long id;
        public String type;

    }
}
