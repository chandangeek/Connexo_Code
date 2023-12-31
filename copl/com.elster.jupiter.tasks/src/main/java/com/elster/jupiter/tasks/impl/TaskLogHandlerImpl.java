/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.tasks.impl;

import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.TaskLogHandler;

import java.time.Instant;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class TaskLogHandlerImpl extends Handler implements TaskLogHandler {

    private TaskOccurrenceImpl taskOccurrence;

    public TaskLogHandlerImpl(TaskOccurrenceImpl taskOccurrence) {
        this.taskOccurrence = taskOccurrence;
        this.setLevel( Level.parse(taskOccurrence.getRecurrentTask().getLogLevel()+"") );
    }

    public TaskLogHandlerImpl(TaskOccurrenceImpl taskOccurrence, RecurrentTask recurrentTask) {
        this.taskOccurrence = taskOccurrence;
        this.setLevel(Level.parse(recurrentTask.getLogLevel() + ""));
    }

    @Override
    public Handler asHandler() {
        return this;
    }

    @Override
    public void publish(LogRecord record) {
        if (isLoggable(record)) {
            taskOccurrence.log(record.getLevel(), Instant.ofEpochMilli(record.getMillis()), record.getMessage());
        }
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() throws SecurityException {
    }
}
