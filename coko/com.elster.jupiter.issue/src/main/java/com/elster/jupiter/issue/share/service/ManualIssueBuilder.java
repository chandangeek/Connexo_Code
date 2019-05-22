/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.share.service;

import com.elster.jupiter.issue.share.Priority;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.UsagePoint;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;


@ProviderType
public interface ManualIssueBuilder {
    ManualIssueBuilder withReason(IssueReason reason);
    ManualIssueBuilder withType(IssueType type);
    ManualIssueBuilder withStatus(IssueStatus status);
    ManualIssueBuilder withPriority(Priority priority);
    ManualIssueBuilder withDevice(EndDevice device);
    ManualIssueBuilder withUsagePoint(UsagePoint usagePoint);
    ManualIssueBuilder withComment(String comment);
    ManualIssueBuilder withDueDate(Instant dueDate);
    ManualIssueBuilder withOverdue(boolean overdue);
    ManualIssueBuilder withAssignToUserAndWorkgroup(Long userId, Long workgroupId);
    ManualIssueBuilder withAssignComment(String assignComment);

    Issue create();

}
