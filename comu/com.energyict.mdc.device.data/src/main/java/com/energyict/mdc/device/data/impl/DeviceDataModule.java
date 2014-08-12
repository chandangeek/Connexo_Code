package com.energyict.mdc.device.data.impl;

import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.device.data.impl.security.SecurityPropertyService;
import com.energyict.mdc.device.data.impl.security.SecurityPropertyServiceImpl;
import com.energyict.mdc.dynamic.relation.RelationService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.scheduling.SchedulingService;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.time.Clock;
import com.elster.jupiter.validation.ValidationService;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

/**
 * Module intended for use by integration tests.
 * <p/>
 * Copyrights EnergyICT
 * Date: 26/02/14
 * Time: 11:30
 */
public class DeviceDataModule extends AbstractModule {

    @Override
    protected void configure() {
        requireBinding(OrmService.class);
        requireBinding(TransactionService.class);
        requireBinding(EventService.class);
        requireBinding(DeviceConfigurationService.class);
        requireBinding(MeteringService.class);
        requireBinding(ValidationService.class);
        requireBinding(Clock.class);
        requireBinding(RelationService.class);
        requireBinding(ProtocolPluggableService.class);
        requireBinding(SchedulingService.class);

        bind(SecurityPropertyService.class).to(SecurityPropertyServiceImpl.class).in(Scopes.SINGLETON);
        bind(DeviceDataService.class).to(DeviceDataServiceImpl.class).in(Scopes.SINGLETON);
    }

}