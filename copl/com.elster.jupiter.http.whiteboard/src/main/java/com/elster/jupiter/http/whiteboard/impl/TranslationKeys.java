package com.elster.jupiter.http.whiteboard.impl;

import com.elster.jupiter.nls.TranslationKey;

/**
 * Created by Jozsef Szekrenyes on 11/5/2018.
 */
public enum TranslationKeys implements TranslationKey {
    SUBSCRIBER_DISPLAYNAME("WhiteboardSubscriber", "Handle user authentication events");

    private String key;
    private String defaultFormat;

    TranslationKeys(String key, String defaultFormat) {
        this.key = key;
        this.defaultFormat = defaultFormat;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getDefaultFormat() {
        return defaultFormat;
    }
}
