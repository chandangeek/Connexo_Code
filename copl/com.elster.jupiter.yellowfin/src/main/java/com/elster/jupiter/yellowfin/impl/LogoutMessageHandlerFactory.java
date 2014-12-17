package com.elster.jupiter.yellowfin.impl;

import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;
import com.elster.jupiter.util.json.JsonService;
import com.elster.jupiter.yellowfin.YellowfinService;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import java.time.Clock;

@Component(name = "com.elster.jupiter.logout.handler", service = MessageHandlerFactory.class, property = {"subscriber=LogoutQueueSubsc", "destination=LogoutQueueDest"}, immediate = true)
public class LogoutMessageHandlerFactory implements MessageHandlerFactory {
    private volatile YellowfinService yellowfinService;
    private volatile JsonService jsonService;
    private volatile Clock clock;

    @Override
    public MessageHandler newMessageHandler() {
        return new LogoutMessageHandler(yellowfinService, jsonService);
    }

    @Reference
    public void setYellowfinService(YellowfinService yellowfinService) {
        this.yellowfinService = yellowfinService;
    }

    @Reference
    public void setJsonService(JsonService jsonService) {
        this.jsonService = jsonService;
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }
}
