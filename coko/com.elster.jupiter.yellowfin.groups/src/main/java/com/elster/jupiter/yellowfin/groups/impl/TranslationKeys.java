/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.yellowfin.groups.impl;

import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.yellowfin.groups.YellowfinGroupsService;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-09-26 (10:37)
 */
public enum TranslationKeys implements TranslationKey {
    ADHOC_SEARCH_SUBSCRIBER(YellowfinGroupsService.ADHOC_SEARCH_LIFE_CYCLE_QUEUE_DEST, YellowfinGroupsService.ADHOC_SEARCH_LIFE_CYCLE_QUEUE_DEST_DISPLAYNAME);

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