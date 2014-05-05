package com.energyict.mdc.device.data.impl.events;

import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;
import org.osgi.service.component.annotations.Component;

@Component(name="com.energyict.mdc.device.data.comschedule.update.messagehandler", service = MessageHandler.class, immediate = true)
public class ComScheduleUpdaterMessageHandlerFactory implements MessageHandlerFactory {
    private static final ComScheduleUpdatedMessageHandler MESSAGE_HANDLER = new ComScheduleUpdatedMessageHandler();
    @Override
    public MessageHandler newMessageHandler() {
        return MESSAGE_HANDLER;
    }
}
