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
    private final String TOKEN_INVALID = "Invalid token ";
    private final String TOKEN_EXPIRED = "Token expired for user ";
    private final String TOKEN_RENEWAL = "Token renewal for user ";
    private final String TOKEN_GENERATED = "Token generated for user ";
    private final String USER_NOT_FOUND = "User not found ";
    private final String SUCCESSFUL_LOGIN = "Successful login for user ";
    private final String UNSUCCESSFUL_LOGIN = "Unsuccessful login attempt for user ";
    private final String USER_DISABLED = "User account disabled ";

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
                    if (entry.getKey().equals(SUCCESSFUL_LOGIN)) {
                        userLogin.log(Level.INFO, entry.getKey() + entry.getValue());
                    } else if (entry.getKey().equals(UNSUCCESSFUL_LOGIN)) {
                        userLogin.log(Level.WARNING, entry.getKey() + entry.getValue());
                    } else if (entry.getKey().equals(TOKEN_RENEWAL)){
                        tokenRenewal.log(Level.INFO, entry.getKey() + entry.getValue());
                    } else if (entry.getKey().equals(TOKEN_EXPIRED)){
                        tokenRenewal.log(Level.INFO, entry.getKey() + entry.getValue());
                    } else if (entry.getKey().equals(TOKEN_INVALID)){
                        tokenRenewal.log(Level.WARNING, entry.getKey() + entry.getValue());
                    } else if (entry.getKey().equals(USER_NOT_FOUND)){
                        tokenRenewal.log(Level.INFO, entry.getKey() + entry.getValue());
                    } else if (entry.getKey().equals(USER_DISABLED)){
                        tokenRenewal.log(Level.INFO, entry.getKey() + entry.getValue());
                    } else if (entry.getKey().equals(TOKEN_GENERATED)){
                        tokenRenewal.log(Level.INFO, entry.getKey() + entry.getValue());
                    }
                });
    }
}
