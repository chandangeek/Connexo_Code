/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.servicecall;

import com.elster.jupiter.issue.share.entity.CreationRule;
import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.users.User;

import aQute.bnd.annotation.ProviderType;
import com.google.common.collect.ImmutableList;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@ProviderType
public class ServiceCallIssueFilter {
    private boolean unassignedOnly;
    private User assignee;
    private Set<IssueStatus> statuses = new HashSet<>();
    private Set<IssueReason> issueReasons = new HashSet<>();
    private Set<CreationRule> rules = new HashSet<>();
    private Set<ServiceCall> serviceCalls = new HashSet<>();

    public void setUnassignedOnly() {
        this.unassignedOnly = true;
    }
    
    public void setAssignee(User assignee) {
        this.assignee = assignee;
    }

    public void addIssueReason(IssueReason issueReason) {
        issueReasons.add(issueReason);
    }

    public void addStatus(IssueStatus status) {
        statuses.add(status);
    }

    public Optional<User> getAssignee() {
        return Optional.ofNullable(assignee);
    }
    
    public boolean isUnassignedOnly() {
        return unassignedOnly;
    }

    public List<IssueReason> getIssueReasons() {
        return ImmutableList.copyOf(issueReasons);
    }

    public List<IssueStatus> getStatuses() {
        return ImmutableList.copyOf(statuses);
    }

    public void addRule(CreationRule rule) {
        rules.add(rule);
    }

    public List<CreationRule> getRules() {
        return ImmutableList.copyOf(rules);
    }

    public void addServiceCall(ServiceCall serviceCall) {
        serviceCalls.add(serviceCall);
    }

    public List<ServiceCall> getServiceCalls() {
        return ImmutableList.copyOf(serviceCalls);
    }
}
