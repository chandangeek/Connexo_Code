/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.rest.request;

import com.elster.jupiter.issue.share.Priority;
import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.UsagePoint;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AddIssueRequest {

    private IssueReason reason;
    private Priority priority;
    private EndDevice device;
    private UsagePoint usagePoint;
    private String comment;
    private String title;
    private Instant dueDate;

    public IssueReason getReason() {
        return reason;
    }

    public void setReason(IssueReason reason) {
        this.reason = reason;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public EndDevice getDevice() {
        return device;
    }

    public void setDevice(EndDevice device) {
        this.device = device;
    }

    public UsagePoint getUsagePoint() {
        return usagePoint;
    }

    public void setUsagePoint(UsagePoint usagePoint) {
        this.usagePoint = usagePoint;
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

    public Instant getDueDate() {
        return dueDate;
    }

    public void setDueDate(Instant dueDate) {
        this.dueDate = dueDate;
    }
}
