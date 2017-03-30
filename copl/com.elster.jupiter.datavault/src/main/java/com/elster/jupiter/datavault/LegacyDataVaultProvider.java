/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.datavault;

import java.util.concurrent.atomic.AtomicReference;

public interface LegacyDataVaultProvider {
    public static final AtomicReference<LegacyDataVaultProvider> instance = new AtomicReference<>();

    DataVault getKeyVault();

}
