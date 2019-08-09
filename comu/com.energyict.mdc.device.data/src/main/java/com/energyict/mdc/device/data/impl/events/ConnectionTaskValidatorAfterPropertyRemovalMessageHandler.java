/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.events;

import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.energyict.mdc.common.tasks.ConnectionTask;
import com.energyict.mdc.common.tasks.PartialConnectionTask;
import com.energyict.mdc.device.data.impl.tasks.ServerConnectionTaskService;
import com.energyict.mdc.device.data.tasks.ConnectionTaskService;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

/**
 * Checks all {@link ConnectionTask}s that depend on a {@link PartialConnectionTask}
 * that was updated and some of its required properties were removed.
 * Every ConnectionTask that is missing a required property because of that
 * will be marked as {@link ConnectionTask.ConnectionTaskLifecycleStatus#INCOMPLETE}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-07-16 (08:56)
 */
public class ConnectionTaskValidatorAfterPropertyRemovalMessageHandler implements MessageHandler {

    private final MessageService messageService;
    private final ServerConnectionTaskService connectionTaskService;
    private final Injector injector;

    public ConnectionTaskValidatorAfterPropertyRemovalMessageHandler(MessageService messageService, ServerConnectionTaskService connectionTaskService) {
        super();
        this.messageService = messageService;
        this.connectionTaskService = connectionTaskService;
        this.injector = Guice.createInjector(this.getModule());
    }

    @Override
    public void process(Message message) {
        ConnectionTasksRevalidationMessage.from(message, this.injector).process();
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