package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.orm.OrmService;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
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

        bind(DeviceConfigurationService.class).to(DeviceConfigurationServiceImpl.class).in(Scopes.SINGLETON);
    }

}