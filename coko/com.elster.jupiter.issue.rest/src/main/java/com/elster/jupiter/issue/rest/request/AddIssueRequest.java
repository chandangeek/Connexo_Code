/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.rest.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties(ignoreUnknown = true)
public class AddIssueRequest {

    public String reasonId;
    public String statusId;
    public String priority;
    public String deviceName;
    public long usagePointId;
    public String comment;
    public DueInInfo dueDate;
    public long assignToUserId;
    public long assignToWorkgroupId;
    public String assignComment;

}
