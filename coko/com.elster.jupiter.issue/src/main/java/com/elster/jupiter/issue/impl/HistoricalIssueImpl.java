package com.elster.jupiter.issue.impl;

import com.elster.jupiter.issue.*;
import com.elster.jupiter.util.time.UtcInstant;

public class HistoricalIssueImpl implements HistoricalIssue {
    protected long id;
    protected IssueReason reason;
    protected long reasonId;
    protected UtcInstant dueDate;
    protected IssueStatus status;
    protected long deviceId;
    protected IssueAssigneeType assigneeType;
    protected long assigneeId;
    protected UtcInstant createTime;

    HistoricalIssueImpl(Issue fromIssue){
        if (fromIssue == null){
            throw new IllegalArgumentException("Source issue for historical issue can't be null");
        }
        this.reason = fromIssue.getReason();
        this.dueDate = fromIssue.getDueDate();
        this.status = fromIssue.getStatus();
        this.deviceId = fromIssue.getDevice().getId();
        this.assigneeType = fromIssue.getAssignee().getAssigneeType();
        // TODO here should be a valid reference to Assignee object
        this.assigneeId = 0L;
        this.createTime = fromIssue.getCreateTime();
    }

    @Override
    public long getId() {
        return id;
    }
    @Override
    public IssueReason getReason() {
        return reason;
    }
    @Override
    public UtcInstant getDueDate() {
        return dueDate;
    }
    @Override
    public IssueStatus getStatus() {
        return status;
    }
    @Override
    public long getDeviceId() {
        return deviceId;
    }
    @Override
    public IssueAssigneeType getAssigneeType() {
        return assigneeType;
    }
    @Override
    public long getAssigneeId() {
        return assigneeId;
    }
    @Override
    public UtcInstant getCreateTime() {
        return createTime;
    }

    public long getReasonId() {
        return reasonId;
    }

    public void setReasonId(long reasonId) {
        this.reasonId = reasonId;
    }
}
