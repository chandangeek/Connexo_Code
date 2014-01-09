package com.elster.jupiter.tasks.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.util.time.UtcInstant;

import javax.inject.Inject;
import java.util.Date;

class TaskOccurrenceImpl implements TaskOccurrence {

    private long id;
    private long recurrentTaskId;
    private RecurrentTask recurrentTask;
    private UtcInstant triggerTime;

    private final DataModel dataModel;

    @SuppressWarnings("unused")
    @Inject
	TaskOccurrenceImpl(DataModel dataModel) {
        // for persistence
        this.dataModel = dataModel;
    }

    TaskOccurrenceImpl init(RecurrentTask recurrentTask, Date triggerTime) {
        this.recurrentTask = recurrentTask;
        this.recurrentTaskId = recurrentTask.getId();
        this.triggerTime = new UtcInstant(triggerTime);
        return this;
    }

    static TaskOccurrenceImpl from(DataModel dataModel, RecurrentTask recurrentTask, Date triggerTime) {
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
    public Date getTriggerTime() {
        return triggerTime.toDate();
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
