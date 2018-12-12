/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;

public enum UsagePointModelTranslationKeys implements TranslationKey {
    NAME_MODEL("name", "Name"),
    SERVICE_CATEGORY_MODEL("displayServiceCategory", "Service category"),
    METROLOGY_CONFIGURATION_MODEL("displayMetrologyConfiguration", "Metrology configuration"),
    TYPE_MODEL("displayType", "Type"),
    CONNECTION_STATE_MODEL("displayConnectionState", "Connection state"),
    LOCATION_MODEL("location", "Location"),
    STATE("state", "State");
    private final String key;
    private final String defaultFormat;

    UsagePointModelTranslationKeys(String key, String defaultFormat) {
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

    public String getDisplayName(Thesaurus thesaurus) {
        return thesaurus.getFormat(this).format();
    }

}
