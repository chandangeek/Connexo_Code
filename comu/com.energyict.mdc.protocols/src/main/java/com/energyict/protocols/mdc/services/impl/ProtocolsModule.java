package com.energyict.protocols.mdc.services.impl;


import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.orm.OrmService;
import com.energyict.mdc.io.impl.SocketServiceImpl;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.api.impl.device.messages.DeviceMessageSpecificationServiceImpl;
import com.energyict.mdc.protocol.api.services.ConnectionTypeService;
import com.energyict.mdc.protocol.api.services.DeviceCacheMarshallingService;
import com.energyict.mdc.protocol.api.services.DeviceProtocolMessageService;
import com.energyict.mdc.protocol.api.services.DeviceProtocolSecurityService;
import com.energyict.mdc.protocol.api.services.DeviceProtocolService;
import com.energyict.mdc.protocol.api.services.InboundDeviceProtocolService;
import com.energyict.mdc.protocol.api.services.LicensedProtocolService;
import com.energyict.mdc.upl.io.SocketService;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

import java.time.Clock;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-12-19 (14:38)
 */
public class ProtocolsModule extends AbstractModule {

    @Override
    protected void configure() {
        requireBinding(OrmService.class);
        requireBinding(Clock.class);
        requireBinding(NlsService.class);
        requireBinding(PropertySpecService.class);

        bind(SocketService.class).to(SocketServiceImpl.class).in(Scopes.SINGLETON);
        bind(ConnectionTypeService.class).to(ConnectionTypeServiceImpl.class).in(Scopes.SINGLETON);
        bind(DeviceProtocolMessageService.class).to(DeviceProtocolMessageServiceImpl.class).in(Scopes.SINGLETON);
        bind(DeviceProtocolSecurityService.class).to(DeviceProtocolSecurityServiceImpl.class).in(Scopes.SINGLETON);
        bind(DeviceProtocolService.class).to(DeviceProtocolServiceImpl.class).in(Scopes.SINGLETON);
        bind(InboundDeviceProtocolService.class).to(InboundDeviceProtocolServiceImpl.class).in(Scopes.SINGLETON);
        bind(LicensedProtocolService.class).to(LicensedProtocolServiceImpl.class).in(Scopes.SINGLETON);
        bind(DeviceCacheMarshallingService.class).to(DeviceCacheMarshallingServiceImpl.class).in(Scopes.SINGLETON);
        bind(DeviceMessageSpecificationService.class).to(DeviceMessageSpecificationServiceImpl.class).in(Scopes.SINGLETON);
    }

}