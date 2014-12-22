package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.PartialConnectionInitiationTask;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.scheduling.SchedulingService;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;

import javax.inject.Inject;

/**
 *  Provides an implementation for an {@link PartialConnectionInitiationTask}
 *
 *  @author sva
 * @since 22/01/13 - 14:35
 */
public class PartialConnectionInitiationTaskImpl extends PartialOutboundConnectionTaskImpl implements PartialConnectionInitiationTask {

    @Inject
    PartialConnectionInitiationTaskImpl(DataModel dataModel, EventService eventService, Thesaurus thesaurus, ProtocolPluggableService protocolPluggableService, SchedulingService schedulingService) {
        super(dataModel, eventService, thesaurus, protocolPluggableService, schedulingService);
    }

    static PartialConnectionInitiationTaskImpl from(DataModel dataModel, DeviceConfiguration configuration) {
        return dataModel.getInstance(PartialConnectionInitiationTaskImpl.class).init(configuration);
    }

    private PartialConnectionInitiationTaskImpl init(DeviceConfiguration configuration) {
        setConfiguration(configuration);
        return this;
    }

    @Override
    protected CreateEventType createEventType() {
        return CreateEventType.PARTIAL_CONNECTION_INITIATION_TASK;
    }

    @Override
    protected UpdateEventType updateEventType() {
        return UpdateEventType.PARTIAL_CONNECTION_INITIATION_TASK;
    }

    @Override
    protected ValidateDeleteEventType validateDeleteEventType() {
        return ValidateDeleteEventType.PARTIAL_CONNECTION_INITIATION_TASK;
    }

    @Override
    protected DeleteEventType deleteEventType() {
        return DeleteEventType.PARTIAL_CONNECTION_INITIATION_TASK;
    }

    @Override
    protected void doDelete() {
        dataModel.mapper(PartialConnectionInitiationTaskImpl.class).remove(this);
    }

}
