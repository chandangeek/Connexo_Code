/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.insight.issue.datavalidation;

import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.users.User;

import aQute.bnd.annotation.ProviderType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@ProviderType
public class UsagePointDataValidationIssueFilter {

    private List<IssueStatus> statuses = new ArrayList<>();

    private boolean unassignedOnly = false;
    
    private Optional<User> assignee = Optional.empty();

    private Optional<IssueReason> issueReason = Optional.empty();

    private Optional<UsagePoint> usagePoint = Optional.empty();

    public void setUnassignedOnly() {
        this.unassignedOnly = true;
    }
    
    public void setAssignee(User assignee) {
        this.assignee = Optional.of(assignee);
    }

    public void setUsagePoint(UsagePoint usagePoint) {
        this.usagePoint = Optional.of(usagePoint);
    }

    public void setIssueReason(IssueReason issueReason) {
        this.issueReason = Optional.of(issueReason);
    }

    public void addStatus(IssueStatus status) {
        statuses.add(status);
    }

    public Optional<User> getAssignee() {
        return assignee;
    }
    
    public boolean isUnassignedOnly() {
        return unassignedOnly;
    }

    public Optional<UsagePoint> getUsagePoint() {
        return usagePoint;
    }

    public Optional<IssueReason> getIssueReason() {
        return issueReason;
    }

    public List<IssueStatus> getStatuses() {
        return Collections.unmodifiableList(statuses);
    }
}