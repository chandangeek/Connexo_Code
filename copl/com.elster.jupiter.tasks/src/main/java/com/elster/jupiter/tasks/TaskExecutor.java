/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.tasks;

public interface TaskExecutor {

    void execute(TaskOccurrence occurrence);

    default void postExecute(TaskOccurrence occurrence) {
    }
}
