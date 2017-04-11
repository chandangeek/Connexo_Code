/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.kore.api.impl;

import com.elster.jupiter.kore.api.impl.servicecall.UsagePointCommandMessageHandlerFactory;
import com.elster.jupiter.nls.TranslationKey;

public enum TranslationKeys implements TranslationKey {

    USAGE_POINT_COMMAND_MESSAGE_HANDLER_DISPLAYNAME(UsagePointCommandMessageHandlerFactory.SUBSCRIBER_NAME, UsagePointCommandMessageHandlerFactory.SUBSCRIBER_DISPLAYNAME),
    ;

    private final String key;
    private final String defaultFormat;

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
