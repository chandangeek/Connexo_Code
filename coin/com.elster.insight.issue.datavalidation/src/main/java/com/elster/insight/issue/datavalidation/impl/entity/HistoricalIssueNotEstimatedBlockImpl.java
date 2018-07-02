/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.insight.issue.datavalidation.impl.entity;

import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.insight.issue.datavalidation.HistoricalIssueDataValidation;
import com.elster.insight.issue.datavalidation.HistoricalIssueNotEstimatedBlock;
import com.elster.insight.issue.datavalidation.NotEstimatedBlock;

public class HistoricalIssueNotEstimatedBlockImpl extends NotEstimatedBlockImpl implements HistoricalIssueNotEstimatedBlock {

    @IsPresent
    private Reference<HistoricalIssueDataValidation> issue = ValueReference.absent();

    HistoricalIssueNotEstimatedBlockImpl init(HistoricalIssueDataValidation issue, NotEstimatedBlock block) {
        this.issue.set(issue);
        this.init(block.getChannel(), block.getReadingType(), block.getStartTime(), block.getEndTime());
        return this;
    }
}
