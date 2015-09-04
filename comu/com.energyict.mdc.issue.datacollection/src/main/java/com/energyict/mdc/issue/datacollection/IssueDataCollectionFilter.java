package com.energyict.mdc.issue.datacollection;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.users.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ProviderType
public class IssueDataCollectionFilter {
    private List<IssueStatus> statuses = new ArrayList<>();
    private List<IssueReason> reasons = new ArrayList<>();
    private List<EndDevice> devices = new ArrayList<>();
    private Optional<User> assignee = Optional.empty();
    private boolean unassignedOnly = false;

    public IssueDataCollectionFilter() {
    }

    public void setUnassignedOnly() {
        this.unassignedOnly = true;
    }

    public void setAssignee(User assignee) {
        this.assignee = Optional.of(assignee);
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

    public Optional<User> getAssignee() {
        return assignee;
    }

    public boolean isUnassignedOnly() {
        return unassignedOnly;
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
}