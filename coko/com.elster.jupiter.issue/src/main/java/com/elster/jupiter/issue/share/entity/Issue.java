package com.elster.jupiter.issue.share.entity;

import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.time.UtcInstant;
import com.google.common.base.Optional;

public interface Issue extends Entity {

    String getTitle();
    IssueReason getReason();
    IssueStatus getStatus();
    IssueAssignee getAssignee();
    EndDevice getDevice();
    Optional<UsagePoint> getUsagePoint();
    UtcInstant getDueDate();
    boolean isOverdue();
    CreationRule getRule();

    void setReason(IssueReason reason);
    void setStatus(IssueStatus status);
    void setDevice(EndDevice device);
    void setDueDate(UtcInstant dueDate);
    void setOverdue(boolean overdue);
    void setRule(CreationRule rule);

    Optional<IssueComment> addComment(String body, User author);
    void assignTo(String type, long id);
    void assignTo(IssueAssignee assignee);
    void autoAssign();
}
