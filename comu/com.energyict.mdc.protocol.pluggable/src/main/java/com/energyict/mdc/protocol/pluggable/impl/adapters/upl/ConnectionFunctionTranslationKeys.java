/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.pluggable.impl.adapters.upl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.energyict.mdc.upl.UPLConnectionFunction;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * @author Stijn Vanhoorelbeke
 * @since 15.06.17 - 10:54
 */
public enum ConnectionFunctionTranslationKeys implements TranslationKey {

    Gateway("ConnectionFunction.gateway", "Gateway"),
    Mirror("ConnectionFunction.mirror", "Mirror"),
    Inbound("ConnectionFunction.inbound", "Inbound");

    private static final String TRANSLATION_KEY_PREFIX = "ConnectionFunction.";

    private String translationKey;
    private String defaultFormat;

    ConnectionFunctionTranslationKeys(String translationKey, String defaultFormat) {
        this.translationKey = translationKey;
        this.defaultFormat = defaultFormat;
    }

    public String getTranslationKey() {
        return translationKey;
    }

    @Override
    public String getKey() {
        return translationKey;
    }

    @Override
    public String getDefaultFormat() {
        return this.defaultFormat;
    }

    public static Optional<ConnectionFunctionTranslationKeys> from(UPLConnectionFunction connectionFunction) {
        String translationKey = TRANSLATION_KEY_PREFIX + connectionFunction.getConnectionFunctionName().toLowerCase();
        return Stream
                .of(values())
                .filter(each -> each.translationKey.equals(translationKey))
                .findAny();
    }

    public static String translationFor(UPLConnectionFunction connectionFunction, Thesaurus thesaurus) {
        Optional<ConnectionFunctionTranslationKeys> translationKey = from(connectionFunction);
        return translationKey.isPresent() ? thesaurus.getFormat(translationKey.get()).format() : connectionFunction.getConnectionFunctionName();
    }
}