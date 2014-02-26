package com.elster.jupiter.issue.rest.response;

import com.elster.jupiter.issue.Issue;

public class IssueInfo {
    private long id;
    private String reason;
    private String status;
    private long dueDate;
    private IssueAssignee assignee;
    private IssueDevice device;
    private long creationDate;
    private long version;

    public IssueInfo(Issue issue){
        if (issue != null) {
            this.setId(issue.getId());
            this.setReason(issue.getReason().getName());
            this.setStatus(issue.getStatus().getName());
            this.setDueDate(issue.getDueDate().getTime());
            this.setAssignee(new IssueAssignee(issue.getAssignee()));
            this.setDevice(new IssueDevice(issue.getDevice()));
            this.setCreationDate(issue.getCreateTime().getTime());
            this.setVersion(issue.getVersion());
        }
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getDueDate() {
        return dueDate;
    }

    public void setDueDate(long dueDate) {
        this.dueDate = dueDate;
    }

    public IssueAssignee getAssignee() {
        return assignee;
    }

    public void setAssignee(IssueAssignee assignee) {
        this.assignee = assignee;
    }

    public IssueDevice getDevice() {
        return device;
    }

    public void setDevice(IssueDevice device) {
        this.device = device;
    }

    public long getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(long creationDate) {
        this.creationDate = creationDate;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }
}
