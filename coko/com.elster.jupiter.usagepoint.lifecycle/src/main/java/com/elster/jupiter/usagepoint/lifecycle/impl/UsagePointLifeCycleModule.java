/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.usagepoint.lifecycle.impl;

import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.usagepoint.lifecycle.UsagePointLifeCycleService;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointLifeCycleConfigurationService;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointMicroActionFactory;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointMicroCheckFactory;
import com.elster.jupiter.usagepoint.lifecycle.impl.actions.UsagePointMicroActionFactoryImpl;
import com.elster.jupiter.usagepoint.lifecycle.impl.checks.UsagePointMicroCheckFactoryImpl;
import com.elster.jupiter.users.UserService;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

import javax.inject.Inject;
import javax.inject.Provider;
import java.time.Clock;

public class UsagePointLifeCycleModule extends AbstractModule {
    @Override
    protected void configure() {
        requireBinding(NlsService.class);
        requireBinding(OrmService.class);
        requireBinding(UpgradeService.class);
        requireBinding(ThreadPrincipalService.class);
        requireBinding(UsagePointLifeCycleConfigurationService.class);
        requireBinding(MeteringService.class);
        requireBinding(Clock.class);
        requireBinding(MessageService.class);
        requireBinding(TaskService.class);
        requireBinding(UserService.class);

        bind(ServerUsagePointLifeCycleService.class).to(UsagePointLifeCycleServiceImpl.class).in(Scopes.SINGLETON);
        bind(UsagePointLifeCycleService.class).toProvider(UsagePointLifeCycleServiceProvider.class);
        bind(UsagePointMicroActionFactory.class).to(UsagePointMicroActionFactoryImpl.class).in(Scopes.SINGLETON);
        bind(UsagePointMicroCheckFactory.class).to(UsagePointMicroCheckFactoryImpl.class).in(Scopes.SINGLETON);
    }

    private static class UsagePointLifeCycleServiceProvider implements Provider<UsagePointLifeCycleService> {
        private final ServerUsagePointLifeCycleService lifeCycleService;

        @Inject
        private UsagePointLifeCycleServiceProvider(ServerUsagePointLifeCycleService lifeCycleService) {
            this.lifeCycleService = lifeCycleService;
        }

        @Override
        public UsagePointLifeCycleService get() {
            return this.lifeCycleService;
        }
    }
}
