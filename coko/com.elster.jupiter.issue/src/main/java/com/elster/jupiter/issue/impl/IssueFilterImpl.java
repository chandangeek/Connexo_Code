/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.impl;

import com.elster.jupiter.issue.share.IssueFilter;
import com.elster.jupiter.issue.share.entity.DueDateRange;
import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.Location;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.UsagePointGroup;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.WorkGroup;

import com.google.common.collect.ImmutableList;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class IssueFilterImpl implements IssueFilter {
    private String issueId;
    private List<IssueStatus> statuses = new ArrayList<>();
    private List<IssueReason> reasons = new ArrayList<>();
    private List<EndDevice> devices = new ArrayList<>();
    private List<Location> locations = new ArrayList<>();
    private List<EndDeviceGroup> deviceGroups = new ArrayList<>();
    private List<UsagePoint> usagePoints = new ArrayList<>();
    private List<UsagePointGroup> usagePointGroup = new ArrayList<>();
    private List<User> assignees = new ArrayList<>();
    private List<WorkGroup> workGroupAssignees = new ArrayList<>();
    private List<DueDateRange> dueDates = new ArrayList<>();
    private List<IssueType> issueTypes = new ArrayList<>();
    private String priorities = "";
    private boolean unassignedSelected = false;
    private boolean unassignedWorkGroupSelected = false;
    private Long startCreateTime;
    private Long endCreateTime;
    private boolean showTopology = false;
    private Optional<Instant> untilSnoozeDateTime = Optional.empty();


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
    public void addLocation(Location location) {
        if (location != null) {
            this.locations.add(location);
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
    public void setShowTopology(boolean showTopology) {
        this.showTopology = showTopology;
    }

    @Override
    public boolean getShowTopology() {
        return this.showTopology;
    }

    @Override
    public void addUsagePoint(UsagePoint usagePoint) {
        if (usagePoint != null) {
            this.usagePoints.add(usagePoint);
        }
    }

    @Override
    public List<UsagePointGroup> getUsagePointGroups() {
        return this.usagePointGroup;
    }

    @Override
    public void addUsagePointGroup(UsagePointGroup usagePointGroup) {
        if (usagePointGroup != null) {
            this.usagePointGroup.add(usagePointGroup);
        }
    }

    @Override
    public void setIssueReason(IssueReason issueReason) {
        if (issueReason != null) {
            this.reasons.add(issueReason);
        }
    }

    @Override
    public void setPriority(String priority) {
        if (priority != null) {
            this.priorities = priority;
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
    public List<Location> getLocations() {
        return this.locations;
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
    public String getPriorities() {
        return priorities;
    }

    @Override
    public void addDueDate(long startTime, long endTime) {
        this.dueDates.add(new DueDateRange(startTime, endTime));
    }

    @Override
    public void addUntilSnoozeDateTime(Instant instant) {
        this.untilSnoozeDateTime = Optional.of(instant);
    }

    @Override
    public Optional<Instant> getUntilSnoozeDateTime() {
        return this.untilSnoozeDateTime;
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