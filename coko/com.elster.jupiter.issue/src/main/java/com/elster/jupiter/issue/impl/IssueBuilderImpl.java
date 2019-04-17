/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.impl;

import com.elster.jupiter.issue.impl.records.OpenIssueImpl;
import com.elster.jupiter.issue.share.Priority;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.issue.share.service.IssueBuilder;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.users.User;

import java.time.Instant;
import java.util.Optional;

public class IssueBuilderImpl implements IssueBuilder {

    private final DataModel dataModel;
    private final User user;
    private IssueReason reason;
    private Priority priority;
    private EndDevice device;
    private UsagePoint usagePoint;
    private String comment;
    private Instant dueDate;

    public IssueBuilderImpl(User user, DataModel dataModel) {
        this.user = user;
        this.dataModel = dataModel;
    }

    @Override
    public IssueBuilder withReason(IssueReason reason) {
        this.reason = reason;
        return this;
    }

    @Override
    public IssueBuilder withPriority(Priority priority) {
        this.priority = priority;
        return this;
    }

    @Override
    public IssueBuilder withDevice(EndDevice device) {
        this.device = device;
        return this;
    }

    @Override
    public IssueBuilder withUsagePoint(UsagePoint usagePoint) {
        this.usagePoint = usagePoint;
        return this;
    }

    @Override
    public IssueBuilder withComment(String comment) {
        this.comment = comment;
        return this;
    }

    @Override
    public IssueBuilder withDueDate(Instant dueDate) {
        this.dueDate = dueDate;
        return this;
    }

    @Override
    public Issue create() {
        OpenIssueImpl issue = dataModel.getInstance(OpenIssueImpl.class);
        Optional.ofNullable(reason).ifPresent(issue::setReason);
        Optional.ofNullable(priority).ifPresent(issue::setPriority);
        Optional.ofNullable(dueDate).ifPresent(issue::setDueDate);
        Optional.ofNullable(device).ifPresent(issue::setDevice);
        Optional.ofNullable(usagePoint).ifPresent(issue::setUsagePoint);
        Optional.ofNullable(comment).ifPresent(comm -> issue.addComment(comm, user));
        issue.save();
        return issue;
    }
}
