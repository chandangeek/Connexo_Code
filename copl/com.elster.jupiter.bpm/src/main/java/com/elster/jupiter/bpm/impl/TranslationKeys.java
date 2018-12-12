/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.bpm.impl;

import com.elster.jupiter.bpm.BpmAppService;
import com.elster.jupiter.nls.TranslationKey;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-09-26 (09:49)
 */
public enum TranslationKeys implements TranslationKey {
    APPLICATION(BpmAppService.APPLICATION_KEY, BpmAppService.APPLICATION_NAME),
    QUEUE_SUBSCRIBER(BpmServiceImpl.BPM_QUEUE_SUBSC, BpmServiceImpl.BPM_QUEUE_DISPLAYNAME);

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

}