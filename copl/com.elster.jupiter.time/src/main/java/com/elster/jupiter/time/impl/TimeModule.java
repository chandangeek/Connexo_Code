package com.elster.jupiter.time.impl;

import com.elster.jupiter.time.TimeService;
import com.google.inject.AbstractModule;

public class TimeModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(TimeService.class).to(TimeServiceImpl.class);
    }
}
