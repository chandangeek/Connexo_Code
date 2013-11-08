package com.elster.jupiter.validation.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.orm.cache.ComponentCache;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public enum Bus {
    ;

    public static final String COMPONENTNAME = "VAL";

    private static AtomicReference<ServiceLocator> locatorHolder = new AtomicReference<>();

    public static void setServiceLocator(ServiceLocator locator) {
        Bus.locatorHolder.set(Objects.requireNonNull(locator));
    }

    public static void clearServiceLocator(ServiceLocator old) {
        locatorHolder.compareAndSet(Objects.requireNonNull(old), null);
    }

    private static ServiceLocator getLocator() {
        return locatorHolder.get();
    }

    public static OrmClient getOrmClient() {
        return getLocator().getOrmClient();
    }

    static ComponentCache getComponentCache() {
        return getLocator().getComponentCache();
    }

    public static EventService getEventService() {
        return getLocator().getEventService();
    }
}
