package com.energyict.mdc.device.data.impl.events;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.MessageService;
import com.google.inject.Injector;

import static com.energyict.mdc.device.data.impl.events.ConnectionTaskValidatorAfterPropertyRemovalMessageHandlerFactory.TASK_DESTINATION;

/**
 * Defines common behavior for all messages that are published
 * for a-synchronous handling for the purpose of checking that
 * all connection tasks that depend on a partial connection task
 * that has had at least one of its required properties removed.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-07-16 (11:05)
 */
public abstract class ConnectionTasksRevalidationMessage {

    /**
     * The character that separates the class name from the actual list of properties.
     */
    static final String CLASS_NAME_PROPERTIES_SEPARATOR = "#";

    private final MessageService messageService;

    protected ConnectionTasksRevalidationMessage(MessageService messageService) {
        super();
        this.messageService = messageService;
    }

    public static ConnectionTasksRevalidationMessage from(Message message, Injector injector) {
        String[] classNameAndProperties = new String(message.getPayload()).split(CLASS_NAME_PROPERTIES_SEPARATOR);
        if (StartConnectionTasksRevalidationAfterPropertyRemoval.class.getSimpleName().equals(classNameAndProperties[0])) {
            return injector.getInstance(StartConnectionTasksRevalidationAfterPropertyRemoval.class).with(classNameAndProperties[1]);
        }
        else {
            return injector.getInstance(RevalidateConnectionTasksAfterPropertyRemoval.class).with(classNameAndProperties[1]);
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
                .orElseThrow(() -> new RuntimeException("DestinationSpec " + TASK_DESTINATION + " is missing, run init DDC to fix this"));
    }

    private String messagePayload() {
        return this.getClass().getSimpleName() + CLASS_NAME_PROPERTIES_SEPARATOR + this.propertiesPayload();
    }

    protected abstract String propertiesPayload();

    /**
     * Processes this message.
     */
    protected abstract void process();

}