package com.elster.jupiter.users.impl;

import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;
import com.elster.jupiter.util.json.JsonService;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import java.util.logging.Logger;

@Component(name = "com.elster.jupiter.users.messagehandlerlauncher", service = MessageHandlerFactory.class, property = {"subscriber=UsrQueueSubsc", "destination=UsrQueueDest"}, immediate = true)
public class UsersCreatedMessageHandlerFactory implements MessageHandlerFactory{

    public UsersCreatedMessageHandlerFactory(){

    }

    private static final String USER_LOG = "userLog";
    private static final String TOKEN_RENEWAL = "tokenRenewal";
    private Logger userLogin = Logger.getLogger(USER_LOG);
    private Logger tokenRenewal = Logger.getLogger(TOKEN_RENEWAL);
    private volatile JsonService jsonService;

    @Override
    public MessageHandler newMessageHandler() {
        return new UserCreatedMessageHandler(userLogin, tokenRenewal, jsonService);
    }

    @Activate
    public void activate(BundleContext context) {
    }

    @Deactivate
    public void deactivate() {
    }

    @Reference
    public void setJsonService(JsonService jsonService){
        this.jsonService = jsonService;
    }
}
