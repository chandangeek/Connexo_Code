/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.task;

import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.tasks.TaskOccurrence;

import java.time.Instant;
import java.util.Optional;

public interface OpenTaskIssue extends OpenIssue, TaskIssue {
    
    HistoricalTaskIssue close(IssueStatus status);

    void addTaskOccurrence(TaskOccurrence taskOccurrence, String errorMessage, Instant failureTime);

    void removeTaskOccurrence(TaskOccurrence taskOccurrence, String errorMessage, Instant failureTime);
}
