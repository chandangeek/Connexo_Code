package com.energyict.mdc.device.data.impl.messagehandlers;

import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.energyict.mdc.device.data.ConnectionTaskService;

/**
 * This message handler will trigger connections to rerun. Supports both a group of connections identified by a filter and an exhaustive list
 * Created by bvn on 3/25/15.
 */
public class ConnectionTaskMessageHandler implements MessageHandler {

    private ConnectionTaskService connectionTaskService;

    @Override
    public void process(Message message) {

    }

    @Override
    public void onMessageDelete(Message message) {

    }

    public MessageHandler init(ConnectionTaskService connectionTaskService) {
        this.connectionTaskService = connectionTaskService;
        return this;
    }
}
