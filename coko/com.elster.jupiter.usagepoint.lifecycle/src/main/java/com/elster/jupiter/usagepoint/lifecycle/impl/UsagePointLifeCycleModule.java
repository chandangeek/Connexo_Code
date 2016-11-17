package com.elster.jupiter.usagepoint.lifecycle.impl;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.usagepoint.lifecycle.UsagePointLifeCycleService;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointLifeCycleConfigurationService;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointMicroActionFactory;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointMicroCheckFactory;
import com.elster.jupiter.usagepoint.lifecycle.impl.actions.UsagePointMicroActionFactoryImpl;
import com.elster.jupiter.usagepoint.lifecycle.impl.checks.UsagePointMicroCheckFactoryImpl;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

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

        bind(UsagePointLifeCycleService.class).to(UsagePointLifeCycleServiceImpl.class).in(Scopes.SINGLETON);
        bind(UsagePointMicroActionFactory.class).to(UsagePointMicroActionFactoryImpl.class).in(Scopes.SINGLETON);
        bind(UsagePointMicroCheckFactory.class).to(UsagePointMicroCheckFactoryImpl.class).in(Scopes.SINGLETON);
    }
}
