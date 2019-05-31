/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.servicecall.issue;

import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.OpenIssue;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface OpenIssueServiceCall extends OpenIssue, IssueServiceCall {
    
    HistoricalIssueServiceCall close(IssueStatus status);

//    void addNotEstimatedBlock(Channel channel, ReadingType readingType, Instant timeStamp);
//
//    void removeNotEstimatedBlock(Channel channel, ReadingType readingType, Instant timeStamp);
}
