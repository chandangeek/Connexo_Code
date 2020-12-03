/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.soap.whiteboard.cxf;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;

public enum PayloadSaveStrategy implements TranslationKey {
    //the default
    ERRORS("Only for failed occurrences"),
    // others
    ALWAYS("Always"),
    NEVER("Never");

    private String name;

    PayloadSaveStrategy(String name) {
        this.name = name;
    }

    public String getDisplayName(Thesaurus thesaurus) {
        return thesaurus.getString(getKey(), this.getDefaultFormat());
    }

    @Override
    public String getKey() {
        return "webservices.savepayload." + name().toLowerCase();
    }

    @Override
    public String getDefaultFormat() {
        return name;
    }
}
