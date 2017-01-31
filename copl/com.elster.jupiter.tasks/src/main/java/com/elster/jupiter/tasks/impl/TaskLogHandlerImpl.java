/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.tasks.impl;

import com.elster.jupiter.tasks.TaskLogHandler;

import java.time.Instant;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class TaskLogHandlerImpl extends Handler implements TaskLogHandler {

    private TaskOccurrenceImpl taskOccurrence;

    public TaskLogHandlerImpl(TaskOccurrenceImpl taskOccurrence) {
        this.taskOccurrence = taskOccurrence;
    }

    @Override
    public Handler asHandler() {
        return this;
    }

    @Override
    public void publish(LogRecord record) {
        taskOccurrence.log(record.getLevel(), Instant.ofEpochMilli(record.getMillis()), record.getMessage());
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() throws SecurityException {
    }
}
