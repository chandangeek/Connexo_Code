/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.time.TemporalExpression;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.comserver.OutboundComPortPool;
import com.energyict.mdc.common.device.config.PartialOutboundConnectionTask;
import com.energyict.mdc.common.protocol.ConnectionType;
import com.energyict.mdc.common.rest.MinTimeDuration;
import com.energyict.mdc.common.scheduling.NextExecutionSpecs;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.scheduling.SchedulingService;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlTransient;

/**
 *  Provides an implementation for an {@link PartialOutboundConnectionTask}
 *
 * @author sva
 * @since 22/01/13 - 11:52
 */
@ConnectionTypeDirectionValidForConnectionTask(groups = {Save.Create.class, Save.Update.class}, direction = ConnectionType.ConnectionTypeDirection.OUTBOUND)
@DeviceConfigurationMustBeDirectlyAddressable(groups = {Save.Create.class, Save.Update.class})
abstract class PartialOutboundConnectionTaskImpl extends PartialConnectionTaskImpl implements PartialOutboundConnectionTask {

    enum Fields {
        NEXT_EXECUTION_SPECS("nextExecutionSpecs");
        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        String fieldName() {
            return javaFieldName;
        }
    }

    private SchedulingService schedulingService;

    @Valid
    private Reference<NextExecutionSpecs> nextExecutionSpecs = ValueReference.absent();

    /**
     * Defines the delay to wait before retrying when this connectionTask failed.
     */
    @NotNull(groups = {Save.Create.class, Save.Update.class}, message = '{' + MessageSeeds.Keys.RETRY_DELAY_MUST_BE_HIGHER + '}')
    @MinTimeDuration(value = 60, groups = {Save.Create.class, Save.Update.class}, message = '{' + MessageSeeds.Keys.RETRY_DELAY_MUST_BE_HIGHER + '}')
    private TimeDuration rescheduleRetryDelay;

    public PartialOutboundConnectionTaskImpl() {
        super();
    }

    PartialOutboundConnectionTaskImpl(DataModel dataModel, EventService eventService, Thesaurus thesaurus, ProtocolPluggableService protocolPluggableService, SchedulingService schedulingService) {
        super(dataModel, eventService, thesaurus, protocolPluggableService);
        this.schedulingService = schedulingService;
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
    @XmlTransient
    public TemporalExpression getTemporalExpression() {
        return this.nextExecutionSpecs.get().getTemporalExpression();
    }

    @Override
    public void setTemporalExpression(TemporalExpression temporalExpression) {
        if (!this.nextExecutionSpecs.isPresent()) {
            NextExecutionSpecs newNextExecutionSpecs = schedulingService.newNextExecutionSpecs(temporalExpression);
            this.nextExecutionSpecs.set(newNextExecutionSpecs);
        } else  {
            this.nextExecutionSpecs.get().setTemporalExpression(temporalExpression);
            this.nextExecutionSpecs.get().update();
        }
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
        return CreateEventType.PARTIAL_SCHEDULED_CONNECTION_TASK;
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
