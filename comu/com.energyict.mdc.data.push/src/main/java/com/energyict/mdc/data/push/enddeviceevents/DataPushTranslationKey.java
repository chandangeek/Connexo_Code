/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.data.push.enddeviceevents;

import com.elster.jupiter.nls.TranslationKey;

public enum DataPushTranslationKey implements TranslationKey {
    EVENT_SUBSCRIBER(EndDeviceEventMessageHandlerFactory.SUBSCRIBER_NAME, EndDeviceEventMessageHandlerFactory.SUBSCRIBER_DISPLAY_NAME);

    private final String key;
    private final String defaultFormat;

    DataPushTranslationKey(String key, String defaultFormat) {
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
