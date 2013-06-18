package com.elster.jupiter.tasks.impl;

import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.util.time.Clock;

import java.util.Date;

class RecurrentTaskImpl implements RecurrentTask {

    private long id;
    private String cronString;
    private Date nextExecution;
    private String payload;
    private String destination;

    RecurrentTaskImpl(String name, CronExpression cronExpression, String destination, String payload) {
        this.destination = destination;
        this.payload = payload;
        this.cronString = cronExpression.toString();
    }

    @Override
    public long getId() {
        //TODO automatically generated method body, provide implementation.
        return 0;
    }

    @Override
    public void updateNextExecution(Clock clock) {

    }

    @Override
    public String getDestination() {
        return destination;
    }

    @Override
    public String getPayLoad() {
        return payload;
    }

    @Override
    public Date getNextExecution() {
        return nextExecution;
    }

    @Override
    public TaskOccurrence createTaskOccurrence(Clock clock) {
        //TODO automatically generated method body, provide implementation.
        return null;
    }

    @Override
    public void save() {
        //TODO automatically generated method body, provide implementation.

    }

    @Override
    public void delete() {
        //TODO automatically generated method body, provide implementation.

    }


    @Override
    public String getName() {
        //TODO automatically generated method body, provide implementation.
        return null;
    }
}
