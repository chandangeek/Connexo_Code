package com.energyict.mdc.multisense.api.impl.utils;

import com.elster.jupiter.nls.TranslationKey;

/**
 * Created by bvn on 7/13/15.
 */
public enum TranslationSeeds implements TranslationKey {


    CONNECTION_TASK_STATUS_INCOMPLETE("Incomplete" , "Incomplete"),
    CONNECTION_TASK_STATUS_ACTIVE("Active", "Active"),
    CONNECTION_TASK_STATUS_INACTIVE("Inactive", "Inactive"),
    MINIMIZE_CONNECTIONS("MinimizeConnections", "Minimize connections"),
    AS_SOON_AS_POSSIBLE("AsSoonAsPossible", "As soon as possible"),
    ;
    private String key;
    private final String defaultFormat;

    private TranslationSeeds(String key, String defaultFormat) {
        this.key = key;
        this.defaultFormat = defaultFormat;
    }

    public String getKey() {
        return key;
    }

    @Override
    public String getDefaultFormat() {
        return defaultFormat;
    }
}
