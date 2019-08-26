/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.webservice.issue;

import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.users.User;

import aQute.bnd.annotation.ProviderType;
import com.google.common.collect.ImmutableList;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@ProviderType
public class WebServiceIssueFilter {
    private Set<IssueStatus> statuses = new HashSet<>();
    private boolean unassignedOnly;
    private User assignee;
    private IssueReason issueReason;
    private Set<Long> occurrenceIds = new HashSet<>();
    private Set<Long> endPointIds = new HashSet<>();

    public void setUnassignedOnly() {
        this.unassignedOnly = true;
    }
    
    public void setAssignee(User assignee) {
        this.assignee = assignee;
    }

    public void setIssueReason(IssueReason issueReason) {
        this.issueReason = issueReason;
    }

    public void addStatus(IssueStatus status) {
        statuses.add(status);
    }

    public void addWebServiceCallOccurrenceId(long occurrenceId) {
        occurrenceIds.add(occurrenceId);
    }

    public void addEndPointConfigurationId(long endPointConfigurationId) {
        endPointIds.add(endPointConfigurationId);
    }

    public Optional<User> getAssignee() {
        return Optional.ofNullable(assignee);
    }
    
    public boolean isUnassignedOnly() {
        return unassignedOnly;
    }
    
    public Optional<IssueReason> getIssueReason() {
        return Optional.ofNullable(issueReason);
    }

    public List<IssueStatus> getStatuses() {
        return ImmutableList.copyOf(statuses);
    }

    public List<Long> getWebServiceCallOccurrenceIds() {
        return ImmutableList.copyOf(occurrenceIds);
    }

    public List<Long> getEndPointConfigurationIds() {
        return ImmutableList.copyOf(endPointIds);
    }
}
