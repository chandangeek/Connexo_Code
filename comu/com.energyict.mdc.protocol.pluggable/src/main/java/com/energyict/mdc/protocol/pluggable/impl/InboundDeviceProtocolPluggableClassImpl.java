/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.pluggable.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.pluggable.PluggableClass;
import com.energyict.mdc.protocol.api.inbound.InboundDeviceProtocol;
import com.energyict.mdc.protocol.pluggable.InboundDeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import javax.inject.Inject;
import java.util.List;

public final class InboundDeviceProtocolPluggableClassImpl extends PluggableClassWrapper<InboundDeviceProtocol> implements InboundDeviceProtocolPluggableClass {

    private final ProtocolPluggableService protocolPluggableService;

    @Inject
    public InboundDeviceProtocolPluggableClassImpl(EventService eventService, Thesaurus thesaurus, ProtocolPluggableService protocolPluggableService) {
        super(eventService, thesaurus);
        this.protocolPluggableService = protocolPluggableService;
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
        return this.protocolPluggableService.createInboundDeviceProtocolFor(pluggableClass);
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