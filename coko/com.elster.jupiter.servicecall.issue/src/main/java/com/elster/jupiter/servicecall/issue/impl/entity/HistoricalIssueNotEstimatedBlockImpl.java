/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.servicecall.issue.impl.entity;

import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.servicecall.issue.HistoricalIssueNotEstimatedBlock;
import com.elster.jupiter.servicecall.issue.HistoricalIssueServiceCall;
import com.elster.jupiter.servicecall.issue.NotEstimatedBlock;

public class HistoricalIssueNotEstimatedBlockImpl extends NotEstimatedBlockImpl implements HistoricalIssueNotEstimatedBlock {

    @IsPresent
    private Reference<HistoricalIssueServiceCall> issue = ValueReference.absent();

    HistoricalIssueNotEstimatedBlockImpl init(HistoricalIssueServiceCall issue, NotEstimatedBlock block) {
        this.issue.set(issue);
    //    this.init(block.getChannel(), block.getReadingType(), block.getStartTime(), block.getEndTime());
        return this;
    }
}
