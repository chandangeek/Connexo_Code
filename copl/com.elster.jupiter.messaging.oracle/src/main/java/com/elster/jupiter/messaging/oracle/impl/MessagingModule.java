package com.elster.jupiter.messaging.oracle.impl;

import com.elster.jupiter.messaging.MessageService;
import com.google.inject.AbstractModule;

public class MessagingModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(MessageService.class).to(MessageServiceImpl.class);

    }
}
