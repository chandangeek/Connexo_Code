/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.insight.issue.datavalidation.impl.entity;

import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.insight.issue.datavalidation.UsagePointHistoricalIssueDataValidation;
import com.elster.insight.issue.datavalidation.UsagePointHistoricalIssueUsagePointNotEstimatedBlock;
import com.elster.insight.issue.datavalidation.UsagePointNotEstimatedBlock;

public class UsagePointHistoricalIssueUsagePointUsagePointNotEstimatedBlockImpl extends UsagePointUsagePointNotEstimatedBlockImpl implements UsagePointHistoricalIssueUsagePointNotEstimatedBlock {

    @IsPresent
    private Reference<UsagePointHistoricalIssueDataValidation> issue = ValueReference.absent();

    UsagePointHistoricalIssueUsagePointUsagePointNotEstimatedBlockImpl init(UsagePointHistoricalIssueDataValidation issue, UsagePointNotEstimatedBlock block) {
        this.issue.set(issue);
        this.init(block.getChannel(), block.getReadingType(), block.getStartTime(), block.getEndTime());
        return this;
    }
}
