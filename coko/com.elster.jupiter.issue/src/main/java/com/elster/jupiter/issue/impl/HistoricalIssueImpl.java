package com.elster.jupiter.issue.impl;

import com.elster.jupiter.issue.*;
import com.elster.jupiter.util.time.UtcInstant;

public class HistoricalIssueImpl implements HistoricalIssue {
    protected long id;
    protected String type;
    protected UtcInstant createTime;
    protected UtcInstant dueDate;
    protected long reasonId;
    protected long statusId;
    protected long deviceId;
    protected IssueAssigneeType assigneeType;
    protected long assigneeUserId;
    protected long assigneeTeamId;
    protected long assigneeRoleId;

    HistoricalIssueImpl(Issue fromIssue){
        if (fromIssue == null){
            throw new IllegalArgumentException("Source issue for historical issue can't be null");
        }
        this.type = Issue.TYPE_IDENTIFIER;
        this.createTime = fromIssue.getCreateTime();
        this.dueDate = fromIssue.getDueDate();
        this.reasonId = fromIssue.getReason().getId();
        this.statusId = fromIssue.getStatus().getId();
        this.deviceId = fromIssue.getDevice().getId();
        this.assigneeType = fromIssue.getAssignee().getType();
        switch (this.assigneeType){
            case USER:
                if (fromIssue.getAssignee().getUser() != null){
                    this.assigneeUserId = fromIssue.getAssignee().getUser().getId();
                }
                break;
            case TEAM:
                if (fromIssue.getAssignee().getTeam() != null) {
                    this.assigneeTeamId = fromIssue.getAssignee().getTeam().getId();
                }
                break;
            case ROLE:
                if (fromIssue.getAssignee().getRole() != null) {
                    this.assigneeRoleId = fromIssue.getAssignee().getRole().getId();
                }
                break;
        }
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public UtcInstant getCreateTime() {
        return createTime;
    }

    public void setCreateTime(UtcInstant createTime) {
        this.createTime = createTime;
    }

    public UtcInstant getDueDate() {
        return dueDate;
    }

    public void setDueDate(UtcInstant dueDate) {
        this.dueDate = dueDate;
    }

    public long getReasonId() {
        return reasonId;
    }

    public void setReasonId(long reasonId) {
        this.reasonId = reasonId;
    }

    public long getStatusId() {
        return statusId;
    }

    public void setStatusId(long statusId) {
        this.statusId = statusId;
    }

    public long getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(long deviceId) {
        this.deviceId = deviceId;
    }

    public IssueAssigneeType getAssigneeType() {
        return assigneeType;
    }

    public void setAssigneeType(IssueAssigneeType assigneeType) {
        this.assigneeType = assigneeType;
    }

    public long getAssigneeUserId() {
        return assigneeUserId;
    }

    public void setAssigneeUserId(long assigneeUserId) {
        this.assigneeUserId = assigneeUserId;
    }

    public long getAssigneeTeamId() {
        return assigneeTeamId;
    }

    public void setAssigneeTeamId(long assigneeTeamId) {
        this.assigneeTeamId = assigneeTeamId;
    }

    public long getAssigneeRoleId() {
        return assigneeRoleId;
    }

    public void setAssigneeRoleId(long assigneeRoleId) {
        this.assigneeRoleId = assigneeRoleId;
    }
}
