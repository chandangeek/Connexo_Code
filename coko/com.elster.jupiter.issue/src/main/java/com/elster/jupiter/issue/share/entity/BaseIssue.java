package com.elster.jupiter.issue.share.entity;

import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.util.time.UtcInstant;

public interface BaseIssue extends Entity {

    String getTitle();

    IssueReason getReason();

    void setReason(IssueReason reason);

    IssueStatus getStatus();

    void setStatus(IssueStatus status);

    UtcInstant getDueDate();

    void setDueDate(UtcInstant dueDate);

    EndDevice getDevice();

    void setDevice(EndDevice device);

    CreationRule getRule();

    void setRule(CreationRule rule);

    boolean isOverdue();

    void setOverdue(boolean overdue);

    IssueAssignee getAssignee();
}
