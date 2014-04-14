package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.device.config.DeviceCommunicationConfiguration;
import com.energyict.mdc.device.config.PartialConnectionInitiationTask;
import com.energyict.mdc.device.config.exceptions.DuplicateNameException;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import javax.inject.Inject;

/**
 *  Provides an implementation for an {@link PartialConnectionInitiationTask}
 *
 *  @author sva
 * @since 22/01/13 - 14:35
 */
public class PartialConnectionInitiationTaskImpl extends PartialScheduledConnectionTaskImpl implements PartialConnectionInitiationTask {

    @Inject
    PartialConnectionInitiationTaskImpl(DataModel dataModel, EventService eventService, Thesaurus thesaurus, EngineModelService engineModelService, ProtocolPluggableService protocolPluggableService) {
        super(dataModel, eventService, thesaurus, engineModelService, protocolPluggableService);
    }

    static PartialConnectionInitiationTaskImpl from(DataModel dataModel, DeviceCommunicationConfiguration configuration) {
        return dataModel.getInstance(PartialConnectionInitiationTaskImpl.class).init(configuration);
    }

    private PartialConnectionInitiationTaskImpl init(DeviceCommunicationConfiguration configuration) {
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
    protected DeleteEventType deleteEventType() {
        return DeleteEventType.PARTIAL_CONNECTION_INITIATION_TASK;
    }

    @Override
    protected void doDelete() {
        dataModel.mapper(PartialConnectionInitiationTaskImpl.class).remove(this);
    }

}
