package com.elster.jupiter.issue.share.entity;

import com.elster.jupiter.issue.share.Priority;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.users.User;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;
import java.util.Optional;

@ProviderType
public interface Issue extends Entity {

    String getIssueId();

    String getTitle();

    IssueReason getReason();

    void setReason(IssueReason reason);

    IssueStatus getStatus();

    void setStatus(IssueStatus status);

    IssueAssignee getAssignee();

    EndDevice getDevice();

    void setDevice(EndDevice device);

    Optional<UsagePoint> getUsagePoint();

    Instant getDueDate();

    void setDueDate(Instant dueDate);

    boolean isOverdue();

    void setOverdue(boolean overdue);

    CreationRule getRule();

    void setRule(CreationRule rule);

    Optional<IssueComment> addComment(String body, User author);

    void assignTo(Long userId, Long workGroupId);

    void assignTo(IssueAssignee assignee);

    void assignTo(String type, long id);

    void autoAssign();

    Priority getPriority();

    void setPriority(Priority priority);

    Instant getCreateDateTime();

    void setCreateDateTime(Instant dateTime);

}
