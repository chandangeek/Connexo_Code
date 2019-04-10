/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.alarms;

import com.elster.jupiter.issue.share.Priority;
import com.elster.jupiter.issue.share.entity.CreationRule;
import com.elster.jupiter.issue.share.entity.DueDateRange;
import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.WorkGroup;

import aQute.bnd.annotation.ProviderType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ProviderType
public class DeviceAlarmFilter {
    private String alarmId;
    private List<IssueStatus> statuses = new ArrayList<>();
    private List<IssueReason> reasons = new ArrayList<>();
    private List<EndDevice> devices = new ArrayList<>();
    private List<EndDeviceGroup> deviceGroups = new ArrayList<>();
    private List<User> userAssignee = new ArrayList<>();
    private List<WorkGroup> workGroupAssignees = new ArrayList<>();
    private List<DueDateRange> dueDates = new ArrayList<>();
    private List<Boolean> cleared = new ArrayList<>();
    private List<Priority> priorities = new ArrayList<>();
    private boolean unassignedOnly = false;
    private boolean unassignedWorkGroupSelected = false;
    private Long startCreateTime = null;
    private Long endCreateTime = null;
    private CreationRule rule = null;

    public DeviceAlarmFilter() {
    }

    public void setUnassignedOnly() {
        this.unassignedOnly = true;
    }

    public void setUserAssignee(User userAssignee) {
        this.userAssignee.add(userAssignee);
    }

    public void setDevice(EndDevice device) {
        if (device != null) {
            this.devices.add(device);
        }
    }

    public void setAlarmReason(IssueReason issueReason) {
        if (issueReason != null) {
            this.reasons.add(issueReason);
        }
    }

    public void setStatus(IssueStatus status) {
        if (status != null) {
            statuses.add(status);
        }
    }

    public List<Priority> getPriorities() {
        return Collections.unmodifiableList(priorities);
    }

    public void setPriority(Priority priority) {
        this.priorities.add(priority);
    }

    public List<User> getUserAssignee() {
        return Collections.unmodifiableList(userAssignee);
    }

    public boolean isUnassignedOnly() {
        return unassignedOnly;
    }

    public List<EndDevice> getDevices() {
        return Collections.unmodifiableList(this.devices);
    }

    public List<IssueReason> getAlarmReasons() {
        return Collections.unmodifiableList(this.reasons);
    }

    public List<IssueStatus> getStatuses() {
        return Collections.unmodifiableList(this.statuses);
    }

    public String getAlarmId() {
        return alarmId;
    }

    public void setAlarmId(String alarmId) {
        this.alarmId = alarmId.trim();
    }

    public List<WorkGroup> getWorkGroupAssignees() {
        return Collections.unmodifiableList(workGroupAssignees);
    }

    public void addWorkGroupAssignees(WorkGroup workGroup) {
        this.workGroupAssignees.add(workGroup);
    }

    public List<DueDateRange> getDueDates() {
        return Collections.unmodifiableList(dueDates);
    }

    public void setDueDates(long startTime, long endTime) {
        this.dueDates.add(new DueDateRange(startTime, endTime));
    }

    public boolean isUnassignedWorkGroupSelected() {
        return unassignedWorkGroupSelected;
    }

    public void setUnassignedWorkGroupSelected() {
        this.unassignedWorkGroupSelected = true;
    }

    public List<Boolean> getCleared() {
        return Collections.unmodifiableList(cleared);
    }

    public void addToClearead(boolean cleared) {
        this.cleared.add(cleared);
    }

    public Long getStartCreateTime() {
        return startCreateTime;
    }

    public void setStartCreateTime(Long startCreateTime) {
        this.startCreateTime = startCreateTime;
    }

    public Long getEndCreateTime() {
        return endCreateTime;
    }

    public void setEndCreateTime(Long endCreateTime) {
        this.endCreateTime = endCreateTime;
    }

    public CreationRule getRule() {
        return rule;
    }

    public void setRule(CreationRule rule) {
        this.rule = rule;
    }

    public void addDeviceGroup(EndDeviceGroup deviceGroup) {
        this.deviceGroups.add(deviceGroup);
    }

    public void setDeviceGroups(List<EndDeviceGroup> deviceGroups) {this.deviceGroups = deviceGroups;}

    public List<EndDeviceGroup> getDeviceGroups() { return this.deviceGroups; }
}