package com.elster.jupiter.tasks.impl;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.RecurrentTaskBuilder;
import com.elster.jupiter.util.time.ScheduleExpression;
import com.elster.jupiter.util.time.ScheduleExpressionParser;

import java.time.Instant;

/**
 * RecurrentTaskBuilder implementation that builds instances of RecurrentTaskImpl
 */
class DefaultRecurrentTaskBuilder implements RecurrentTaskBuilder {

    private final ScheduleExpressionParser scheduleExpressionParser;

    private String cronString;
    private ScheduleExpression scheduleExpression;
    private String name;
    private String payload;
    private DestinationSpec destination;
    private boolean scheduleImmediately;
    private Instant firstExecution;
    private final DataModel dataModel;

    public DefaultRecurrentTaskBuilder(DataModel dataModel, ScheduleExpressionParser scheduleExpressionParser) {
        this.dataModel = dataModel;
        this.scheduleExpressionParser = scheduleExpressionParser;
    }

    @Override
    public RecurrentTaskBuilder setScheduleExpressionString(String expression) {
        cronString = expression;
        scheduleExpression = scheduleExpressionParser.parse(cronString).orElseThrow(IllegalArgumentException::new);
        return this;
    }

    @Override
    public RecurrentTaskBuilder setScheduleExpression(ScheduleExpression scheduleExpression) {
        cronString = scheduleExpression.encoded();
        this.scheduleExpression = scheduleExpression;
        return this;
    }

    @Override
    public RecurrentTaskBuilder setDestination(DestinationSpec destination) {
        this.destination = destination;
        return this;
    }

    @Override
    public RecurrentTaskBuilder setPayLoad(String payLoad) {
        this.payload = payLoad;
        return this;
    }

    @Override
    public RecurrentTaskBuilder setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public RecurrentTaskBuilder scheduleImmediately(boolean value) {
        scheduleImmediately = value;
        return this;
    }

    @Override
    public RecurrentTaskBuilder setFirstExecution(Instant instant) {
        firstExecution = instant;
        return this;
    }

    @Override
    public RecurrentTask build() {
        RecurrentTaskImpl recurrentTask = RecurrentTaskImpl.from(dataModel, name, scheduleExpression, destination, payload);
        if (firstExecution != null) {
            recurrentTask.setNextExecution(firstExecution);
        } else if (scheduleImmediately) {
            recurrentTask.updateNextExecution();
        }
        recurrentTask.save();
        return recurrentTask;
    }
}
