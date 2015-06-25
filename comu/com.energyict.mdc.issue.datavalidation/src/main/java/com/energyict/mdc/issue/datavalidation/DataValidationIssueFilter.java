package com.energyict.mdc.issue.datavalidation;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.users.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@ProviderType
public class DataValidationIssueFilter {

    private List<IssueStatus> statuses = new ArrayList<>();

    private boolean unassignedOnly = false;
    
    private Optional<User> assignee = Optional.empty();

    private Optional<IssueReason> issueReason = Optional.empty();

    private Optional<EndDevice> device = Optional.empty();

    public void setUnassignedOnly() {
        this.unassignedOnly = true;
    }
    
    public void setAssignee(User assignee) {
        this.assignee = Optional.of(assignee);
    }

    public void setDevice(EndDevice device) {
        this.device = Optional.of(device);
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
    
    public Optional<EndDevice> getDevice() {
        return device;
    }

    public Optional<IssueReason> getIssueReason() {
        return issueReason;
    }

    public List<IssueStatus> getStatuses() {
        return Collections.unmodifiableList(statuses);
    }
}