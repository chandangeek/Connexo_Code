/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;

public enum BypassStatus implements TranslationKey {
    OPEN("bypassStatus.open", "Open"),
    CLOSED("bypassStatus.closed", "Closed"),
    UNKNOWN("bypassStatus.unknown", "Unknown");

    private String key;
    private String value;

    BypassStatus(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public String getDefaultFormat() {
        return value;
    }

    public String getDisplayValue(Thesaurus thesaurus) {
        return thesaurus.getFormat(this).format();
    }

}