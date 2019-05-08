/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.impl;

import com.elster.jupiter.issue.impl.records.OpenIssueImpl;
import com.elster.jupiter.issue.share.Priority;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.issue.share.service.ManualIssueBuilder;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.users.User;

import java.time.Instant;
import java.util.Optional;

public class ManualIssueBuilderImpl implements ManualIssueBuilder {

    private final DataModel dataModel;
    private final User user;
    private IssueReason reason;
    private Priority priority;
    private EndDevice device;
    private UsagePoint usagePoint;
    private String comment;
    private Instant dueDate;
    private IssueStatus status;
    private boolean overdue;
    private IssueType type;


    public ManualIssueBuilderImpl(User user, DataModel dataModel) {
        this.user = user;
        this.dataModel = dataModel;
    }

    @Override
    public ManualIssueBuilder withReason(IssueReason reason) {
        this.reason = reason;
        return this;
    }

    @Override
    public ManualIssueBuilder withPriority(Priority priority) {
        this.priority = priority;
        return this;
    }

    @Override
    public ManualIssueBuilder withDevice(EndDevice device) {
        this.device = device;
        return this;
    }

    @Override
    public ManualIssueBuilder withUsagePoint(UsagePoint usagePoint) {
        this.usagePoint = usagePoint;
        return this;
    }

    @Override
    public ManualIssueBuilder withComment(String comment) {
        this.comment = comment;
        return this;
    }

    @Override
    public ManualIssueBuilder withDueDate(Instant dueDate) {
        this.dueDate = dueDate;
        return this;
    }

    @Override
    public ManualIssueBuilder withStatus(IssueStatus status) {
        this.status = status;
        return this;
    }

    @Override
    public ManualIssueBuilder withOverdue(boolean overdue) {
        this.overdue = overdue;
        return this;
    }

    @Override
    public ManualIssueBuilder withType(IssueType type) {
        this.type = type;
        return this;
    }

    @Override
    public Issue create() {
        OpenIssueImpl issue = dataModel.getInstance(OpenIssueImpl.class);
        Optional.ofNullable(reason).ifPresent(issue::setReason);
        Optional.ofNullable(status).ifPresent(issue::setStatus);
        Optional.ofNullable(priority).ifPresent(issue::setPriority);
        Optional.ofNullable(dueDate).ifPresent(issue::setDueDate);
        Optional.ofNullable(device).ifPresent(issue::setDevice);
        Optional.ofNullable(usagePoint).ifPresent(issue::setUsagePoint);
        Optional.ofNullable(type).ifPresent(issue::setType);
        issue.setOverdue(overdue);
        issue.save();
        Optional.ofNullable(comment).ifPresent(comm -> issue.addComment(comm, user));
        return issue;
    }
}
