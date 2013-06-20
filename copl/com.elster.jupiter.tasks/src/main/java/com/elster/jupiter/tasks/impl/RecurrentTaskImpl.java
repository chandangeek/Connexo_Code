package com.elster.jupiter.tasks.impl;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.util.time.Clock;

import java.util.Date;

class RecurrentTaskImpl implements RecurrentTask {

    private long id;
    private String name;
    private transient CronExpression cronExpression;
    private String cronString;
    private Date nextExecution;
    private String payload;
    private String destination;
    private transient DestinationSpec destinationSpec;

    private RecurrentTaskImpl() {
        // for persistence
    }

    RecurrentTaskImpl(String name, CronExpression cronExpression, DestinationSpec destinationSpec, String payload) {
        this.destinationSpec = destinationSpec;
        this.destination = destinationSpec.getName();
        this.payload = payload;
        this.cronString = cronExpression.toString();
        this.name = name;
        this.cronExpression = cronExpression;
    }

    @Override
    public long getId() {
        return id;
    }

    void setId(long id) {
        this.id = id;
    }

    @Override
    public void updateNextExecution(Clock clock) {
        nextExecution = getCronExpression().nextAfter(clock.now());
    }

    private CronExpression getCronExpression() {
        if (cronExpression == null) {
            cronExpression = Bus.getCronExpressionParser().parse(cronString);
        }
        return cronExpression;
    }

    @Override
    public DestinationSpec getDestination() {
        if (destinationSpec == null) {
            destinationSpec = Bus.getMessageService().getDestinationSpec(destination);
        }
        return destinationSpec;
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
        TaskOccurrence occurrence = new TaskOccurrenceImpl(this, clock.now());
        occurrence.save();
        return occurrence;
    }

    @Override
    public void save() {
        if (id == 0) {
            Bus.getOrmClient().getRecurrentTaskFactory().persist(this);
        } else {
            Bus.getOrmClient().getRecurrentTaskFactory().update(this);
        }

    }

    @Override
    public void delete() {
        Bus.getOrmClient().getRecurrentTaskFactory().remove(this);
    }


    @Override
    public String getName() {
        return name;
    }

    public void setNextExecution(Date nextExecution) {
        this.nextExecution = nextExecution;
    }
}
