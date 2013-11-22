package com.energyict.mdc.common.impl;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Holds on to all service component that are of interest to the mdw.coreimpl module.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-11-05 (15:14)
 */
public class Bus {

    private static AtomicReference<EnvironmentAdapter> environmentAdapter = new AtomicReference<>();

    public static EnvironmentAdapter getEnvironmentAdapter () {
        return environmentAdapter.get();
    }

    public static void setEnvironmentAdapter (EnvironmentAdapter environmentAdapter) {
        Bus.environmentAdapter.set(environmentAdapter);
    }

    public static void clearEnvironmentAdapter (EnvironmentAdapter old) {
        Bus.environmentAdapter.compareAndSet(old, null);
    }

}
