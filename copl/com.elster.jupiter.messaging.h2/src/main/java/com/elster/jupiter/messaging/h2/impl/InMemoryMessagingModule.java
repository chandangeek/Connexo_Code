package com.elster.jupiter.messaging.h2.impl;

import com.elster.jupiter.messaging.MessageService;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

public class InMemoryMessagingModule extends AbstractModule {
	
    @Override
    protected void configure() {
        bind(MessageService.class).to(TransientMessageService.class).in(Scopes.SINGLETON);
    }
    
}
