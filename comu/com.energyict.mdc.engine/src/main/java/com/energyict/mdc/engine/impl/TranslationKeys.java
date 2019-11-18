/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.energyict.mdc.engine.impl.status.ComServerAliveStatusHandlerFactory;

public enum TranslationKeys implements TranslationKey {

    COM_SERVER_STATUS_TASK_NAME_TRANSLATION(ComServerAliveStatusHandlerFactory.COM_SERVER_ALIVE_TIMEOUT_TASK_SUBSCRIBER, "ComServer status");

    private final String key;
    private final String defaultFormat;

    TranslationKeys(String key, String defaultFormat) {
        this.key = key;
        this.defaultFormat = defaultFormat;
    }

    @Override
    public String getKey() {
        return this.key;
    }

    @Override
    public String getDefaultFormat() {
        return this.defaultFormat;
    }

    public String getTranslated(Thesaurus thesaurus) {
        return thesaurus.getFormat(this).format();
    }
}
