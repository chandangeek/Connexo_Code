package com.elster.jupiter.issue.rest.response.issue;

import com.elster.jupiter.issue.rest.response.IssueAssigneeInfo;
import com.elster.jupiter.issue.rest.response.IssueReasonInfo;
import com.elster.jupiter.issue.rest.response.IssueStatusInfo;
import com.elster.jupiter.issue.rest.response.device.DeviceInfo;
import com.elster.jupiter.issue.rest.response.device.DeviceShortInfo;
import com.elster.jupiter.issue.share.entity.BaseIssue;
import com.elster.jupiter.metering.EndDevice;

public class IssueInfo<T extends DeviceInfo> {
    private long id;
    private IssueReasonInfo reason;
    private IssueStatusInfo status;
    private long dueDate;
    private IssueAssigneeInfo assignee;
    private DeviceInfo device;
    private long creationDate;
    private long version;

    public IssueInfo(BaseIssue issue){
        init(issue, DeviceShortInfo.class);
    }

    public IssueInfo(BaseIssue issue, Class<T> deviceType){
        init(issue, deviceType);
    }

    private final void init(BaseIssue issue, Class<? extends DeviceInfo> deviceType){
        if (issue != null) {
            this.id = issue.getId();
            this.reason = new IssueReasonInfo(issue.getReason());
            this.status = new IssueStatusInfo(issue.getStatus());
            this.dueDate = issue.getDueDate() != null ? issue.getDueDate().getTime() : 0;
            this.assignee = (issue.getAssignee() != null ? new IssueAssigneeInfo(issue.getAssignee()) : null);
            try {
                this.device = issue.getDevice() != null ? deviceType.getConstructor(EndDevice.class).newInstance(issue.getDevice()) : null;
            } catch (ReflectiveOperationException e) {
            }
            this.creationDate = issue.getCreateTime().getTime();
            this.version = issue.getVersion();
        }
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public IssueReasonInfo getReason() {
        return reason;
    }

    public void setReason(IssueReasonInfo reason) {
        this.reason = reason;
    }

    public IssueStatusInfo getStatus() {
        return status;
    }

    public void setStatus(IssueStatusInfo status) {
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

    public DeviceInfo getDevice() {
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
