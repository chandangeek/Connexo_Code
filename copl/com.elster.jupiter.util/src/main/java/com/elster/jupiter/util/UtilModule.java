package com.elster.jupiter.util;

import com.elster.jupiter.util.beans.BeanService;
import com.elster.jupiter.util.beans.impl.BeanServiceImpl;
import com.elster.jupiter.util.json.JsonService;
import com.elster.jupiter.util.json.impl.JsonServiceImpl;
import com.elster.jupiter.util.time.Clock;
import com.elster.jupiter.util.time.impl.DefaultClock;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

public class UtilModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(JsonService.class).to(JsonServiceImpl.class).in(Scopes.SINGLETON);
        bind(BeanService.class).to(BeanServiceImpl.class).in(Scopes.SINGLETON);
        bind(Clock.class).to(DefaultClock.class).in(Scopes.SINGLETON);
    }
}
