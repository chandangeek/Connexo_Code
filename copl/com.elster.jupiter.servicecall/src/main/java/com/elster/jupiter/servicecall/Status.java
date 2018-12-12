/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.servicecall;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;

/**
 * Created by bvn on 2/10/16.
 */
public enum Status implements TranslationKey {
    ACTIVE("Active"),
    DEPRECATED("Deprecated");

    private String name;

    Status(String name) {
        this.name = name;
    }

    public String getDisplayName(Thesaurus thesaurus) {
        return thesaurus.getString(getTranslationKey(this), this.getDefaultFormat());
    }

    public static String getTranslationKey(Status status){
        return "servicecalltype.status." + status.name().toLowerCase();
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public String getKey() {
        return getTranslationKey(this);
    }

    @Override
    public String getDefaultFormat() {
        return toString();
    }

}
