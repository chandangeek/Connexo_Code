package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.device.config.NextExecutionSpecBuilder;
import com.energyict.mdc.device.config.PartialOutboundConnectionTask;
import com.energyict.mdc.device.config.PartialOutboundConnectionTaskBuilder;
import com.energyict.mdc.engine.config.OutboundComPortPool;
import com.energyict.mdc.scheduling.NextExecutionSpecs;
import com.energyict.mdc.scheduling.SchedulingService;
import com.elster.jupiter.time.TemporalExpression;

/**
 * Copyrights EnergyICT
 * Date: 13/03/14
 * Time: 15:12
 */
public abstract class AbstractScheduledPartialConnectionTaskBuilder<S, U extends PartialOutboundConnectionTask> extends AbstractPartialConnectionTaskBuilder<S, OutboundComPortPool, U> implements PartialOutboundConnectionTaskBuilder<S, U> {

    private final SchedulingService schedulingService;
    private NextExecutionSpecs nextExecutionSpecs;
    private TimeDuration retryDelay;

    AbstractScheduledPartialConnectionTaskBuilder(Class<?> selfType, DataModel dataModel, DeviceCommunicationConfigurationImpl configuration, SchedulingService schedulingService, EventService eventService) {
        super(eventService, selfType, dataModel, configuration);
        this.schedulingService = schedulingService;
    }

    @Override
    public NextExecutionSpecBuilder<S> nextExecutionSpec() {
        return new InternalNextExecutionSpecBuilder();
    }

    @Override
    public S rescheduleDelay(TimeDuration duration) {
        retryDelay = duration;
        return myself;
    }

    private class InternalNextExecutionSpecBuilder implements NextExecutionSpecBuilder<S> {

        private TemporalExpression temporalExpression;

        @Override
        public NextExecutionSpecBuilder temporalExpression(TimeDuration frequency, TimeDuration offset) {
            temporalExpression = new TemporalExpression(frequency, offset);
            return this;
        }

        @Override
        public NextExecutionSpecBuilder temporalExpression(TimeDuration frequency) {
            temporalExpression = new TemporalExpression(frequency);
            return this;
        }

        @Override
        public S set() {
            nextExecutionSpecs = schedulingService.newNextExecutionSpecs(temporalExpression);
            return AbstractScheduledPartialConnectionTaskBuilder.this.myself;
        }
    }

    @Override
    void populate(U instance) {
        instance.setComportPool(comPortPool);
        if (nextExecutionSpecs != null) {
            nextExecutionSpecs.save();
            instance.setNextExecutionSpecs(nextExecutionSpecs);
        }
        instance.setRescheduleRetryDelay(retryDelay);
    }
}
