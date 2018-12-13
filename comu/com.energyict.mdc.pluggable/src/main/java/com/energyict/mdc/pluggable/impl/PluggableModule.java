/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.pluggable.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.NlsService;
import com.energyict.mdc.pluggable.PluggableService;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

import java.time.Clock;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-16 (15:51)
 */
public class PluggableModule extends AbstractModule {

    @Override
    protected void configure() {
        requireBinding(EventService.class);
        requireBinding(NlsService.class);
        requireBinding(Clock.class);

        bind(PluggableService.class).to(PluggableServiceImpl.class).in(Scopes.SINGLETON);
    }

}