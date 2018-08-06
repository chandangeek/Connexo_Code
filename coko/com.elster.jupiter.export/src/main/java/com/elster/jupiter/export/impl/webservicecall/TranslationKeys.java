/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.impl.webservicecall;

import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.servicecall.ServiceCall;

public enum TranslationKeys implements TranslationKey {
    SERVICE_CALL_TYPE_NAME("data.export.web.serviceCall.type.name", "Export confirmation waiting"),
    SERVICE_CALL_CPS_NAME(WebServiceDataExportCustomPropertySet.CUSTOM_PROPERTY_SET_ID, "Data export web service call properties"),
    SERVICE_CALL_CPS_DOMAIN_NAME(ServiceCall.class.getSimpleName(), "Service call"),

    UUID("uuid", "UUID"),
    TIMEOUT("timeout", "Timeout"),
    ERROR_MESSAGE("errorMessage", "Error message");

    private String key;
    private String defaultFormat;

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
}
