/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.rest.request;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AssignIssueRequest extends BulkIssueRequest {
    public AssigneeReference assignee;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AssigneeReference {
        public long userId;
        public long workGroupId;
        public long id;
        public String type;

    }
}
