/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.usagepoint.lifecycle.config.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointLifeCycleConfigurationService;
import com.elster.jupiter.users.UserService;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

public class UsagePointLifeCycleConfigurationModule extends AbstractModule {
    @Override
    protected void configure() {
        requireBinding(OrmService.class);
        requireBinding(NlsService.class);
        requireBinding(UpgradeService.class);
        requireBinding(UserService.class);
        requireBinding(EventService.class);

        bind(UsagePointLifeCycleConfigurationService.class).to(UsagePointLifeCycleConfigurationServiceImpl.class).in(Scopes.SINGLETON);
    }
}
