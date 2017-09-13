/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.events;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.MessageService;
import com.energyict.mdc.protocol.api.ConnectionFunction;

import com.google.inject.Injector;

import static com.energyict.mdc.device.data.impl.events.ConnectionTaskValidatorAfterConnectionFunctionModificationMessageHandlerFactory.TASK_DESTINATION;

/**
 * Defines common behavior for all messages that are published
 * for a-synchronous handling for the purpose of checking that
 * all connection tasks that depend on a partial connection task
 * which had a modification of {@link ConnectionFunction}
 * (either added/changed/removed).
 *
 * @author Stijn Vanhoorelbeke
 * @since 2017-08-17 (11:05)
 */
public abstract class ConnectionTasksConnectionFunctionRevalidationMessage {

    /**
     * The character that separates the class name from the id of the previous connection function
     */
    protected static final String CLASS_NAME_CONNECTION_FUNCTION_SEPARATOR = "#";

    private final MessageService messageService;

    protected ConnectionTasksConnectionFunctionRevalidationMessage(MessageService messageService) {
        super();
        this.messageService = messageService;
    }

    public static ConnectionTasksConnectionFunctionRevalidationMessage from(Message message, Injector injector) {
        String[] classNameAndProperties = new String(message.getPayload()).split(CLASS_NAME_CONNECTION_FUNCTION_SEPARATOR);
        if (StartConnectionTasksRevalidationAfterConnectionFunctionModification.class.getSimpleName().equals(classNameAndProperties[0])) {
            return injector.getInstance(StartConnectionTasksRevalidationAfterConnectionFunctionModification.class).with(classNameAndProperties[1]);
        } else {
            return injector.getInstance(RevalidateConnectionTasksAfterConnectionFunctionModification.class).with(classNameAndProperties[1]);
        }
    }

    protected MessageService getMessageService() {
        return messageService;
    }

    /**
     * Publishes this message on the appropriate DestinationSpec.
     */
    public void publish() {
        this.getMessageDestination().message(this.messagePayload()).send();
    }

    private DestinationSpec getMessageDestination() {
        return this.messageService
                .getDestinationSpec(TASK_DESTINATION)
                .orElseThrow(() -> new RuntimeException("DestinationSpec " + TASK_DESTINATION + " is missing"));
    }

    private String messagePayload() {
        return this.getClass().getSimpleName() + CLASS_NAME_CONNECTION_FUNCTION_SEPARATOR + this.propertiesPayload();
    }

    protected abstract String propertiesPayload();

    /**
     * Processes this message.
     */
    protected abstract void process();

}