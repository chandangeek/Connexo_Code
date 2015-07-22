package com.energyict.mdc.device.data.impl.events;

import com.energyict.mdc.device.data.impl.tasks.ServerConnectionTaskService;

import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;

/**
 * Provides factory services for {@link ConnectionTaskValidatorAfterPropertyRemovalMessageHandler}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-07-16 (08:43)
 */
@Component(name = "com.energyict.mdc.device.data.connectiontask.validation.handler.factory", service = MessageHandlerFactory.class, property = {"subscriber=" + ConnectionTaskValidatorAfterPropertyRemovalMessageHandlerFactory.TASK_SUBSCRIBER, "destination=" + ConnectionTaskValidatorAfterPropertyRemovalMessageHandlerFactory.TASK_DESTINATION}, immediate = true)
@SuppressWarnings("unused")
public class ConnectionTaskValidatorAfterPropertyRemovalMessageHandlerFactory implements MessageHandlerFactory {

    public static final String TASK_DESTINATION = "MDCConnTaskRevalidation";
    public static final String TASK_SUBSCRIBER = "MDCConnTaskRevalidator";
    public static final String TASK_SUBSCRIBER_DISPLAY_NAME = "Revalidate connection methods after property removal on configuration level";

    private volatile MessageService messageService;
    private volatile ServerConnectionTaskService connectionTaskService;

    // For OSGi purposes
    public ConnectionTaskValidatorAfterPropertyRemovalMessageHandlerFactory() {
        super();
    }

    // For testing purposes
    @Inject
    public ConnectionTaskValidatorAfterPropertyRemovalMessageHandlerFactory(MessageService messageService, ServerConnectionTaskService connectionTaskService) {
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
        return new ConnectionTaskValidatorAfterPropertyRemovalMessageHandler(this.messageService, this.connectionTaskService);
    }

}