package com.elster.jupiter.yellowfin.impl;

import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.util.json.JsonService;
import com.elster.jupiter.yellowfin.YellowfinService;

public class LogoutMessageHandler implements MessageHandler {
    private final YellowfinService yellowfinService;
    private final JsonService jsonService;

    public LogoutMessageHandler(YellowfinService yellowfinService, JsonService jsonService){
        this.yellowfinService = yellowfinService;
        this.jsonService = jsonService;
    }

    @Override
    public void process(Message message) {
        String user = jsonService.deserialize(message.getPayload(), String.class);
        yellowfinService.logout(user);
    }
}
