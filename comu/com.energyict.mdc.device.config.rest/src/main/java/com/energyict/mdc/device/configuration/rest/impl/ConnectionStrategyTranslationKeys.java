/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.energyict.mdc.common.device.config.ConnectionStrategy;

import java.util.stream.Stream;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-09-04 (12:42)
 */
public enum ConnectionStrategyTranslationKeys implements TranslationKey {

    AS_SOON_AS_POSSIBLE(ConnectionStrategy.AS_SOON_AS_POSSIBLE, "As soon as possible"),
    MINIMIZE_CONNECTIONS(ConnectionStrategy.MINIMIZE_CONNECTIONS, "Minimize connections");

    private final ConnectionStrategy connectionStrategy;
    private final String defaultFormat;

    ConnectionStrategyTranslationKeys(ConnectionStrategy connectionStrategy, String defaultFormat) {
        this.connectionStrategy = connectionStrategy;
        this.defaultFormat = defaultFormat;
    }

    @Override
    public String getKey() {
        return this.connectionStrategy.name();
    }

    @Override
    public String getDefaultFormat() {
        return defaultFormat;
    }

    public static ConnectionStrategyTranslationKeys from(ConnectionStrategy connectionStrategy) {
        return Stream
                .of(values())
                .filter(each -> each.connectionStrategy.equals(connectionStrategy))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("Translation missing for connection strategy: " + connectionStrategy));
    }

    public static String translationFor(ConnectionStrategy connectionStrategy, Thesaurus thesaurus) {
        return thesaurus.getFormat(from(connectionStrategy)).format();
    }

}