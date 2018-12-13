/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.pubsub.impl;

import com.elster.jupiter.pubsub.Publisher;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

public class PubSubModule extends AbstractModule {
	
    @Override
    protected void configure() {
        bind(Publisher.class).to(PublisherImpl.class).in(Scopes.SINGLETON);
    }
}
