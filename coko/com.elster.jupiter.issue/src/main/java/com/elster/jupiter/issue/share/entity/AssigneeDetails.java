/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.share.entity;

public class AssigneeDetails {
    private long assigneeId;
    private String assigneeType;

    public AssigneeDetails(long assigneeId, String assigneeType) {
        this.assigneeId = assigneeId;
        this.assigneeType = assigneeType;
    }

    public String getAssigneeType() {
        return assigneeType;
    }

    public void setAssigneeType(String assigneeType) {
        this.assigneeType = assigneeType;
    }

    public long getAssigneeId() {
        return assigneeId;
    }

    public void setAssigneeId(long assigneeId) {
        this.assigneeId = assigneeId;
    }
}
