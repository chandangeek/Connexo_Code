package com.elster.jupiter.issue;

import com.elster.jupiter.util.time.UtcInstant;

public interface HistoricalIssue {
    long getId();
    String getType();
    UtcInstant getCreateTime();
    long getReasonId();
    UtcInstant getDueDate();
    long getDeviceId();
    long getStatusId();
    IssueAssigneeType getAssigneeType();
    long getAssigneeUserId();
    long getAssigneeTeamId();
    long getAssigneeRoleId();
}
