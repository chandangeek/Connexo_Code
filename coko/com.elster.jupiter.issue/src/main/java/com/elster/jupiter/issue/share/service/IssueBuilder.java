/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.share.service;

import com.elster.jupiter.issue.share.Priority;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.UsagePoint;

import java.time.Instant;


public interface IssueBuilder {
    IssueBuilder withReason(IssueReason reason);
    IssueBuilder withPriority(Priority priority);
    IssueBuilder withDevice(EndDevice device);
    IssueBuilder withUsagePoint(UsagePoint usagePoint);
    IssueBuilder withComment(String comment);
    IssueBuilder withDueDate(Instant dueDate);
    Issue create();

}
