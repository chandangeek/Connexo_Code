package com.elster.jupiter.issue.impl;

import aQute.bnd.annotation.ProviderType;

import com.elster.jupiter.issue.share.IssueFilter;
import com.elster.jupiter.issue.share.entity.DueDateRange;
import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.users.User;

import java.util.ArrayList;
import java.util.List;

@ProviderType
public class IssueFilterImpl implements IssueFilter {
    private List<IssueStatus> statuses = new ArrayList<>();
    private List<IssueReason> reasons = new ArrayList<>();
    private List<EndDevice> devices = new ArrayList<>();
    private List<User> assignees = new ArrayList<>();
    private List<DueDateRange> dueDates = new ArrayList<>();
    private List<IssueType> issueTypes = new ArrayList<>();
    private boolean unassignedSelected = false;

    public IssueFilterImpl() {
    }

    @Override
    public void setUnassignedSelected() {
        this.unassignedSelected = true;
    }

    @Override
    public void addDevice(EndDevice device) {
        if (device != null) {
            this.devices.add(device);
        }
    }

    @Override
    public void setIssueReason(IssueReason issueReason) {
        if (issueReason != null) {
            this.reasons.add(issueReason);
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
    public List<User> getAssignees() {
        return this.assignees;
    }

    @Override
    public boolean isUnassignedSelected() {
        return unassignedSelected;
    }

    @Override
    public List<EndDevice> getDevices() {
        return this.devices;
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
    public void addDueDate(long startTime, long endTime) {
        this.dueDates.add(new DueDateRange(startTime, endTime));
    }

    @Override
    public List<IssueType> getIssueTypes() {
        return issueTypes;
    }

    @Override
    public void addIssueType(IssueType issueType) {
        this.issueTypes.add(issueType);
    }
}
