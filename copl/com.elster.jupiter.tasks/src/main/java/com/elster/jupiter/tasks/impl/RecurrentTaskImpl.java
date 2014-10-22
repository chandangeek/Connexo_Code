package com.elster.jupiter.tasks.impl;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.util.time.ScheduleExpression;
import com.elster.jupiter.util.time.ScheduleExpressionParser;

import javax.inject.Inject;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

class RecurrentTaskImpl implements RecurrentTask {

    private long id;
    private String name;
    private transient ScheduleExpression scheduleExpression;
    private String cronString;
    private Instant nextExecution;
    private String payload;
    private String destination;
    private Instant lastRun;
    private transient DestinationSpec destinationSpec;

    private final Clock clock;
    private final ScheduleExpressionParser scheduleExpressionParser;
    private final MessageService messageService;
    private final DataModel dataModel;

    @Inject
	RecurrentTaskImpl(DataModel dataModel, ScheduleExpressionParser scheduleExpressionParser, MessageService messageService, Clock clock) {
        this.dataModel = dataModel;
        this.scheduleExpressionParser = scheduleExpressionParser;
        this.messageService = messageService;
        // for persistence
        this.clock = clock;
    }

    RecurrentTaskImpl init(String name, ScheduleExpression scheduleExpression, DestinationSpec destinationSpec, String payload) {
        this.destinationSpec = destinationSpec;
        this.destination = destinationSpec.getName();
        this.payload = payload;
        this.cronString = scheduleExpression.encoded();
        this.name = name;
        this.scheduleExpression = scheduleExpression;
        return this;
    }

    static RecurrentTaskImpl from(DataModel dataModel, String name, ScheduleExpression scheduleExpression, DestinationSpec destinationSpec, String payload) {
        return dataModel.getInstance(RecurrentTaskImpl.class).init(name, scheduleExpression, destinationSpec, payload);
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
        ZonedDateTime now = ZonedDateTime.ofInstant(clock.instant(), ZoneId.systemDefault());
        ZonedDateTime nextOccurrence = getScheduleExpression().nextOccurrence(now);
        nextExecution = nextOccurrence.toInstant();
    }

    private ScheduleExpression getScheduleExpression() {
        if (scheduleExpression == null) {
            scheduleExpression = scheduleExpressionParser.parse(cronString).get();
        }
        return scheduleExpression;
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
        TaskOccurrence occurrence = TaskOccurrenceImpl.from(dataModel, this, nextExecution);
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

    @Override
    public Optional<Instant> getLastRun() {
        return Optional.ofNullable(lastRun);
    }

    void updateLastRun(Instant triggerTime) {
        lastRun = triggerTime;
        save();
    }
}
