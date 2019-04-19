/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.rest.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties(ignoreUnknown = true)
public class AddIssueRequest {

    private String reasonId;
    private String statusId;
    private String priority;
    private String deviceMrid;
    private String usagePointMrid;
    private String comment;
    private String title;
    private long dueDate;
    private boolean overdue;

    public String getReasonId() {
        return reasonId;
    }

    public void setReasonId(String reasonId) {
        this.reasonId = reasonId;
    }

    public String getStatusId() {
        return statusId;
    }

    public void setStatusId(String statusId) {
        this.statusId = statusId;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getDeviceMrid() {
        return deviceMrid;
    }

    public void setDeviceMrid(String deviceMrid) {
        this.deviceMrid = deviceMrid;
    }

    public String getUsagePointMrid() {
        return usagePointMrid;
    }

    public void setUsagePointMrid(String usagePointMrid) {
        this.usagePointMrid = usagePointMrid;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getDueDate() {
        return dueDate;
    }

    public void setDueDate(long dueDate) {
        this.dueDate = dueDate;
    }

    public boolean isOverdue() {
        return overdue;
    }

    public void setOverdue(boolean overdue) {
        this.overdue = overdue;
    }
}
