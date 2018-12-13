/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.users.UserService;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

import java.time.Clock;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-02-15 (13:19)
 */
public class MetrologyConfigurationModule extends AbstractModule {

    @Override
    protected void configure() {
        requireBinding(Clock.class);
        requireBinding(OrmService.class);
        requireBinding(QueryService.class);
        requireBinding(UserService.class);
        requireBinding(EventService.class);
        requireBinding(ThreadPrincipalService.class);
        requireBinding(CustomPropertySetService.class);

        bind(MetrologyConfigurationService.class).to(MetrologyConfigurationServiceImpl.class).in(Scopes.SINGLETON);
        bind(ServerMetrologyConfigurationService.class).to(MetrologyConfigurationServiceImpl.class).in(Scopes.SINGLETON);
    }

}