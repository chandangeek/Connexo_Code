/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.alarms.rest.request;

import com.elster.jupiter.issue.rest.request.EntityReference;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SingleDeviceAlarmRequest {
    public EntityReference issue;
    public String comment;
}
