/*
 * Copyright (c) 2021 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.custom.meterreadingdocument;

import com.elster.jupiter.nls.TranslationKey;

public enum TranslationKeys implements TranslationKey {
    SAP_METER_READING_DOCUMENT_EVENT_SUBSCRIBER(SapMeterReadingDocumentOnDemandHandlerFactory.SUBSCRIBER_NAME,SapMeterReadingDocumentOnDemandHandlerFactory.SUBSCRIBER_DISPLAYNAME);

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
