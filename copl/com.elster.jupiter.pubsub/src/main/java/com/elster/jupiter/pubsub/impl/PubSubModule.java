package com.elster.jupiter.pubsub.impl;

import com.elster.jupiter.pubsub.Publisher;
import com.google.inject.AbstractModule;
import org.osgi.service.log.LogService;

public class PubSubModule extends AbstractModule {
	
	private final LogService logService;
	
	public PubSubModule(LogService logService) {
		this.logService = logService;
	}

    @Override
    protected void configure() {
        bind(Publisher.class).toInstance(new PublisherImpl(logService));
    }
}
