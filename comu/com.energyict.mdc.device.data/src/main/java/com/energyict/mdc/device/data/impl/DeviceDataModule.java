package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.orm.OrmService;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.DeviceDataService;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

/**
 * Module intended for use by integration tests
 * <p/>
 * Copyrights EnergyICT
 * Date: 26/02/14
 * Time: 11:30
 */
public class DeviceDataModule extends AbstractModule {

    @Override
    protected void configure() {
        requireBinding(OrmService.class);
        requireBinding(EventService.class);
        requireBinding(DeviceConfigurationService.class);

        bind(DeviceDataService.class).to(DeviceDataServiceImpl.class).in(Scopes.SINGLETON);
    }
}
