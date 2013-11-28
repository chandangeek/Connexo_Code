package com.elster.jupiter.pubsub.impl;

import com.elster.jupiter.pubsub.Publisher;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import org.osgi.service.log.LogService;

public class PubSubModule extends AbstractModule {

    @Override
    protected void configure() {
        requireBinding(LogService.class);

        bind(Publisher.class).to(PublisherImpl.class).in(Scopes.SINGLETON);
    }
}
