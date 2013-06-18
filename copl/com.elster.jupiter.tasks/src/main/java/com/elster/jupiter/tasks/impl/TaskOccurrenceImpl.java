package com.elster.jupiter.tasks.impl;

import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.TaskOccurrence;

import java.util.Date;

public class TaskOccurrenceImpl implements TaskOccurrence {

    private long id;
    private long recurrentTaskId;
    private RecurrentTask recurrentTask;
    private Date triggerTime;

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
        //TODO automatically generated method body, provide implementation.

    }
}
