package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.device.config.DeviceCommunicationConfiguration;
import com.energyict.mdc.device.config.PartialInboundConnectionTask;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.engine.model.InboundComPortPool;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import javax.inject.Inject;

/**
 * Provides an implementation for an {@link PartialInboundConnectionTask}
 *
 * @author sva
 * @since 21/01/13 - 16:43
 */
@ConnectionTypeDirectionValidForConnectionTask(groups = {Save.Create.class, Save.Update.class}, direction = ConnectionType.Direction.INBOUND)
public class PartialInboundConnectionTaskImpl extends PartialConnectionTaskImpl implements PartialInboundConnectionTask {

    @Inject
    PartialInboundConnectionTaskImpl(DataModel dataModel, EventService eventService, Thesaurus thesaurus, EngineModelService engineModelService, ProtocolPluggableService protocolPluggableService) {
        super(dataModel, eventService, thesaurus, engineModelService, protocolPluggableService);
    }

    static PartialInboundConnectionTaskImpl from(DataModel dataModel, DeviceCommunicationConfiguration configuration) {
        return dataModel.getInstance(PartialInboundConnectionTaskImpl.class).init(configuration);
    }

    private PartialInboundConnectionTaskImpl init(DeviceCommunicationConfiguration configuration) {
        setConfiguration(configuration);
        return this;
    }

    @Override
    protected final CreateEventType createEventType() {
        return CreateEventType.PARTIAL_INBOUND_CONNECTION_TASK;
    }

    @Override
    protected final UpdateEventType updateEventType() {
        return UpdateEventType.PARTIAL_INBOUND_CONNECTION_TASK;
    }

    @Override
    protected final DeleteEventType deleteEventType() {
        return DeleteEventType.PARTIAL_INBOUND_CONNECTION_TASK;
    }

    @Override
    protected void doDelete() {
        dataModel.mapper(PartialInboundConnectionTaskImpl.class).remove(this);
    }

    @Override
    public InboundComPortPool getComPortPool () {
        return (InboundComPortPool) super.getComPortPool();
    }

    @Override
    public void setComportPool(InboundComPortPool comPortPool) {
        doSetComportPool(comPortPool);
    }

    @Override
    protected Class<InboundComPortPool> expectedComPortPoolType () {
        return InboundComPortPool.class;
    }

}
