package com.energyict.mdc.device.lifecycle.impl;

import com.energyict.mdc.device.lifecycle.DeviceLifeCycleService;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
import com.energyict.mdc.device.topology.TopologyService;

import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.UserService;
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
        requireBinding(UserService.class);
        requireBinding(TransactionService.class);
        requireBinding(DeviceLifeCycleConfigurationService.class);
        bind(ServerMicroCheckFactory.class).to(MicroCheckFactoryImpl.class).in(Scopes.SINGLETON);
        bind(ServerMicroActionFactory.class).to(MicroActionFactoryImpl.class).in(Scopes.SINGLETON);
        bind(DeviceLifeCycleService.class).to(DeviceLifeCycleServiceImpl.class).in(Scopes.SINGLETON);
    }

}