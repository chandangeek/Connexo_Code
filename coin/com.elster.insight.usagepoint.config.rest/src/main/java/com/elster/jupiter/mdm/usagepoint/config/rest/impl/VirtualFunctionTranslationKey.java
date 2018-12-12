/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.config.rest.impl;

import com.elster.jupiter.nls.TranslationKey;

/**
 * Contains an entry for every virtual function
 * to provide a {@link TranslationKey} for that virtual function.
 * Virtual functions do not really exist, i.e. they are implicitely
 * done by the data aggregation service but the UI want to make them
 * explicit to explain what the data aggregation service is or will to.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-04-05 (11:41)
 */
enum VirtualFunctionTranslationKey implements TranslationKey {

    TOU("function.virtual.tou", "Time of use({0}, {1})");

    private final String key;
    private final String defaultFormat;

    VirtualFunctionTranslationKey(String key, String defaultFormat) {
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