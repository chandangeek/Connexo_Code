package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.pki.PkiService;
import com.elster.jupiter.validation.ValidationService;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.LockService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.scheduling.SchedulingService;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

/**
 * Module intended for use by integration tests
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-30 (15:49)
 */
public class DeviceConfigurationModule extends AbstractModule {

    @Override
    protected void configure() {
        requireBinding(OrmService.class);
        requireBinding(EventService.class);
        requireBinding(MeteringService.class);
        requireBinding(MdcReadingTypeUtilService.class);
        requireBinding(SchedulingService.class);
        requireBinding(ValidationService.class);
        requireBinding(EstimationService.class);
        requireBinding(CalendarService.class);
        requireBinding(PkiService.class);
        bind(DeviceConfigurationService.class).to(ServerDeviceConfigurationService.class).in(Scopes.SINGLETON);
        bind(ServerDeviceConfigurationService.class).to(DeviceConfigurationServiceImpl.class).in(Scopes.SINGLETON);
        bind(LockService.class).to(ServerDeviceConfigurationService.class).in(Scopes.SINGLETON);
    }

}