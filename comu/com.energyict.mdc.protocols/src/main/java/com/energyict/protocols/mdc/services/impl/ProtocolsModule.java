package com.energyict.protocols.mdc.services.impl;

import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.util.time.Clock;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.api.services.ConnectionTypeService;
import com.energyict.mdc.protocol.api.services.DeviceProtocolMessageService;
import com.energyict.mdc.protocol.api.services.DeviceProtocolSecurityService;
import com.energyict.mdc.protocol.api.services.DeviceProtocolService;
import com.energyict.mdc.protocol.api.services.HexService;
import com.energyict.mdc.protocol.api.services.InboundDeviceProtocolService;
import com.energyict.mdc.protocol.api.services.LicensedProtocolService;
import com.energyict.protocols.mdc.services.SocketService;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

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

        bind(ConnectionTypeService.class).to(ConnectionTypeServiceImpl.class).in(Scopes.SINGLETON);
        bind(DeviceProtocolMessageService.class).to(DeviceProtocolMessageServiceImpl.class).in(Scopes.SINGLETON);
        bind(DeviceProtocolSecurityService.class).to(DeviceProtocolSecurityServiceImpl.class).in(Scopes.SINGLETON);
        bind(DeviceProtocolService.class).to(DeviceProtocolServiceImpl.class).in(Scopes.SINGLETON);
        bind(InboundDeviceProtocolService.class).to(InboundDeviceProtocolServiceImpl.class).in(Scopes.SINGLETON);
        bind(LicensedProtocolService.class).to(LicensedProtocolServiceImpl.class).in(Scopes.SINGLETON);
        bind(SocketService.class).to(SocketServiceImpl.class).in(Scopes.SINGLETON);
        bind(HexService.class).to(HexServiceImpl.class).in(Scopes.SINGLETON);
    }

}