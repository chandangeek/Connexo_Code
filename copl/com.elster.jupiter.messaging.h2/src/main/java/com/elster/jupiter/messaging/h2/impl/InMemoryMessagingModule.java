package com.elster.jupiter.messaging.h2.impl;

import com.elster.jupiter.messaging.MessageService;
import com.google.inject.AbstractModule;

public class InMemoryMessagingModule extends AbstractModule {
	
    @Override
    protected void configure() {
        TransientMessageService instance = new TransientMessageService();
        instance.install();
        bind(MessageService.class).toInstance(instance);
    }
    
}
