package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-30 (15:49)
 */
public class DeviceConfigurationModule extends AbstractModule {

    @Override
    protected void configure() {
        requireBinding(DataModel.class);
        requireBinding(OrmService.class);
        requireBinding(EventService.class);

        bind(DeviceConfigurationService.class).to(DeviceConfigurationServiceImpl.class).in(Scopes.SINGLETON);
    }

}