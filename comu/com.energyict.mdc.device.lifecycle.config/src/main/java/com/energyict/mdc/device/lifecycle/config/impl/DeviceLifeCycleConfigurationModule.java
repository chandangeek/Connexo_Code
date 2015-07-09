package com.energyict.mdc.device.lifecycle.config.impl;

import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.UserService;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

/**
 * Module intended for use by integration tests.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-19 (16:04)
 */
public class DeviceLifeCycleConfigurationModule extends AbstractModule {

    @Override
    protected void configure() {
        requireBinding(OrmService.class);
        requireBinding(NlsService.class);
        requireBinding(UserService.class);
        requireBinding(TransactionService.class);
        requireBinding(EventService.class);
        bind(DeviceLifeCycleConfigurationService.class).to(DeviceLifeCycleConfigurationServiceImpl.class).in(Scopes.SINGLETON);
    }

}