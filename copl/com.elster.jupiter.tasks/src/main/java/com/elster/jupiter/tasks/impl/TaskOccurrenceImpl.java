package com.elster.jupiter.tasks.impl;

import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.util.time.UtcInstant;

import java.util.Date;

class TaskOccurrenceImpl implements TaskOccurrence {

    private long id;
    private long recurrentTaskId;
    private RecurrentTask recurrentTask;
    private UtcInstant triggerTime;

    private TaskOccurrenceImpl() {
        // for persistence
    }

    TaskOccurrenceImpl(RecurrentTask recurrentTask, Date triggerTime) {
        this.recurrentTask = recurrentTask;
        this.recurrentTaskId = recurrentTask.getId();
        this.triggerTime = new UtcInstant(triggerTime);
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
            recurrentTask = Bus.getOrmClient().getRecurrentTaskFactory().getExisting(recurrentTaskId);
        }
        return recurrentTask;
    }

    @Override
    public void save() {
        if (id == 0) {
            Bus.getOrmClient().getTaskOccurrenceFactory().persist(this);
        } else {
            Bus.getOrmClient().getTaskOccurrenceFactory().update(this);
        }
    }
}
