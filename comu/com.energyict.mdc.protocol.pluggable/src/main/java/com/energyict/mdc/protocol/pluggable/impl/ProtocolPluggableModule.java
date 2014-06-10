package com.energyict.mdc.protocol.pluggable.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.orm.OrmService;
import com.energyict.mdc.dynamic.relation.RelationService;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.pluggable.PluggableService;
import com.energyict.mdc.protocol.api.services.ConnectionTypeService;
import com.energyict.mdc.protocol.api.services.DeviceCacheMarshallingService;
import com.energyict.mdc.protocol.api.services.DeviceProtocolService;
import com.energyict.mdc.protocol.api.services.InboundDeviceProtocolService;
import com.energyict.mdc.protocol.api.services.LicensedProtocolService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-12-19 (14:38)
 */
public class ProtocolPluggableModule extends AbstractModule {

    @Override
    protected void configure() {
        requireBinding(OrmService.class);
        requireBinding(EventService.class);
        requireBinding(PluggableService.class);
        requireBinding(RelationService.class);
        requireBinding(DeviceProtocolService.class);
        requireBinding(ConnectionTypeService.class);
        requireBinding(InboundDeviceProtocolService.class);
        requireBinding(LicenseService.class);
        requireBinding(LicensedProtocolService.class);
        requireBinding(IssueService.class);
        requireBinding(DeviceCacheMarshallingService.class);

        bind(ProtocolPluggableService.class).to(ProtocolPluggableServiceImpl.class).in(Scopes.SINGLETON);
    }

}