package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.device.config.NextExecutionSpecs;
import com.energyict.mdc.device.config.PartialScheduledConnectionTask;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.engine.model.OutboundComPortPool;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 *  Provides an implementation for an {@link PartialScheduledConnectionTask}
 *
 * @author sva
 * @since 22/01/13 - 11:52
 */
@CheckMinimumRescheduleDelay(groups = {Save.Create.class, Save.Update.class}, minimumRescheduleDelayInSeconds = 60)
public abstract class PartialScheduledConnectionTaskImpl extends PartialConnectionTaskImpl implements PartialScheduledConnectionTask {

    private Reference<NextExecutionSpecs> nextExecutionSpecs = ValueReference.absent();

    /**
     * Defines the delay to wait before retrying when this connectionTask failed
     */
    private TimeDuration rescheduleRetryDelay;

    PartialScheduledConnectionTaskImpl(DataModel dataModel, EventService eventService, Thesaurus thesaurus, EngineModelService engineModelService, ProtocolPluggableService protocolPluggableService) {
        super(dataModel, eventService, thesaurus, engineModelService, protocolPluggableService);
    }

    @Override
    public TimeDuration getRescheduleDelay() {
        return rescheduleRetryDelay;
    }

    @Override
    public NextExecutionSpecs getNextExecutionSpecs() {
        return this.nextExecutionSpecs.orNull();
    }

    @Override
    public OutboundComPortPool getComPortPool () {
        return (OutboundComPortPool) super.getComPortPool();
    }

    @Override
    public void setComportPool(OutboundComPortPool comPortPool) {
        doSetComportPool(comPortPool);
    }

    @Override
    protected Class<OutboundComPortPool> expectedComPortPoolType () {
        return OutboundComPortPool.class;
    }

    @Override
    public void setNextExecutionSpecs(NextExecutionSpecs nextExecutionSpec) {
        nextExecutionSpecs.set(nextExecutionSpec);
    }

    @Override
    public void setRescheduleRetryDelay(TimeDuration rescheduleRetryDelay) {
        this.rescheduleRetryDelay = rescheduleRetryDelay;
    }

    public static class MinimumRescheduleDelayValidator implements ConstraintValidator<CheckMinimumRescheduleDelay, PartialScheduledConnectionTaskImpl> {

        private int minimalDelayInSeconds;

        @Inject
        public MinimumRescheduleDelayValidator() {
        }

        @Override
        public void initialize(CheckMinimumRescheduleDelay constraintAnnotation) {
            minimalDelayInSeconds = constraintAnnotation.minimumRescheduleDelayInSeconds();
        }

        @Override
        public boolean isValid(PartialScheduledConnectionTaskImpl value, ConstraintValidatorContext context) {
            TimeDuration rescheduleDelay = value.getRescheduleDelay();
            return rescheduleDelay!= null && rescheduleDelay.getSeconds() >= minimalDelayInSeconds;
        }
    }
}
