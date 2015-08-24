package com.energyict.mdc.issue.datavalidation;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ReadingType;

import java.time.Instant;

@ProviderType
public interface OpenIssueDataValidation extends OpenIssue, IssueDataValidation {
    
    HistoricalIssueDataValidation close(IssueStatus status);

    void addNotEstimatedBlock(Channel channel, ReadingType readingType, Instant timeStamp);
    
    void removeNotEstimatedBlock(Channel channel, ReadingType readingType, Instant timeStamp);
}
