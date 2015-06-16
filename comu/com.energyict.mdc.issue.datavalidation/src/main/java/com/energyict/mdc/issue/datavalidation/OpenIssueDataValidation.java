package com.energyict.mdc.issue.datavalidation;

import java.time.Instant;

import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ReadingType;

public interface OpenIssueDataValidation extends OpenIssue, IssueDataValidation {

    void addNotEstimatedBlock(Channel channel, ReadingType readingType, Instant startTime, Instant endTime);
    
    void removeNotEstimatedBlock(Channel channel, ReadingType readingType, Instant timeStamp);
}
