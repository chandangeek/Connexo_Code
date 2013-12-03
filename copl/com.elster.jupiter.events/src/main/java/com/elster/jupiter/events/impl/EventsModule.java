package com.elster.jupiter.events.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.cache.CacheService;
import com.elster.jupiter.pubsub.Publisher;
import com.elster.jupiter.util.beans.BeanService;
import com.elster.jupiter.util.json.JsonService;
import com.elster.jupiter.util.time.Clock;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

public class EventsModule extends AbstractModule {

    @Override
    protected void configure() {
        requireBinding(Clock.class);
        requireBinding(JsonService.class);
        requireBinding(Publisher.class);
        requireBinding(BeanService.class);
        requireBinding(OrmService.class);
        requireBinding(CacheService.class);
        requireBinding(MessageService.class);
        requireBinding(BundleContext.class);

        bind(EventService.class).to(EventServiceImpl.class).in(Scopes.SINGLETON);
    }
}
