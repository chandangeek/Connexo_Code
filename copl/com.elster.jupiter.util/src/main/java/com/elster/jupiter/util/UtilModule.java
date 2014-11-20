package com.elster.jupiter.util;

import java.time.Clock;

import com.elster.jupiter.util.beans.BeanService;
import com.elster.jupiter.util.beans.impl.BeanServiceImpl;
import com.elster.jupiter.util.cron.CronExpressionParser;
import com.elster.jupiter.util.cron.impl.DefaultCronExpressionParser;
import com.elster.jupiter.util.json.JsonService;
import com.elster.jupiter.util.json.impl.JsonServiceImpl;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

public class UtilModule extends AbstractModule {
	
	private final Clock clock;

    public UtilModule(Clock clock) {
        this.clock = clock;
    }

    public UtilModule() {
    	this.clock = Clock.systemDefaultZone();
    }

    @Override
    protected void configure() {
        bind(JsonService.class).to(JsonServiceImpl.class).in(Scopes.SINGLETON);
        bind(BeanService.class).to(BeanServiceImpl.class).in(Scopes.SINGLETON);
        bind(CronExpressionParser.class).to(DefaultCronExpressionParser.class).in(Scopes.SINGLETON);
        bind(Clock.class).toInstance(clock);        
    }
}
