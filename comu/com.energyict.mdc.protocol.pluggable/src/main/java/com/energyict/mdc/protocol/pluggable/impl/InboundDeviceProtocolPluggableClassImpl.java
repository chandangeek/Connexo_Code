package com.energyict.mdc.protocol.pluggable.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.dynamic.PropertySpec;
import com.energyict.mdc.pluggable.PluggableClass;
import com.energyict.mdc.protocol.api.inbound.InboundDeviceProtocol;
import com.energyict.mdc.protocol.api.services.InboundDeviceProtocolService;
import com.energyict.mdc.protocol.pluggable.InboundDeviceProtocolPluggableClass;

import javax.inject.Inject;
import java.util.List;

/**
 * Defines a PluggableClass based on a {@link InboundDeviceProtocol}.
 * <p/>
 * Copyrights EnergyICT
 * Date: 5/11/12
 * Time: 10:15
 */
public final class InboundDeviceProtocolPluggableClassImpl extends PluggableClassWrapper<InboundDeviceProtocol> implements InboundDeviceProtocolPluggableClass {

    private InboundDeviceProtocolService inboundDeviceProtocolService;

    @Inject
    public InboundDeviceProtocolPluggableClassImpl(EventService eventService, Thesaurus thesaurus, InboundDeviceProtocolService inboundDeviceProtocolService) {
        super(eventService, thesaurus);
        this.inboundDeviceProtocolService = inboundDeviceProtocolService;
    }

    static InboundDeviceProtocolPluggableClassImpl from (DataModel dataModel, PluggableClass pluggableClass) {
        return dataModel.getInstance(InboundDeviceProtocolPluggableClassImpl.class).initializeFrom(pluggableClass);
    }

    InboundDeviceProtocolPluggableClassImpl initializeFrom (PluggableClass pluggableClass) {
        this.setPluggableClass(pluggableClass);
        return this;
    }

    @Override
    protected Discriminator discriminator() {
        return Discriminator.DISCOVERYPROTOCOL;
    }

    @Override
    protected void validateLicense() {
        // Nothing to validate for inbound device protocols
    }

    @Override
    protected InboundDeviceProtocol newInstance(PluggableClass pluggableClass) {
        return this.inboundDeviceProtocolService.createInboundDeviceProtocolFor(pluggableClass);
    }

    @Override
    public TypedProperties getProperties(List<PropertySpec> propertySpecs) {
        return super.getProperties(propertySpecs);
    }

    @Override
    public String getVersion() {
        return super.getVersion();
    }

    @Override
    public InboundDeviceProtocol getInboundDeviceProtocol () {
        InboundDeviceProtocol inboundDeviceProtocol = this.newInstance();
        inboundDeviceProtocol.copyProperties(this.getProperties(inboundDeviceProtocol.getPropertySpecs()));
        return inboundDeviceProtocol;
    }

    @Override
    protected CreateEventType createEventType() {
        return CreateEventType.INBOUNDEVICEPROTOCOL;
    }

    @Override
    protected UpdateEventType updateEventType() {
        return UpdateEventType.INBOUNDEVICEPROTOCOL;
    }

    @Override
    protected DeleteEventType deleteEventType() {
        return DeleteEventType.INBOUNDEVICEPROTOCOL;
    }

}