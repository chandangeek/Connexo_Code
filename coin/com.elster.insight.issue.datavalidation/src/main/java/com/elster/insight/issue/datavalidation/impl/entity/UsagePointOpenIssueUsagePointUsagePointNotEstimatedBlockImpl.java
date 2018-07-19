/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.insight.issue.datavalidation.impl.entity;

import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.insight.issue.datavalidation.UsagePointOpenIssueDataValidation;
import com.elster.insight.issue.datavalidation.UsagePointOpenIssueUsagePointNotEstimatedBlock;

import java.time.Instant;

public class UsagePointOpenIssueUsagePointUsagePointNotEstimatedBlockImpl extends UsagePointUsagePointNotEstimatedBlockImpl implements UsagePointOpenIssueUsagePointNotEstimatedBlock {

    @IsPresent
    private Reference<UsagePointOpenIssueDataValidation> issue = ValueReference.absent();
    
    UsagePointOpenIssueUsagePointUsagePointNotEstimatedBlockImpl init(UsagePointOpenIssueDataValidation issue, Channel channel, ReadingType readingType, Instant startTime, Instant endTime) {
        this.issue.set(issue);
        super.init(channel, readingType, startTime, endTime);
        return this;
    }
}
