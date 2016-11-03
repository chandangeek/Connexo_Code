package com.elster.jupiter.usagepoint.lifecycle.impl;

import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.usagepoint.lifecycle.UsagePointLifeCycleService;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointMicroActionFactory;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointMicroCheckFactory;
import com.elster.jupiter.usagepoint.lifecycle.impl.actions.UsagePointMicroActionFactoryImpl;
import com.elster.jupiter.usagepoint.lifecycle.impl.checks.UsagePointMicroCheckFactoryImpl;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

public class UsagePointLifeCycleModule extends AbstractModule {
    @Override
    protected void configure() {
        requireBinding(NlsService.class);
        requireBinding(MeteringService.class);
        requireBinding(FiniteStateMachineService.class);
        requireBinding(UpgradeService.class);

        bind(UsagePointLifeCycleService.class).to(UsagePointLifeCycleServiceImpl.class).in(Scopes.SINGLETON);
        bind(UsagePointMicroActionFactory.class).to(UsagePointMicroActionFactoryImpl.class).in(Scopes.SINGLETON);
        bind(UsagePointMicroCheckFactory.class).to(UsagePointMicroCheckFactoryImpl.class).in(Scopes.SINGLETON);
    }
}
