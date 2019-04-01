/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.search;

import com.elster.jupiter.nls.TranslationKey;
import com.energyict.mdc.device.config.ConnectionStrategy;

public enum ConnectionStrategyTranslationKeys{

    AS_SOON_AS_POSSIBLE(ConnectionStrategy.AS_SOON_AS_POSSIBLE, PropertyTranslationKeys.CONNECTION_TASK_STRATEGY_AS_SOON_AS_POSSIBLE),
    MINIMIZE_CONNECTIONS(ConnectionStrategy.MINIMIZE_CONNECTIONS, PropertyTranslationKeys.CONNECTION_TASK_STRATEGY_MINIMIZE_CONNECTIONS);

    private final ConnectionStrategy connectionStrategy;
    private final TranslationKey defaultFormat;

    ConnectionStrategyTranslationKeys(ConnectionStrategy connectionStrategy, TranslationKey defaultFormat) {
        this.connectionStrategy = connectionStrategy;
        this.defaultFormat = defaultFormat;
    }

    public ConnectionStrategy getStatus() {
        return connectionStrategy;
    }

    public TranslationKey translation() {
        return this.defaultFormat;
    }
}
