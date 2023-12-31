/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.issue.datacollection.entity;

import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.OpenIssue;

public interface OpenIssueDataCollection extends OpenIssue, IssueDataCollection {
    
    HistoricalIssueDataCollection close(IssueStatus status);
    
}
