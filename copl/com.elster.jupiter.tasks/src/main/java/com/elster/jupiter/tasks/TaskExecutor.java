/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.tasks;

import aQute.bnd.annotation.ConsumerType;

@ConsumerType
public interface TaskExecutor {

    void execute(TaskOccurrence occurrence);

    default void postExecute(TaskOccurrence occurrence) {
    }
}
