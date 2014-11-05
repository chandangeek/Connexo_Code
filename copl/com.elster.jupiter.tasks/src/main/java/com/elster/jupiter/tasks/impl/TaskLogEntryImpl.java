package com.elster.jupiter.tasks.impl;

import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.tasks.TaskLogEntry;
import com.elster.jupiter.tasks.TaskOccurrence;

import javax.inject.Inject;
import java.time.Instant;
import java.util.logging.Level;

/**
 * Copyrights EnergyICT
 * Date: 5/11/2014
 * Time: 10:30
 */
public class TaskLogEntryImpl implements TaskLogEntry {

    private Reference<TaskOccurrence> taskOccurrence = ValueReference.absent();
    private int position;
    private Instant timeStamp;
    private String message;
    private String stackTrace;
    private int level;

    @Inject
    TaskLogEntryImpl() {
    }

    TaskLogEntryImpl init(TaskOccurrence occurrence, Instant timeStamp, Level level, String message) {
        this.taskOccurrence.set(occurrence);
        this.timeStamp = timeStamp;
        this.level = level.intValue();
        this.message = message.trim().substring(0, Math.min(message.trim().length(), Table.DESCRIPTION_LENGTH));
        return this;
    }

    @Override
    public TaskOccurrence getTaskOccurrence() {
        return taskOccurrence.get();
    }

    @Override
    public Instant getTimeStamp() {
        return timeStamp;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public Level getLevel() {
        return Level.parse(Integer.toString(level));
    }

}
