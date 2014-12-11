package com.energyict.protocols.mdc.services.impl;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Provides access to third party services that were injected
 * at the time the protocols bundle was activated.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-09 (11:17)
 */
public final class Bus {

    private static AtomicReference<OrmClient> ormClientProvider = new AtomicReference<>();

    public static OrmClient getOrmClient() {
        return ormClientProvider.get();
    }

    public static void setOrmClient(OrmClient ormClient) {
        ormClientProvider.set(ormClient);
    }

    public static void clearOrmClient(OrmClient old) {
        ormClientProvider.compareAndSet(old, null);
    }

}