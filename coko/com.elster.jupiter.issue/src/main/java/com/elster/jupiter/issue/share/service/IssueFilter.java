package com.elster.jupiter.issue.share.service;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.issue.share.entity.DueDateRange;
import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.users.User;

import java.util.ArrayList;
import java.util.List;

@ProviderType
public class IssueFilter {
    private List<IssueStatus> statuses = new ArrayList<>();
    private List<IssueReason> reasons = new ArrayList<>();
    private List<EndDevice> devices = new ArrayList<>();
    private List<User> assignees = new ArrayList<>();
    private List<DueDateRange> dueDates = new ArrayList<>();
    private List<IssueType> issueTypes = new ArrayList<>();
    private boolean unassignedSelected = false;

    public IssueFilter() {
    }

    public void setUnassignedSelected() {
        this.unassignedSelected = true;
    }

    public void addDevice(EndDevice device) {
        if (device != null) {
            this.devices.add(device);
        }
    }

    public void setIssueReason(IssueReason issueReason) {
        if (issueReason != null) {
            this.reasons.add(issueReason);
        }
    }

    public void addStatus(IssueStatus status) {
        if (status != null) {
            statuses.add(status);
        }
    }

    public void addAssignee(User assignee) {
        this.assignees.add(assignee);
    }

    public List<User> getAssignees() {
        return this.assignees;
    }

    public boolean isUnassignedSelected() {
        return unassignedSelected;
    }

    public List<EndDevice> getDevices() {
        return this.devices;
    }

    public List<IssueReason> getIssueReasons() {
        return this.reasons;
    }

    public List<IssueStatus> getStatuses() {
        return this.statuses;
    }

    public List<DueDateRange> getDueDates() {
        return this.dueDates;
    }

    public void addDueDate(long startTime, long endTime) {
        this.dueDates.add(new DueDateRange(startTime, endTime));
    }

    public List<IssueType> getIssueTypes() {
        return issueTypes;
    }

    public void addIssueType(IssueType issueType) {
        this.issueTypes.add(issueType);
    }
}
