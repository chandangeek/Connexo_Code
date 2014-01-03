package com.energyict.mdc.common;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Copyrights EnergyICT
 *
 * @since 9/6/12 3:31 PM
 */
public interface DataVaultProvider {
    public static final AtomicReference<DataVaultProvider> instance = new AtomicReference<>();

    DataVault getKeyVault();

}
