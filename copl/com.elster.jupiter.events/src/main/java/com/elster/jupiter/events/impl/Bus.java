package com.elster.jupiter.events.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.orm.cache.ComponentCache;
import com.elster.jupiter.pubsub.Publisher;
import com.elster.jupiter.util.beans.BeanService;
import com.elster.jupiter.util.json.JsonService;
import com.elster.jupiter.util.time.Clock;
import org.osgi.service.event.EventAdmin;

public enum Bus {
    ;

    public static final String COMPONENTNAME = "EVT";

    private static volatile ServiceLocator serviceLocator;

    public static ComponentCache getComponentCache() {
        return serviceLocator.getComponentCache();
    }

    public static Clock getClock() {
        return serviceLocator.getClock();
    }

    public static EventAdmin getEventAdmin() {
        return serviceLocator.getEventAdmin();
    }

    public static Publisher getPublisher() {
        return serviceLocator.getPublisher();
    }

    public static BeanService getBeanService() {
        return serviceLocator.getBeanService();
    }

    public static JsonService getJsonService() {
        return serviceLocator.getJsonService();
    }

    public static MessageService getMessageService() {
        return serviceLocator.getMessageService();
    }

    public static EventConfiguration getEventConfiguration() {
        return serviceLocator.getEventConfiguration();
    }

    public static void setServiceLocator(ServiceLocator serviceLocator) {
        Bus.serviceLocator = serviceLocator;
    }

    public static OrmClient getOrmClient() {
        return Bus.serviceLocator.getOrmClient();
    }

    public static EventService getEventService() {
        return serviceLocator.getEventService();
    }
}
