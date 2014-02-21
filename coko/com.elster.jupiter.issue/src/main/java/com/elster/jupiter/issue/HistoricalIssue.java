package com.elster.jupiter.issue;

import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.util.time.UtcInstant;

public interface HistoricalIssue {
    long getId();

    IssueStatus getStatus();

    IssueReason getReason();

    UtcInstant getDueDate();

    long getDeviceId();

    IssueAssigneeType getAssigneeType();

    long getAssigneeId();

    UtcInstant getCreateTime();
}
