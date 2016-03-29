package com.elster.jupiter.users.impl;

import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.json.JsonService;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.*;
import java.util.regex.Pattern;


public class UserCreatedMessageHandler implements MessageHandler {

//    private static final String USER_LOG = "userLog";
//    private static final String TOKEN_RENEWAL = "tokenRenewal";
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
                    if (entry.getKey().equals("LoginFailed")) {
                        userLogin.log(Level.WARNING, entry.getKey() + " -- " + entry.getValue());
                    } else if (entry.getKey().equals("LoginSuccess")) {
                        userLogin.log(Level.INFO, entry.getKey() + " -- " + entry.getValue());
                    }
                });
    }
}
