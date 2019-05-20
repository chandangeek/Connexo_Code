/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.task.entity;


import com.elster.jupiter.issue.task.RelatedTaskOccurrence;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.tasks.TaskOccurrence;

import java.time.Instant;

public class RelatedTaskOccurrenceImpl implements RelatedTaskOccurrence {

    public enum Fields {
        ISSUE("issue"),
        TASK_OCCURRENCE("taskOccurrence"),
        ERROR_MESSAGE("errorMessage"),
        FAIL_TIME("failureTime");

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }
    }


    @IsPresent
    private Reference<TaskOccurrence> taskOccurrence = Reference.empty();
    @SuppressWarnings("unused")
    private String errorMessage;
    @SuppressWarnings("unused")
    private Instant failureTime;
    @SuppressWarnings("unused")
    private Instant createTime;

    RelatedTaskOccurrenceImpl init(TaskOccurrence eventRecord, String errorMessage, Instant failureTime) {
        this.taskOccurrence.set(eventRecord);
        this.errorMessage = errorMessage;
        this.failureTime = failureTime;
        return this;
    }

    @Override
    public TaskOccurrence getTaskOccurrence() {
        return taskOccurrence.get();
    }

    @Override
    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public Instant getFailureTime() {
        return failureTime;
    }
}
