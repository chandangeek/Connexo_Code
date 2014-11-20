package com.elster.jupiter.data.lifecycle.impl;

import com.elster.jupiter.data.lifecycle.LifeCycleService;
import com.google.inject.AbstractModule;

public class DataLifeCycleModule extends AbstractModule {
	
    public DataLifeCycleModule() {
    }

    @Override
    protected void configure() {
        bind(LifeCycleService.class).to(LifeCycleServiceImpl.class);
    }
}
