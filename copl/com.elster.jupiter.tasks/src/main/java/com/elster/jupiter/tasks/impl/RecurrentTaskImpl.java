package com.elster.jupiter.tasks.impl;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.util.cron.CronExpression;
import com.elster.jupiter.util.cron.CronExpressionParser;
import java.time.Clock;

import javax.inject.Inject;

import java.time.Instant;

class RecurrentTaskImpl implements RecurrentTask {

    private long id;
    private String name;
    private transient CronExpression cronExpression;
    private String cronString;
    private Instant nextExecution;
    private String payload;
    private String destination;
    private transient DestinationSpec destinationSpec;

    private final Clock clock;
    private final CronExpressionParser cronExpressionParser;
    private final MessageService messageService;
    private final DataModel dataModel;

    @Inject
	RecurrentTaskImpl(DataModel dataModel, CronExpressionParser cronExpressionParser, MessageService messageService, Clock clock) {
        this.dataModel = dataModel;
        this.cronExpressionParser = cronExpressionParser;
        this.messageService = messageService;
        // for persistence
        this.clock = clock;
    }

    RecurrentTaskImpl init(String name, CronExpression cronExpression, DestinationSpec destinationSpec, String payload) {
        this.destinationSpec = destinationSpec;
        this.destination = destinationSpec.getName();
        this.payload = payload;
        this.cronString = cronExpression.toString();
        this.name = name;
        this.cronExpression = cronExpression;
        return this;
    }

    static RecurrentTaskImpl from(DataModel dataModel, String name, CronExpression cronExpression, DestinationSpec destinationSpec, String payload) {
        return dataModel.getInstance(RecurrentTaskImpl.class).init(name, cronExpression, destinationSpec, payload);
    }

    @Override
    public long getId() {
        return id;
    }

    void setId(long id) {
        this.id = id;
    }

    @Override
    public void updateNextExecution() {
        nextExecution = getCronExpression().nextAfter(clock.instant());
    }

    private CronExpression getCronExpression() {
        if (cronExpression == null) {
            cronExpression = cronExpressionParser.parse(cronString);
        }
        return cronExpression;
    }

    @Override
    public DestinationSpec getDestination() {
        if (destinationSpec == null) {
            destinationSpec = messageService.getDestinationSpec(destination).get();
        }
        return destinationSpec;
    }

    @Override
    public String getPayLoad() {
        return payload;
    }

    @Override
    public Instant getNextExecution() {
        return nextExecution;
    }

    @Override
    public TaskOccurrence createTaskOccurrence() {
        TaskOccurrence occurrence = TaskOccurrenceImpl.from(dataModel, this, clock.instant());
        occurrence.save();
        return occurrence;
    }

    @Override
    public void save() {
        if (id == 0) {
            dataModel.mapper(RecurrentTask.class).persist(this);
        } else {
            dataModel.mapper(RecurrentTask.class).update(this);
        }

    }

    @Override
    public void delete() {
        dataModel.mapper(RecurrentTask.class).remove(this);
    }

    @Override
    public String getName() {
        return name;
    }

    public void setNextExecution(Instant nextExecution) {
        this.nextExecution = nextExecution;
    }

    @Override
    public void resume() {
        updateNextExecution();
        save();
    }

    @Override
    public void suspend() {
        this.nextExecution = null;
        save();
    }
}
