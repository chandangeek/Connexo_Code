/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.time.impl;

import com.elster.jupiter.time.TimeService;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

public class TimeModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(TimeService.class).to(TimeServiceImpl.class).in(Scopes.SINGLETON);
    }
}
