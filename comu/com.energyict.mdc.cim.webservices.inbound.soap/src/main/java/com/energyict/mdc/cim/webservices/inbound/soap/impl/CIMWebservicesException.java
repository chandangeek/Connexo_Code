/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.cim.webservices.inbound.soap.impl;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

import java.util.function.Supplier;

public class CIMWebservicesException extends LocalizedException implements Supplier<CIMWebservicesException> {
    protected CIMWebservicesException(Thesaurus thesaurus, MessageSeed messageSeed, Object... args) {
        super(thesaurus, messageSeed, args);
    }

    @Override
    public CIMWebservicesException get() {
        return this;
    }

    public static CIMWebservicesException missingElement(Thesaurus thesaurus, String element) {
        return new CIMWebservicesException(thesaurus, MessageSeeds.MISSING_ELEMENT, element);
    }

    public static CIMWebservicesException emptyElement(Thesaurus thesaurus, String element) {
        return new CIMWebservicesException(thesaurus, MessageSeeds.EMPTY_ELEMENT, element);
    }

    public static CIMWebservicesException emptyList(Thesaurus thesaurus, String listName) {
        return new CIMWebservicesException(thesaurus, MessageSeeds.EMPTY_LIST, listName);
    }

    public static CIMWebservicesException unsupportedSyncMode(Thesaurus thesaurus) {
        return new CIMWebservicesException(thesaurus, MessageSeeds.SYNC_MODE_NOT_SUPPORTED_GENERAL);
    }

    public static CIMWebservicesException missingEndpoint(Thesaurus thesaurus, String endpointName, String url) {
        return new CIMWebservicesException(thesaurus, MessageSeeds.MISSING_ENDPOINT, endpointName, url);
    }

    public static CIMWebservicesException noRequestWithCorrelationId(Thesaurus thesaurus, String correlationId) {
        return new CIMWebservicesException(thesaurus, MessageSeeds.NO_REQUEST_WITH_CORRELATION_ID, correlationId);
    }
}
