package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.config.PartialInboundConnectionTask;
import com.energyict.mdc.device.config.PartialInboundConnectionTaskBuilder;
import com.energyict.mdc.engine.config.InboundComPortPool;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;

import javax.inject.Inject;

/**
 * Provides an implementation for the {@link PartialInboundConnectionTask} interface.
 *
 * @author sva
 * @since 21/01/13 - 16:43
 */
@ConnectionTypeDirectionValidForConnectionTask(groups = {Save.Create.class, Save.Update.class}, direction = ConnectionType.Direction.INBOUND)
public class PartialInboundConnectionTaskImpl extends PartialConnectionTaskImpl implements PartialInboundConnectionTask {

    @Inject
    PartialInboundConnectionTaskImpl(DataModel dataModel, EventService eventService, Thesaurus thesaurus, ProtocolPluggableService protocolPluggableService) {
        super(dataModel, eventService, thesaurus, protocolPluggableService);
    }

    static PartialInboundConnectionTaskImpl from(DataModel dataModel, DeviceConfiguration configuration) {
        return dataModel.getInstance(PartialInboundConnectionTaskImpl.class).init(configuration);
    }

    private PartialInboundConnectionTaskImpl init(DeviceConfiguration configuration) {
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
    protected ValidateDeleteEventType validateDeleteEventType() {
        return ValidateDeleteEventType.PARTIAL_INBOUND_CONNECTION_TASK;
    }

    @Override
    protected final DeleteEventType deleteEventType() {
        return DeleteEventType.PARTIAL_INBOUND_CONNECTION_TASK;
    }

    @Override
    protected void doDelete() {
        this.getDataMapper().remove(this);
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
    public PartialConnectionTask cloneForDeviceConfig(DeviceConfiguration deviceConfiguration) {
        PartialInboundConnectionTaskBuilder builder = deviceConfiguration.newPartialInboundConnectionTask(getName(), getPluggableClass());
        builder.asDefault(isDefault());
        getProperties().stream().forEach(partialConnectionTaskProperty -> builder.addProperty(partialConnectionTaskProperty.getName(), partialConnectionTaskProperty.getValue()));
        builder.comPortPool(getComPortPool());
        return builder.build();
    }
}
