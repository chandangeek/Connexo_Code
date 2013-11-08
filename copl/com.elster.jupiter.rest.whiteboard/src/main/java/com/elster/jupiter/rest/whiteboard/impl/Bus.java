package com.elster.jupiter.rest.whiteboard.impl;

import com.elster.jupiter.pubsub.Publisher;
import com.elster.jupiter.rest.whiteboard.RestCallExecutedEvent;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.users.UserService;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

class Bus {
    static final String PID = "com.elster.jupiter.rest.whiteboard";

    private static AtomicReference<ServiceLocator> locatorHolder = new AtomicReference<>();

    public static void setServiceLocator(ServiceLocator locator) {
        Bus.locatorHolder.set(Objects.requireNonNull(locator));
    }

    public static void clearServiceLocator(ServiceLocator old) {
        locatorHolder.compareAndSet(Objects.requireNonNull(old), null);
    }

    public static Publisher getPublisher() {
        return getLocator().getPublisher();
    }

    static void fire(RestCallExecutedEvent event) {
        getLocator().fire(event);
    }

    static ThreadPrincipalService getThreadPrincipalService() {
        return getLocator().getThreadPrincipalService();
    }

    static UserService getUserService() {
        return getLocator().getUserService();
    }

    private Bus() {
        throw new UnsupportedOperationException();
    }

    private static ServiceLocator getLocator() {
        return locatorHolder.get();
    }
}

	