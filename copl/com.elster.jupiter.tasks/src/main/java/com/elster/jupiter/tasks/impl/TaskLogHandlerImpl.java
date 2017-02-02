package com.elster.jupiter.tasks.impl;

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
