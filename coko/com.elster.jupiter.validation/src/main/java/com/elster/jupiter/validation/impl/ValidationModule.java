package com.elster.jupiter.validation.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.util.time.Clock;
import com.elster.jupiter.validation.ValidationService;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

public class ValidationModule extends AbstractModule {

    @Override
    protected void configure() {
        requireBinding(Clock.class);
        requireBinding(EventService.class);
        requireBinding(MessageService.class);
        requireBinding(OrmService.class);

        bind(ValidationService.class).to(ValidationServiceImpl.class).in(Scopes.SINGLETON);
    }
}
