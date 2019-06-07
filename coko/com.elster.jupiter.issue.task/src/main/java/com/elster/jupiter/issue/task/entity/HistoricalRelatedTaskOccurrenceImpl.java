/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.task.entity;

import com.elster.jupiter.issue.task.HistoricalRelatedTaskOccurrence;
import com.elster.jupiter.issue.task.TaskIssue;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.tasks.TaskOccurrence;

import java.time.Instant;

public class HistoricalRelatedTaskOccurrenceImpl extends RelatedTaskOccurrenceImpl implements HistoricalRelatedTaskOccurrence {

    @IsPresent
    private Reference<TaskIssue> issue = Reference.empty();


    RelatedTaskOccurrenceImpl init(TaskIssue taskIssue, TaskOccurrence taskOccurrence, String errorMessage, Instant failureTime) {
        this.issue.set(taskIssue);
        super.init(taskOccurrence, errorMessage, failureTime);
        return this;
    }

}
