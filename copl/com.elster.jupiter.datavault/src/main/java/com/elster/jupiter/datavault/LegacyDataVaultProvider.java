package com.elster.jupiter.datavault;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Copyrights EnergyICT
 *
 * @since 9/6/12 3:31 PM
 */
public interface LegacyDataVaultProvider {
    public static final AtomicReference<LegacyDataVaultProvider> instance = new AtomicReference<>();

    DataVault getKeyVault();

}
