package com.elster.jupiter.data.lifecycle.impl;

import java.time.Clock;

import com.elster.jupiter.data.lifecycle.LifeCycleService;
import com.elster.jupiter.util.beans.BeanService;
import com.elster.jupiter.util.beans.impl.BeanServiceImpl;
import com.elster.jupiter.util.json.JsonService;
import com.elster.jupiter.util.json.impl.JsonServiceImpl;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

public class DataLifeCycleModule extends AbstractModule {
	
    public DataLifeCycleModule() {
    }

    @Override
    protected void configure() {
        bind(LifeCycleService.class).to(LifeCycleServiceImpl.class);
    }
}
