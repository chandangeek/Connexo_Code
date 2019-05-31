/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.servicecall.issue.impl.entity;

import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.servicecall.issue.OpenIssueNotEstimatedBlock;
import com.elster.jupiter.servicecall.issue.OpenIssueServiceCall;

import java.time.Instant;

public class OpenIssueNotEstimatedBlockImpl extends NotEstimatedBlockImpl implements OpenIssueNotEstimatedBlock {

    @IsPresent
    private Reference<OpenIssueServiceCall> issue = ValueReference.absent();
    
    OpenIssueNotEstimatedBlockImpl init(OpenIssueServiceCall issue, Channel channel, ReadingType readingType, Instant startTime, Instant endTime) {
        this.issue.set(issue);
        //super.init(channel, readingType, startTime, endTime);
        return this;
    }
}
