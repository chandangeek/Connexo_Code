package com.energyict.protocols.mdc.services.impl;

import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.io.SocketService;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.api.services.ConnectionTypeService;
import com.energyict.mdc.protocol.api.services.DeviceCacheMarshallingService;
import com.energyict.mdc.protocol.api.services.DeviceProtocolMessageService;
import com.energyict.mdc.protocol.api.services.DeviceProtocolSecurityService;
import com.energyict.mdc.protocol.api.services.DeviceProtocolService;
import com.energyict.mdc.protocol.api.services.InboundDeviceProtocolService;
import com.energyict.mdc.protocol.api.services.LicensedProtocolService;

import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.orm.OrmService;
import com.energyict.protocols.impl.ConnectionTypeServiceImpl;
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
        requireBinding(IssueService.class);
        requireBinding(Clock.class);
        requireBinding(NlsService.class);
        requireBinding(PropertySpecService.class);
        requireBinding(SocketService.class);

        bind(ConnectionTypeService.class).to(ConnectionTypeServiceImpl.class).in(Scopes.SINGLETON);
        bind(DeviceProtocolMessageService.class).to(DeviceProtocolMessageServiceImpl.class).in(Scopes.SINGLETON);
        bind(DeviceProtocolSecurityService.class).to(DeviceProtocolSecurityServiceImpl.class).in(Scopes.SINGLETON);
        bind(DeviceProtocolService.class).to(DeviceProtocolServiceImpl.class).in(Scopes.SINGLETON);
        bind(InboundDeviceProtocolService.class).to(InboundDeviceProtocolServiceImpl.class).in(Scopes.SINGLETON);
        bind(LicensedProtocolService.class).to(LicensedProtocolServiceImpl.class).in(Scopes.SINGLETON);
        bind(DeviceCacheMarshallingService.class).to(DeviceCacheMarshallingServiceImpl.class).in(Scopes.SINGLETON);
        bind(PropertySpecServiceDependency.class).in(Scopes.SINGLETON);
    }

}