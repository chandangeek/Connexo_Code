/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.config.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.orm.OrmService;
import com.energyict.mdc.engine.config.EngineConfigurationService;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

public class EngineModelModule extends AbstractModule {

    @Override
    protected void configure() {
        requireBinding(OrmService.class);
        requireBinding(EventService.class);
        requireBinding(NlsService.class);
        bind(EngineConfigurationService.class).to(EngineConfigurationServiceImpl.class).in(Scopes.SINGLETON);
    }

}