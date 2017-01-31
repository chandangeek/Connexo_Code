/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.messaging.h2.impl;

import com.elster.jupiter.messaging.MessageService;
import com.google.inject.AbstractModule;

public class InMemoryMessagingModule extends AbstractModule {
	
    @Override
    protected void configure() {
        TransientMessageService instance = new TransientMessageService();
        instance.createQueueTableSpec("MSG_RAWQUEUETABLE", "RAW", false);
        instance.createQueueTableSpec("MSG_RAWTOPICTABLE", "RAW", true);
        bind(MessageService.class).toInstance(instance);
    }
    
}
