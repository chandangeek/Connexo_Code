package com.elster.jupiter.tasks.impl;

import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.TaskOccurrence;

import java.util.Date;

public class TaskOccurrenceImpl implements TaskOccurrence {

    private long id;
    private long recurrentTaskId;
    private RecurrentTask recurrentTask;
    private Date triggerTime;

    private TaskOccurrenceImpl() {
        // for persistence
    }

    TaskOccurrenceImpl(RecurrentTask recurrentTask, Date triggerTime) {
        this.recurrentTask = recurrentTask;
        this.recurrentTaskId = recurrentTask.getId();
        this.triggerTime = triggerTime;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getPayLoad() {
        return recurrentTask.getPayLoad();
    }

    @Override
    public Date getTriggerTime() {
        return triggerTime;
    }

    @Override
    public RecurrentTask getRecurrentTask() {
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
