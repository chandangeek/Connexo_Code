package com.elster.jupiter.mdm.usagepoint.lifecycle.impl;

import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.mdm.usagepoint.lifecycle.UsagePointLifeCycleService;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.users.UserService;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

public class UsagePointLifeCycleModule extends AbstractModule {
    @Override
    protected void configure() {
        requireBinding(OrmService.class);
        requireBinding(NlsService.class);
        requireBinding(FiniteStateMachineService.class);
        requireBinding(UpgradeService.class);
        requireBinding(UserService.class);

        bind(UsagePointLifeCycleService.class).to(UsagePointLifeCycleServiceImpl.class).in(Scopes.SINGLETON);
    }
}
