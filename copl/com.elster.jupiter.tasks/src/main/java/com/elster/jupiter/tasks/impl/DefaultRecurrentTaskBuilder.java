package com.elster.jupiter.tasks.impl;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.RecurrentTaskBuilder;
import com.elster.jupiter.util.cron.CronExpressionParser;

/**
 * RecurrentTaskBuilder implementation that builds instances of RecurrentTaskImpl
 */
class DefaultRecurrentTaskBuilder implements RecurrentTaskBuilder {

    private final CronExpressionParser cronExpressionParser;

    private String cronString;
    private String name;
    private String payload;
    private DestinationSpec destination;
    private boolean scheduleImmediately;
    private final DataModel dataModel;

    public DefaultRecurrentTaskBuilder(DataModel dataModel, CronExpressionParser cronExpressionParser) {
        this.dataModel = dataModel;
        this.cronExpressionParser = cronExpressionParser;
    }

    @Override
    public RecurrentTaskBuilder setCronExpression(String expression) {
        cronString = expression;
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
    public RecurrentTaskBuilder scheduleImmediately() {
        scheduleImmediately = true;
        return this;
    }

    @Override
    public RecurrentTask build() {
        RecurrentTaskImpl recurrentTask = RecurrentTaskImpl.from(dataModel, name, cronExpressionParser.parse(cronString), destination, payload);
        if (scheduleImmediately) {
            recurrentTask.updateNextExecution();
        }
        return recurrentTask;
    }
}
