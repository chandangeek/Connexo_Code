/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.task.entity;

import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.OpenIssue;

public interface OpenTaskIssue extends OpenIssue, TaskIssue {
    
    HistoricalTaskIssue close(IssueStatus status);
    
}
