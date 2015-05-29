package com.elster.jupiter.issue.share.entity;

import java.time.Instant;
import java.util.Optional;

import aQute.bnd.annotation.ConsumerType;

import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.users.User;

@ConsumerType
public interface Issue extends Entity {

    String getTitle();
    IssueReason getReason();
    IssueStatus getStatus();
    IssueAssignee getAssignee();
    EndDevice getDevice();
    Optional<UsagePoint> getUsagePoint();
    Instant getDueDate();
    boolean isOverdue();
    CreationRule getRule();

    void setReason(IssueReason reason);
    void setStatus(IssueStatus status);
    void setDevice(EndDevice device);
    void setDueDate(Instant dueDate);
    void setOverdue(boolean overdue);
    void setRule(CreationRule rule);

    Optional<IssueComment> addComment(String body, User author);
    void assignTo(String type, long id);
    void assignTo(IssueAssignee assignee);
    void autoAssign();
}
