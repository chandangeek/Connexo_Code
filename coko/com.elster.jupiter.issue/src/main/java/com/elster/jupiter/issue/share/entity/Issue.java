/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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

    //TODO - as per IssueImpl todo:  abstracting out and splitting the issue into 2: MDM and MDC

    void setUsagePoint(UsagePoint usagePoint);

    Optional<UsagePoint> getUsagePoint();

    Instant getDueDate();

    void setDueDate(Instant dueDate);

    boolean isOverdue();

    void setOverdue(boolean overdue);

    Optional<CreationRule> getRule();

    void setRule(CreationRule rule);

    Optional<IssueComment> addComment(String body, User author);

    void removeComment(long id, User author);

    Optional<IssueComment> editComment(long id, String body, User author);

    void assignTo(Long userId, Long workGroupId);

    void assignTo(IssueAssignee assignee);

    void assignTo(String type, long id);

    void autoAssign();

    Priority getPriority();

    void setPriority(Priority priority);

    Instant getCreateDateTime();

    void setCreateDateTime(Instant dateTime);

    Optional<Instant> getSnoozeDateTime();

    void snooze(Instant snoozeDateTime);

    void clearSnooze();

    default IssueType getType() {
        return getReason().getIssueType();
    }

    default void setType(IssueType type) {
        // none
    }
}
