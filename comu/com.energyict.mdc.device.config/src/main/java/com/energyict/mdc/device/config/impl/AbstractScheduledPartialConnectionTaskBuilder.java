package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.device.config.DeviceCommunicationConfiguration;
import com.energyict.mdc.device.config.NextExecutionSpecBuilder;
import com.energyict.mdc.device.config.NextExecutionSpecs;
import com.energyict.mdc.device.config.PartialScheduledConnectionTaskBuilder;
import com.energyict.mdc.device.config.TemporalExpression;
import com.energyict.mdc.engine.model.OutboundComPortPool;

/**
 * Copyrights EnergyICT
 * Date: 13/03/14
 * Time: 15:12
 */
public abstract class AbstractScheduledPartialConnectionTaskBuilder<S, U extends PartialScheduledConnectionTask> extends AbstractPartialConnectionTaskBuilder<S, OutboundComPortPool, U> implements PartialScheduledConnectionTaskBuilder<S, U> {

    private NextExecutionSpecs nextExecutionSpecs;

    AbstractScheduledPartialConnectionTaskBuilder(Class<?> selfType, DataModel dataModel, DeviceCommunicationConfiguration configuration) {
        super(selfType, dataModel, configuration);
    }

    @Override
    public NextExecutionSpecBuilder nextExecutionSpec() {
        return new InternalNextExecutionSpecBuilder();
    }

    private class InternalNextExecutionSpecBuilder implements NextExecutionSpecBuilder<S> {

        private TemporalExpression temporalExpression;

        @Override
        public NextExecutionSpecBuilder temporalExpression(TimeDuration frequency, TimeDuration offset) {
            temporalExpression = new TemporalExpression(frequency, offset);
            return this;
        }

        @Override
        public S set() {
            nextExecutionSpecs = dataModel.getInstance(NextExecutionSpecsImpl.class).initialize(temporalExpression);
            return AbstractScheduledPartialConnectionTaskBuilder.this.myself;
        }
    }

    @Override
    void populate(U instance) {
        instance.setComportPool(comPortPool);
        if (nextExecutionSpecs != null) {
            instance.setNextExecutionSpecs(nextExecutionSpecs);
        }

    }
}
