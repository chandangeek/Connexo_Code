package com.elster.jupiter.events.impl;

import org.osgi.framework.BundleContext;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.pubsub.Publisher;
import com.elster.jupiter.util.beans.BeanService;
import com.elster.jupiter.util.json.JsonService;
import com.elster.jupiter.util.time.Clock;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import org.osgi.service.event.EventAdmin;

public class EventsModule extends AbstractModule {

    @Override
    protected void configure() {
        requireBinding(Clock.class);
        requireBinding(JsonService.class);
        requireBinding(Publisher.class);
        requireBinding(BeanService.class);
        requireBinding(OrmService.class);
        requireBinding(MessageService.class);
        requireBinding(BundleContext.class);
        requireBinding(EventAdmin.class);

        bind(EventService.class).to(EventServiceImpl.class).in(Scopes.SINGLETON);
    }
}
