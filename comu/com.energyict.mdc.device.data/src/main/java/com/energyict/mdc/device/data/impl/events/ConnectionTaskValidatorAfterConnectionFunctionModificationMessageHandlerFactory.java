/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.events;

import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;
import com.energyict.mdc.device.data.impl.tasks.ServerConnectionTaskService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;

/**
 * Provides factory services for {@link ConnectionTaskValidatorAfterConnectionFunctionModificationMessageHandler}.
 *
 * @author Stijn Vanhoorelbeke
 * @since 2017-08-17 (11:34)
 */
@Component(name = "com.energyict.mdc.device.data.connectiontask.connectionfunction.validation.handler.factory",
        service = MessageHandlerFactory.class,
        property = {"subscriber=" + ConnectionTaskValidatorAfterConnectionFunctionModificationMessageHandlerFactory.TASK_SUBSCRIBER,
                "destination=" + ConnectionTaskValidatorAfterConnectionFunctionModificationMessageHandlerFactory.TASK_DESTINATION},
        immediate = true)
@SuppressWarnings("unused")
public class ConnectionTaskValidatorAfterConnectionFunctionModificationMessageHandlerFactory implements MessageHandlerFactory {

    public static final String TASK_DESTINATION = "MDCConnTaskCFRevalidate";
    public static final String TASK_SUBSCRIBER = "MDCConnTaskCFRevalidator";
    public static final String TASK_SUBSCRIBER_DISPLAY_NAME = "Revalidate connection methods after modification of the connection function on configuration level";

    private volatile MessageService messageService;
    private volatile ServerConnectionTaskService connectionTaskService;

    // For OSGi purposes
    public ConnectionTaskValidatorAfterConnectionFunctionModificationMessageHandlerFactory() {
        super();
    }

    // For testing purposes
    @Inject
    public ConnectionTaskValidatorAfterConnectionFunctionModificationMessageHandlerFactory(MessageService messageService, ServerConnectionTaskService connectionTaskService) {
        this();
        this.setMessageService(messageService);
        this.setConnectionTaskService(connectionTaskService);
    }

    @Reference
    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }

    @Reference
    public void setConnectionTaskService(ServerConnectionTaskService connectionTaskService) {
        this.connectionTaskService = connectionTaskService;
    }

    @Override
    public MessageHandler newMessageHandler() {
        return new ConnectionTaskValidatorAfterConnectionFunctionModificationMessageHandler(this.messageService, this.connectionTaskService);
    }

}