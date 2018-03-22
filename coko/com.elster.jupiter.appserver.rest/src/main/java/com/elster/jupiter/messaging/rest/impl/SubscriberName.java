package com.elster.jupiter.messaging.rest.impl;

import com.elster.jupiter.nls.TranslationKey;

public class SubscriberName implements TranslationKey {

    private String key;
    private String value;

    public SubscriberName(String name) {
        this.key = name;
        this.value = name;
    }

    public String getKey() {
        return key;
    }

    public String getDefaultFormat() {
        return value;
    }

}
