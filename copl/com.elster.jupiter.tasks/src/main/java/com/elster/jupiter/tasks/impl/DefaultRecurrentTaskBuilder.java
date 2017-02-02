package com.elster.jupiter.tasks.impl;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.RecurrentTaskBuilder;
import com.elster.jupiter.tasks.RecurrentTaskBuilder.RecurrentTaskBuilderDestinationSetter;
import com.elster.jupiter.tasks.RecurrentTaskBuilder.RecurrentTaskBuilderFinisher;
import com.elster.jupiter.tasks.RecurrentTaskBuilder.RecurrentTaskBuilderNameSetter;
import com.elster.jupiter.tasks.RecurrentTaskBuilder.RecurrentTaskBuilderPayloadSetter;
import com.elster.jupiter.tasks.RecurrentTaskBuilder.RecurrentTaskBuilderScheduleSetter;
import com.elster.jupiter.util.time.ScheduleExpression;
import com.elster.jupiter.util.time.ScheduleExpressionParser;

import java.time.Instant;
import java.util.logging.Level;

/**
 * RecurrentTaskBuilder implementation that builds instances of RecurrentTaskImpl
 */
class DefaultRecurrentTaskBuilder implements RecurrentTaskBuilder, RecurrentTaskBuilderNameSetter, RecurrentTaskBuilderFinisher, RecurrentTaskBuilderScheduleSetter,
        RecurrentTaskBuilderDestinationSetter, RecurrentTaskBuilderPayloadSetter {

    private final ScheduleExpressionParser scheduleExpressionParser;

    private String cronString;
    private ScheduleExpression scheduleExpression;
    private String name;
    private String application;
    private String payload;
    private DestinationSpec destination;
    private boolean scheduleImmediately;
    private Instant firstExecution;
    private final DataModel dataModel;
    private int logLevel = Level.WARNING.intValue();

    @Override
    public RecurrentTaskBuilderNameSetter setApplication(String application) {
        this.application = application;
        return this;
    }

    public DefaultRecurrentTaskBuilder(DataModel dataModel, ScheduleExpressionParser scheduleExpressionParser) {
        this.dataModel = dataModel;
        this.scheduleExpressionParser = scheduleExpressionParser;
    }

    @Override
    public RecurrentTaskBuilderDestinationSetter setScheduleExpressionString(String expression) {
        cronString = expression;
        scheduleExpression = scheduleExpressionParser.parse(cronString).orElseThrow(IllegalArgumentException::new);
        return this;
    }

    @Override
    public RecurrentTaskBuilderDestinationSetter setScheduleExpression(ScheduleExpression scheduleExpression) {
        cronString = scheduleExpression.encoded();
        this.scheduleExpression = scheduleExpression;
        return this;
    }

    @Override
    public RecurrentTaskBuilderPayloadSetter setDestination(DestinationSpec destination) {
        this.destination = destination;
        return this;
    }

    @Override
    public RecurrentTaskBuilderFinisher setPayLoad(String payLoad) {
        this.payload = payLoad;
        return this;
    }

    @Override
    public RecurrentTaskBuilderScheduleSetter setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public RecurrentTaskBuilderFinisher scheduleImmediately(boolean value) {
        scheduleImmediately = value;
        return this;
    }

    @Override
    public RecurrentTaskBuilderFinisher setFirstExecution(Instant instant) {
        firstExecution = instant;
        return this;
    }

    @Override
    public RecurrentTaskBuilderFinisher setLogLevel(int level) {
        logLevel = level;
        return this;
    }

    @Override
    public RecurrentTask build() {
        RecurrentTaskImpl recurrentTask = RecurrentTaskImpl.from(dataModel, application, name, scheduleExpression, destination, payload, logLevel);
        if (firstExecution != null) {
            recurrentTask.setNextExecution(firstExecution);
        } else if (scheduleImmediately) {
            recurrentTask.updateNextExecution();
        }
        recurrentTask.save();
        return recurrentTask;
    }
}
