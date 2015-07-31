package com.energyict.mdc.device.data.importers.impl;

import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.users.UserService;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.imp.DeviceImportService;
import com.energyict.mdc.device.data.importers.impl.devices.shipment.DeviceShipmentImporterFactory;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleService;
import com.energyict.mdc.device.topology.TopologyService;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

import java.time.Clock;

public class DeviceDataImportersModule extends AbstractModule {
    @Override
    protected void configure() {
        requireBinding(PropertySpecService.class);
        requireBinding(Thesaurus.class);
        requireBinding(DeviceConfigurationService.class);
        requireBinding(DeviceService.class);
        requireBinding(DeviceImportService.class);
        requireBinding(TopologyService.class);
        requireBinding(MeteringService.class);
        requireBinding(DeviceLifeCycleService.class);
        requireBinding(FiniteStateMachineService.class);
        requireBinding(UserService.class);
        requireBinding(ThreadPrincipalService.class);
        requireBinding(Clock.class);

        bind(DeviceDataImporterContext.class).to(DeviceDataImporterContext.class).in(Scopes.SINGLETON);
        bind(DeviceDataImporterMessageHandler.class).to(DeviceDataImporterMessageHandler.class).in(Scopes.SINGLETON);
        bind(DeviceShipmentImporterFactory.class).to(DeviceShipmentImporterFactory.class).in(Scopes.SINGLETON);
        //TODO bind all factories
    }
}
