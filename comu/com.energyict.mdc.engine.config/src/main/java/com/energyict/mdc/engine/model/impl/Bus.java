package com.energyict.mdc.engine.model.impl;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class Bus {
    private static AtomicReference<ServiceLocator> serviceLocatorHolder = new AtomicReference<>();

    public static ServiceLocator getServiceLocator() {
        return serviceLocatorHolder.get();
    }

    public static void setServiceLocator(ServiceLocator serviceLocator) {
        serviceLocatorHolder.set(Objects.requireNonNull(serviceLocator));
    }

    public static void clearServiceLocator(ServiceLocator oldServiceLocator) {
        serviceLocatorHolder.compareAndSet(Objects.requireNonNull(oldServiceLocator), null);
    }
}
