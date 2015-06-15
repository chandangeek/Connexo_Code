package com.energyict.mdc.issue.datavalidation.impl;

import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.energyict.mdc.issue.datavalidation.HistoricalIssueDataValidation;
import com.energyict.mdc.issue.datavalidation.HistoricalIssueNotEstimatedBlock;
import com.energyict.mdc.issue.datavalidation.NotEstimatedBlock;

public class HistoricalIssueNotEstimatedBlockImpl extends NotEstimatedBlockImpl implements HistoricalIssueNotEstimatedBlock {

    @IsPresent
    private Reference<HistoricalIssueDataValidation> issue = ValueReference.absent();

    HistoricalIssueNotEstimatedBlockImpl init(HistoricalIssueDataValidation issue, NotEstimatedBlock block) {
        this.issue.set(issue);
        this.init(block.getChannel(), block.getReadingType(), block.getStartTime(), block.getEndTime());
        return this;
    }
}
