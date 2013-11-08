package com.elster.jupiter.parties.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.orm.cache.ComponentCache;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.time.Clock;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

enum Bus {

    ;
	
	static final String COMPONENTNAME = "PRT";

    private static AtomicReference<ServiceLocator> locatorHolder = new AtomicReference<>();

    public static void setServiceLocator(ServiceLocator locator) {
        Bus.locatorHolder.set(Objects.requireNonNull(locator));
    }

    public static void clearServiceLocator(ServiceLocator old) {
        locatorHolder.compareAndSet(Objects.requireNonNull(old), null);
    }

    public static OrmClient getOrmClient() {
		return getLocator().getOrmClient();
	}
	
	public static ComponentCache getCache() {
		return getLocator().getCache();
	}

    public static Clock getClock() {
        return getLocator().getClock();
    }

    public static UserService getUserService() {
        return getLocator().getUserService();
    }

    public static EventService getEventService() {
        return getLocator().getEventService();
    }

    private static ServiceLocator getLocator() {
        return locatorHolder.get();
    }
}
