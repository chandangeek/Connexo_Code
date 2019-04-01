/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.task.entity;

import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.tasks.TaskOccurrence;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;
import java.util.Optional;

@ProviderType
public interface TaskIssue extends Issue {

    Optional<TaskOccurrence> getTaskOccurrence();

    String getErrorMessage();

    Instant getFailureTime();

}