/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.insight.issue.datavalidation;

import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ReadingType;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;

@ProviderType
public interface UsagePointOpenIssueDataValidation extends OpenIssue, UsagePointIssueDataValidation {
    
    UsagePointHistoricalIssueDataValidation close(IssueStatus status);

    void addNotEstimatedBlock(Channel channel, ReadingType readingType, Instant timeStamp);
    
    void removeNotEstimatedBlock(Channel channel, ReadingType readingType, Instant timeStamp);
}
