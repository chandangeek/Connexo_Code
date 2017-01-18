package com.energyict.protocols.mdc.services.impl;

import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.transaction.TransactionService;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.io.SerialComponentService;
import com.energyict.mdc.io.SocketService;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.protocol.api.DeviceMessageFileService;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.api.impl.device.messages.DeviceMessageSpecificationServiceImpl;
import com.energyict.mdc.protocol.api.services.ConnectionTypeService;
import com.energyict.mdc.protocol.api.services.DeviceCacheMarshallingService;
import com.energyict.mdc.protocol.api.services.DeviceProtocolMessageService;
import com.energyict.mdc.protocol.api.services.DeviceProtocolSecurityService;
import com.energyict.mdc.protocol.api.services.DeviceProtocolService;
import com.energyict.mdc.protocol.api.services.IdentificationService;
import com.energyict.mdc.protocol.api.services.InboundDeviceProtocolService;
import com.energyict.mdc.protocol.api.services.LicensedProtocolService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
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
        requireBinding(com.elster.jupiter.properties.PropertySpecService.class);
        requireBinding(PropertySpecService.class);
        requireBinding(SocketService.class);
        requireBinding(MeteringService.class);
        requireBinding(TopologyService.class);
        requireBinding(SerialComponentService.class);
        requireBinding(MdcReadingTypeUtilService.class);
        requireBinding(IdentificationService.class);
        requireBinding(CollectedDataFactory.class);
        requireBinding(CalendarService.class);
        requireBinding(DeviceConfigurationService.class);
        requireBinding(DeviceMessageFileService.class);
        requireBinding(TransactionService.class);
        requireBinding(ProtocolPluggableService.class);

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