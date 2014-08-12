package com.energyict.mdc.dashboard.impl;

import com.energyict.mdc.dashboard.DashboardService;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import com.google.inject.AbstractModule;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-08-04 (15:29)
 */
public class DashboardModule extends AbstractModule {

    @Override
    protected void configure() {
        requireBinding(EngineModelService.class);
        requireBinding(DeviceConfigurationService.class);
        requireBinding(DeviceDataService.class);
        requireBinding(ProtocolPluggableService.class);

        bind(DashboardService.class).to(DashboardServiceImpl.class);
    }

}