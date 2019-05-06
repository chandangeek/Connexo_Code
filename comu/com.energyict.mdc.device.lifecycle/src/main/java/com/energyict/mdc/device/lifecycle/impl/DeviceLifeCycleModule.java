/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.lifecycle.impl;

import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.UserService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleService;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
import com.energyict.mdc.device.lifecycle.config.DeviceMicroCheckFactory;
import com.energyict.mdc.device.lifecycle.impl.micro.checks.DeviceMicroCheckFactoryImpl;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.device.topology.multielement.MultiElementDeviceService;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

/**
 * Module intended for use by integration tests.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-05-11 (15:50)
 */
public class DeviceLifeCycleModule extends AbstractModule {

    @Override
    protected void configure() {
        requireBinding(NlsService.class);
        requireBinding(ThreadPrincipalService.class);
        requireBinding(PropertySpecService.class);
        requireBinding(TopologyService.class);
        requireBinding(MultiElementDeviceService.class);
        requireBinding(UserService.class);
        requireBinding(TransactionService.class);
        requireBinding(DeviceLifeCycleConfigurationService.class);
        requireBinding(MetrologyConfigurationService.class);
        requireBinding(DataModel.class);
        requireBinding(DeviceService.class);
        bind(DeviceMicroCheckFactory.class).to(DeviceMicroCheckFactoryImpl.class).in(Scopes.SINGLETON);
        bind(ServerMicroActionFactory.class).to(MicroActionFactoryImpl.class).in(Scopes.SINGLETON);
        bind(DeviceLifeCycleService.class).to(DeviceLifeCycleServiceImpl.class).in(Scopes.SINGLETON);
    }

}
