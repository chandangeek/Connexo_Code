package com.energyict.mdc.dynamic.relation.impl;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-12-17 (10:39)
 */
public enum Bus {
    ;

    private static AtomicReference<ServiceLocator> serviceLocatorHolder = new AtomicReference<>();

    public static ServiceLocator getServiceLocator() {
        return serviceLocatorHolder.get();
    }

    static void setServiceLocator(ServiceLocator serviceLocator) {
        serviceLocatorHolder.set(Objects.requireNonNull(serviceLocator));
    }

    static void clearServiceLocator(ServiceLocator oldServiceLocator) {
        serviceLocatorHolder.compareAndSet(Objects.requireNonNull(oldServiceLocator), null);
    }

}