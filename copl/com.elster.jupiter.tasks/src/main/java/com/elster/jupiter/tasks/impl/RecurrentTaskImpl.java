package com.elster.jupiter.tasks.impl;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.util.cron.CronExpression;
import com.elster.jupiter.util.cron.CronExpressionParser;
import com.elster.jupiter.util.time.Clock;
import com.elster.jupiter.util.time.UtcInstant;

import java.util.Date;

class RecurrentTaskImpl implements RecurrentTask {

    private long id;
    private String name;
    private transient CronExpression cronExpression;
    private String cronString;
    private UtcInstant nextExecution;
    private String payload;
    private String destination;
    private transient DestinationSpec destinationSpec;

    private final Clock clock;
    private final CronExpressionParser cronExpressionParser;
    private final MessageService messageService;
    private final DataModel dataModel;

    @SuppressWarnings("unused")
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
        nextExecution = new UtcInstant(getCronExpression().nextAfter(clock.now()));
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
    public Date getNextExecution() {
        return nextExecution == null ? null : nextExecution.toDate();
    }

    @Override
    public TaskOccurrence createTaskOccurrence() {
        TaskOccurrence occurrence = TaskOccurrenceImpl.from(dataModel, this, clock.now());
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

    public void setNextExecution(Date nextExecution) {
        this.nextExecution = new UtcInstant(nextExecution);
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
