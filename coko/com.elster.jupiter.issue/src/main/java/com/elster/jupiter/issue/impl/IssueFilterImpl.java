/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.impl;

import com.elster.jupiter.issue.share.IssueFilter;
import com.elster.jupiter.issue.share.Priority;
import com.elster.jupiter.issue.share.entity.DueDateRange;
import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.WorkGroup;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class IssueFilterImpl implements IssueFilter {
    private String issueId;
    private List<IssueStatus> statuses = new ArrayList<>();
    private List<IssueReason> reasons = new ArrayList<>();
    private Set<EndDevice> devices = new HashSet<>();
    private List<EndDeviceGroup> deviceGroups = new ArrayList<>();
    private List<UsagePoint> usagePoints = new ArrayList<>();
    private List<User> assignees = new ArrayList<>();
    private List<WorkGroup> workGroupAssignees = new ArrayList<>();
    private List<DueDateRange> dueDates = new ArrayList<>();
    private List<IssueType> issueTypes = new ArrayList<>();
    private List<Priority> priorities = new ArrayList();
    private boolean unassignedSelected = false;
    private boolean unassignedWorkGroupSelected = false;
    private Long startCreateTime;
    private Long endCreateTime;


    @Override
    public void setIssueId(String issueId) {
        this.issueId = issueId;
    }

    @Override
    public Optional<String> getIssueId() {
        return Optional.ofNullable(this.issueId);
    }

    @Override
    public void setUnassignedSelected() {
        this.unassignedSelected = true;
    }

    @Override
    public void setUnassignedWorkGroupSelected() {
        this.unassignedWorkGroupSelected = true;
    }

    @Override
    public void addDevice(EndDevice device) {
        if (device != null) {
            this.devices.add(device);
        }
    }

    @Override
    public void addDeviceGroup(EndDeviceGroup deviceGroup) {
            this.deviceGroups.add(deviceGroup);
    }

    @Override
    public List<EndDeviceGroup> getDeviceGroups() {
        return this.deviceGroups;
    }

    @Override
    public void addUsagePoint(UsagePoint usagePoint) {
        if (usagePoint != null) {
            this.usagePoints.add(usagePoint);
        }
    }

    @Override
    public void setIssueReason(IssueReason issueReason) {
        if (issueReason != null) {
            this.reasons.add(issueReason);
        }
    }

    @Override
    public void setPriority(Priority priority) {
        if (priority != null) {
            this.priorities.add(priority);
        }
    }

    @Override
    public void addStatus(IssueStatus status) {
        if (status != null) {
            statuses.add(status);
        }
    }

    @Override
    public void addAssignee(User assignee) {
        this.assignees.add(assignee);
    }

    @Override
    public void addWorkGroupAssignee(WorkGroup workGroup) {
        this.workGroupAssignees.add(workGroup);
    }

    @Override
    public List<User> getAssignees() {
        return this.assignees;
    }

    @Override
    public List<WorkGroup> getWorkGroupAssignees() {
        return this.workGroupAssignees;
    }

    @Override
    public boolean isUnassignedSelected() {
        return unassignedSelected;
    }

    @Override
    public boolean isUnassignedWorkGroupSelected() {
        return unassignedWorkGroupSelected;
    }

    @Override
    public List<EndDevice> getDevices() {
        return ImmutableList.copyOf(this.devices);
    }

    @Override
    public List<UsagePoint> getUsagePoints() {
        return this.usagePoints;
    }

    @Override
    public List<IssueReason> getIssueReasons() {
        return this.reasons;
    }

    @Override
    public List<IssueStatus> getStatuses() {
        return this.statuses;
    }

    @Override
    public List<DueDateRange> getDueDates() {
        return this.dueDates;
    }

    @Override
    public List<Priority> getPriorities() {
        return Collections.unmodifiableList(priorities);
    }

    @Override
    public void addDueDate(long startTime, long endTime) {
        this.dueDates.add(new DueDateRange(startTime, endTime));
    }

    @Override
    public List<IssueType> getIssueTypes() {
        return Collections.unmodifiableList(issueTypes);
    }

    @Override
    public void addIssueType(IssueType issueType) {
        this.issueTypes.add(issueType);
    }

    @Override
    public Long getStartCreateTime() {
        return startCreateTime;
    }

    @Override
    public void setStartCreateTime(Long startCreateTime) {
        this.startCreateTime = startCreateTime;
    }

    @Override
    public Long getEndCreateTime() {
        return endCreateTime;
    }

    @Override
    public void setEndCreateTime(Long endCreateTime) {
        this.endCreateTime = endCreateTime;
    }

}