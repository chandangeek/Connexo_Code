/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.dashboard.impl;

import com.energyict.mdc.dashboard.DashboardService;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.engine.config.EngineConfigurationService;
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
        requireBinding(EngineConfigurationService.class);
        requireBinding(DeviceConfigurationService.class);
        requireBinding(DeviceService.class);
        requireBinding(ProtocolPluggableService.class);

        bind(DashboardService.class).to(DashboardServiceImpl.class);
    }

}