package com.elster.jupiter.users.impl;

import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.util.json.JsonService;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;


public class UserCreatedMessageHandler implements MessageHandler {

    private final JsonService jsonService;
    private final Logger userLogin;
    private final Logger tokenRenewal;

    public UserCreatedMessageHandler(Logger userLogin, Logger tokenRenewal,JsonService jsonService){
        this.jsonService = jsonService;
        this.userLogin = userLogin;
        this.tokenRenewal = tokenRenewal;
    }


    @Override
    public void process(Message message) {
        @SuppressWarnings("unchecked")
        Map<String, String> messageProperties = this.jsonService.deserialize(message.getPayload(), Map.class);
        messageProperties.entrySet().stream()
                .forEach(entry -> {
                    if (entry.getKey().equals("Successful login for user ")) {
                        userLogin.log(Level.INFO, entry.getKey() + entry.getValue());
                    } else if (entry.getKey().equals("Unsuccessful login attempt for user ")) {
                        userLogin.log(Level.WARNING, entry.getKey() + entry.getValue());
                    } else if (entry.getKey().equals("Token renewal for user ")){
                        tokenRenewal.log(Level.INFO, entry.getKey() + entry.getValue());
                    } else if (entry.getKey().equals("Token expired for user ")){
                        tokenRenewal.log(Level.INFO, entry.getKey() + entry.getValue());
                    }
                });
    }
}
