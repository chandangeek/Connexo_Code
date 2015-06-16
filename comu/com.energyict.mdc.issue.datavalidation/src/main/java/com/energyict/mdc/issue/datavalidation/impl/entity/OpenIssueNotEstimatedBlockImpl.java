package com.energyict.mdc.issue.datavalidation.impl.entity;

import java.time.Instant;

import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.energyict.mdc.issue.datavalidation.OpenIssueDataValidation;
import com.energyict.mdc.issue.datavalidation.OpenIssueNotEstimatedBlock;

public class OpenIssueNotEstimatedBlockImpl extends NotEstimatedBlockImpl implements OpenIssueNotEstimatedBlock {

    @IsPresent
    private Reference<OpenIssueDataValidation> issue = ValueReference.absent();
    
    OpenIssueNotEstimatedBlockImpl init(OpenIssueDataValidation issue, Channel channel, ReadingType readingType, Instant startTime, Instant endTime) {
        this.issue.set(issue);
        super.init(channel, readingType, startTime, endTime);
        return this;
    }
    
}
