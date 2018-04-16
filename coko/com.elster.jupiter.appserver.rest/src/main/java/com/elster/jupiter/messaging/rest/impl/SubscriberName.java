package com.elster.jupiter.messaging.rest.impl;

import com.elster.jupiter.nls.TranslationKey;

class SubscriberName implements TranslationKey {

    private String key;
    private String value;

    SubscriberName(String value) {
        this.key = value;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public String getDefaultFormat() {
        return value;
    }

    public static SubscriberName from(String value) {
        return new SubscriberName(value);
    }

}
