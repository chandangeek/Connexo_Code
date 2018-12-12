package com.elster.jupiter.http.whiteboard.impl;

import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.util.json.JsonService;

import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by Jozsef Szekrenyes on 11/1/2018.
 */
public class WhiteBoardMessageHandler implements MessageHandler {
    private static final Logger LOGGER = Logger.getLogger(WhiteBoardMessageHandler.class.getName());

    private final JsonService jsonService;

    public WhiteBoardMessageHandler(JsonService jsonService) {
        this.jsonService = jsonService;
    }

    @Override
    public void process(Message message) {
        LOGGER.info("Message received: " + jsonService.deserialize(message.getPayload(), Map.class));
    }
}
