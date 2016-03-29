package com.elster.jupiter.users.impl;

import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;
import com.elster.jupiter.util.json.JsonService;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import java.io.IOException;
import java.time.Clock;
import java.util.Arrays;
import java.util.logging.*;

@Component(name = "com.elster.jupiter.users.messagehandlerlauncher", service = MessageHandlerFactory.class, property = {"subscriber=UsrQueueSubsc", "destination=UsrQueueDest"}, immediate = true)
public class UsersCreatedMessageHandlerFactory implements MessageHandlerFactory{

    public UsersCreatedMessageHandlerFactory(){

    }

    private static final String USER_LOG = "userLog";
    private static final String TOKEN_RENEWAL = "tokenRenewal";
    private Logger userLogin = Logger.getLogger(USER_LOG);
    private Logger tokenRenewal = Logger.getLogger(TOKEN_RENEWAL);
    private volatile JsonService jsonService;
    private volatile Clock clock;

    @Override
    public MessageHandler newMessageHandler() {
        return new UserCreatedMessageHandler(userLogin, tokenRenewal, jsonService);
    }

    @Activate
    public void activate(BundleContext context) {
        LogManager manager = LogManager.getLogManager();
        userLogin = manager.getProperty(USER_LOG + ".handler") != null ? setFileHandler(manager, manager.getProperty(USER_LOG + ".handler"), userLogin) : userLogin;
        tokenRenewal = manager.getProperty(TOKEN_RENEWAL + ".handler") != null ? setFileHandler(manager, manager.getProperty(TOKEN_RENEWAL + ".handler"), tokenRenewal) : tokenRenewal;
    }

    @Deactivate
    public void deactivate() {
        Arrays.stream(tokenRenewal.getHandlers()).forEach(Handler::close);
        Arrays.stream(userLogin.getHandlers()).forEach(Handler::close);
    }

    private Logger setFileHandler(LogManager manager, String handler, Logger logger){
        String pattern = manager.getProperty(handler + ".pattern");
        if(pattern != null){
            try {
                String limit = manager.getProperty(handler + ".limit") != null ? manager.getProperty(handler + ".limit") : "0";
                String count = manager.getProperty(handler + ".count") != null ? manager.getProperty(handler + ".count") : "1";
                FileHandler fileHandler = new FileHandler(pattern, Integer.valueOf(limit), Integer.valueOf(count), true);
                Formatter f = new SingleLineFormatter();
                fileHandler.setFormatter(f);
                logger.addHandler(fileHandler);
            } catch (IOException e) {
                logger.log(Level.SEVERE , e.getMessage(), e);
            }
        }
        return logger;
    }

    @Reference
    public void setClock(Clock clock){
        this.clock = clock;
    }

    @Reference
    public void setJsonService(JsonService jsonService){
        this.jsonService = jsonService;
    }
}
