package com.elster.jupiter.issue.rest.response.issue;

import com.elster.jupiter.issue.rest.response.IssueAssigneeInfo;
import com.elster.jupiter.issue.rest.response.device.DeviceInfo;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.metering.EndDevice;

public class IssueInfo<T extends DeviceInfo> {
    private long id;
    private String reason;
    private String status;
    private long dueDate;
    private IssueAssigneeInfo assignee;
    private T device;
    private long creationDate;
    private long version;

    public IssueInfo(Issue issue, Class<T> deviceType){
        if (issue != null) {
            this.setId(issue.getId());
            this.setReason(issue.getReason().getName());
            this.setStatus(issue.getStatus().getName());
            this.setDueDate(issue.getDueDate() != null ? issue.getDueDate().getTime() : 0);
            this.setAssignee(issue.getAssignee() != null ? new IssueAssigneeInfo(issue.getAssignee()) : null);
            try {
                this.setDevice(issue.getDevice() != null ? deviceType.getConstructor(EndDevice.class).newInstance(issue.getDevice()) : null);
            } catch (ReflectiveOperationException e) {
            }
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

    public IssueAssigneeInfo getAssignee() {
        return assignee;
    }

    public void setAssignee(IssueAssigneeInfo assignee) {
        this.assignee = assignee;
    }

    public T getDevice() {
        return device;
    }

    public void setDevice(T device) {
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
