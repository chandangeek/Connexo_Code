package com.elster.jupiter.tasks.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.TaskLogEntry;
import com.elster.jupiter.tasks.TaskOccurrence;

import javax.inject.Inject;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

class TaskOccurrenceImpl implements TaskOccurrence {

    private long id;
    private long recurrentTaskId;
    private RecurrentTask recurrentTask;
    private Instant triggerTime;

    private List<TaskLogEntry> logEntries = new ArrayList<>();

    private final DataModel dataModel;

    @Inject
	TaskOccurrenceImpl(DataModel dataModel) {
        // for persistence
        this.dataModel = dataModel;
    }

    TaskOccurrenceImpl init(RecurrentTask recurrentTask, Instant triggerTime) {
        this.recurrentTask = recurrentTask;
        this.recurrentTaskId = recurrentTask.getId();
        this.triggerTime = triggerTime;
        return this;
    }

    static TaskOccurrenceImpl from(DataModel dataModel, RecurrentTask recurrentTask, Instant triggerTime) {
        return dataModel.getInstance(TaskOccurrenceImpl.class).init(recurrentTask, triggerTime);
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getPayLoad() {
        return getRecurrentTask().getPayLoad();
    }

    @Override
    public Instant getTriggerTime() {
        return triggerTime;
    }

    @Override
    public RecurrentTask getRecurrentTask() {
        if (recurrentTask == null) {
            recurrentTask = dataModel.mapper(RecurrentTask.class).getExisting(recurrentTaskId);
        }
        return recurrentTask;
    }

    @Override
    public void save() {
        if (id == 0) {
            dataModel.mapper(TaskOccurrence.class).persist(this);
        } else {
            dataModel.mapper(TaskOccurrence.class).update(this);
        }
    }

    public void hasRun() {
        ((RecurrentTaskImpl) recurrentTask).updateLastRun(getTriggerTime());
    }

    @Override
    public void log(Level level, Instant timestamp, String message) {
        logEntries.add(dataModel.getInstance(TaskLogEntryImpl.class).init(this, timestamp, level, message));
    }

    @Override
    public List<TaskLogEntry> getLogs() {
        return Collections.unmodifiableList(logEntries);
    }
}
