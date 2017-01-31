/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.events.impl;

import org.osgi.framework.BundleContext;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.pubsub.Publisher;
import com.elster.jupiter.util.beans.BeanService;
import com.elster.jupiter.util.json.JsonService;
import java.time.Clock;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

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

        bind(EventService.class).to(EventServiceImpl.class).in(Scopes.SINGLETON);
    }
}
