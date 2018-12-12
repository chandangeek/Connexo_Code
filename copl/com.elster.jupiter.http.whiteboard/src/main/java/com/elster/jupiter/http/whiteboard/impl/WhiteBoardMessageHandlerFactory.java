package com.elster.jupiter.http.whiteboard.impl;

import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;
import com.elster.jupiter.util.json.JsonService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Created by Jozsef Szekrenyes on 11/1/2018.
 */
@Component(name = "com.elster.jupiter.http.whiteboard.impl.WhiteBoardMessageHandlerFactory", service = {
        MessageHandlerFactory.class }, property = {
        "subscriber=WhiteboardSubscriber",
        "destination=JupiterEvents"
        },
        immediate = true)
public class WhiteBoardMessageHandlerFactory implements MessageHandlerFactory {
    private volatile JsonService jsonService;

    @Override
    public MessageHandler newMessageHandler() {
        return new WhiteBoardMessageHandler(jsonService);
    }

    @Reference
    public void setJsonService(JsonService jsonService) {
        this.jsonService = jsonService;
    }

}
