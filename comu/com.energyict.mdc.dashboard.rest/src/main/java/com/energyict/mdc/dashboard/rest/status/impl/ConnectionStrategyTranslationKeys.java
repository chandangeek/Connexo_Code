package com.energyict.mdc.dashboard.rest.status.impl;

import com.energyict.mdc.device.config.ConnectionStrategy;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;

import java.util.stream.Stream;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-09-04 (12:42)
 */
public enum ConnectionStrategyTranslationKeys implements TranslationKey {

    AS_SOON_AS_POSSIBLE(ConnectionStrategy.AS_SOON_AS_POSSIBLE, "As soon a possible"),
    MINIMIZE_CONNECTIONS(ConnectionStrategy.MINIMIZE_CONNECTIONS, "Minimize connections");

    private final ConnectionStrategy connectionStrategy;
    private final String defaultFormat;

    ConnectionStrategyTranslationKeys(ConnectionStrategy connectionStrategy, String defaultFormat) {
        this.connectionStrategy = connectionStrategy;
        this.defaultFormat = defaultFormat;
    }

    @Override
    public String getKey() {
        return ConnectionStrategy.class.getSimpleName() + "." + this.connectionStrategy.name();
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