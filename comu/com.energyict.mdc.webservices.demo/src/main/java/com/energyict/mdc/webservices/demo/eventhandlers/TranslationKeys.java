/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.webservices.demo.eventhandlers;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;

public enum TranslationKeys implements TranslationKey {
    SAP_EVENT_MAPPING_STATUS_CPS(SAPDeviceEventMappingStatusCustomPropertySet.CUSTOM_PROPERTY_SET_ID, "SAP device event mapping status"),
    SAP_EVENT_MAPPING_LOADING_SC_TYPE("SAPEventMappingLoadingServiceCallType", "Loading of SAP device event mapping csv")

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

    public String translate(Thesaurus thesaurus) {
        return thesaurus.getFormat(this).format();
    }
}
