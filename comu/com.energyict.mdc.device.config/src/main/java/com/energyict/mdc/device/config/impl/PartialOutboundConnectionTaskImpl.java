package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.common.rest.MinTimeDuration;
import com.energyict.mdc.device.config.NextExecutionSpecs;
import com.energyict.mdc.device.config.PartialOutboundConnectionTask;
import com.energyict.mdc.device.config.TemporalExpression;
import com.energyict.mdc.device.config.exceptions.MessageSeeds;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.engine.model.OutboundComPortPool;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 *  Provides an implementation for an {@link com.energyict.mdc.device.config.PartialOutboundConnectionTask}
 *
 * @author sva
 * @since 22/01/13 - 11:52
 */
public abstract class PartialOutboundConnectionTaskImpl extends PartialConnectionTaskImpl implements PartialOutboundConnectionTask {

    @Valid
    private Reference<NextExecutionSpecs> nextExecutionSpecs = ValueReference.absent();

    /**
     * Defines the delay to wait before retrying when this connectionTask failed
     */
    @NotNull(groups = {Save.Create.class, Save.Update.class}, message = '{' + MessageSeeds.Constants.UNDER_MINIMUM_RESCHEDULE_DELAY_KEY + '}')
    @MinTimeDuration(value = 60, groups = {Save.Create.class, Save.Update.class}, message = '{' + MessageSeeds.Constants.UNDER_MINIMUM_RESCHEDULE_DELAY_KEY + '}')
    private TimeDuration rescheduleRetryDelay;

    PartialOutboundConnectionTaskImpl(DataModel dataModel, EventService eventService, Thesaurus thesaurus, EngineModelService engineModelService, ProtocolPluggableService protocolPluggableService) {
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
    public TemporalExpression getTemporalExpression() {
        return this.nextExecutionSpecs.get().getTemporalExpression();
    }

    @Override
    public void setTemporalExpression(TemporalExpression temporalExpression) {
        if (!this.nextExecutionSpecs.isPresent()) {
            NextExecutionSpecsImpl instance = dataModel.getInstance(NextExecutionSpecsImpl.class);
            instance.setTemporalExpression(temporalExpression);
            instance.save();
            this.nextExecutionSpecs.set(instance);
        } else {
            this.nextExecutionSpecs.get().setTemporalExpression(temporalExpression);
        }
        this.nextExecutionSpecs.get().save();
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
    protected CreateEventType createEventType() {
        return CreateEventType.PARTIAL_OUTBOUND_CONNECTION_TASK;
    }

    @Override
    protected Class<OutboundComPortPool> expectedComPortPoolType () {
        return OutboundComPortPool.class;
    }

    @Override
    public void setNextExecutionSpecs(NextExecutionSpecs nextExecutionSpec) {
        if (nextExecutionSpec==null) {
            this.nextExecutionSpecs=ValueReference.absent();
        }
        nextExecutionSpecs.set(nextExecutionSpec);
    }

    @Override
    public void setRescheduleRetryDelay(TimeDuration rescheduleRetryDelay) {
        this.rescheduleRetryDelay = rescheduleRetryDelay;
    }

}
