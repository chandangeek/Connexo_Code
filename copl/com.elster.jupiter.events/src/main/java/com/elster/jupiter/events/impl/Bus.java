package com.elster.jupiter.events.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.pubsub.Publisher;
import com.elster.jupiter.util.beans.BeanService;
import com.elster.jupiter.util.json.JsonService;
import com.elster.jupiter.util.time.Clock;
import org.osgi.service.event.EventAdmin;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public enum Bus {
    ;

    public static final String COMPONENTNAME = "EVT";

    private static AtomicReference<ServiceLocator> locatorHolder = new AtomicReference<>();

    public static void setServiceLocator(ServiceLocator locator) {
        Bus.locatorHolder.set(Objects.requireNonNull(locator));
    }

    public static void clearServiceLocator(ServiceLocator old) {
        locatorHolder.compareAndSet(Objects.requireNonNull(old), null);
    }

    public static Clock getClock() {
        return getLocator().getClock();
    }

    public static EventAdmin getEventAdmin() {
        return getLocator().getEventAdmin();
    }

    public static Publisher getPublisher() {
        return getLocator().getPublisher();
    }

    public static BeanService getBeanService() {
        return getLocator().getBeanService();
    }

    public static JsonService getJsonService() {
        return getLocator().getJsonService();
    }

    public static MessageService getMessageService() {
        return getLocator().getMessageService();
    }

    public static EventConfiguration getEventConfiguration() {
        return getLocator().getEventConfiguration();
    }

    public static OrmClient getOrmClient() {
        return getLocator().getOrmClient();
    }

    public static EventService getEventService() {
        return getLocator().getEventService();
    }

    private static ServiceLocator getLocator() {
        return locatorHolder.get();
    }
}
