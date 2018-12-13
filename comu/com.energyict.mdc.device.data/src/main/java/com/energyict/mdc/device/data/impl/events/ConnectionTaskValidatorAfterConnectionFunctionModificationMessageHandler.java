/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.events;

import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.data.impl.tasks.ServerConnectionTaskService;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ConnectionTaskService;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

/**
 * Checks all {@link ConnectionTask}s that depend on a {@link PartialConnectionTask}
 * that was updated and had its ConnectionFunction modified (either added/updated/removed).<br/>
 * All ComTaskExecutions relying on a ConnectionTask using an involved ConnectionFunction
 * (either the old one (if present), or the new one (if present)) will be updated with the correct
 * ConnectionTask (or the ConnectionTask will be removed).
 *
 *
 * @author Stijn Vanhoorelbeke
 * @since 2017-08-17 (11:34)
 */
public class ConnectionTaskValidatorAfterConnectionFunctionModificationMessageHandler implements MessageHandler {

    private final MessageService messageService;
    private final ServerConnectionTaskService connectionTaskService;
    private final Injector injector;

    public ConnectionTaskValidatorAfterConnectionFunctionModificationMessageHandler(MessageService messageService, ServerConnectionTaskService connectionTaskService) {
        super();
        this.messageService = messageService;
        this.connectionTaskService = connectionTaskService;
        this.injector = Guice.createInjector(this.getModule());
    }

    @Override
    public void process(Message message) {
        ConnectionTasksConnectionFunctionRevalidationMessage.from(message, this.injector).process();
    }

    private Module getModule() {
        return new AbstractModule() {
            @Override
            public void configure() {
                this.bind(MessageService.class).toInstance(messageService);
                this.bind(ConnectionTaskService.class).toInstance(connectionTaskService);
                this.bind(ServerConnectionTaskService.class).toInstance(connectionTaskService);
            }
        };
    }

}