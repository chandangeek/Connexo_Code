package com.elster.jupiter.tasks.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.TaskOccurrence;

import javax.inject.Inject;

import java.time.Instant;

class TaskOccurrenceImpl implements TaskOccurrence {

    private long id;
    private long recurrentTaskId;
    private RecurrentTask recurrentTask;
    private Instant triggerTime;

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
}
