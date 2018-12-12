/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.messaging.oracle.impl;

import com.elster.jupiter.messaging.MessageService;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

public class MessagingModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(MessageService.class).to(MessageServiceImpl.class).in(Scopes.SINGLETON);

    }
}
