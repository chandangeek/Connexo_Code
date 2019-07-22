/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.webservices.impl;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.util.exception.MessageSeed;

import java.util.Arrays;
import java.util.stream.Collectors;

public class SAPWebServiceException extends LocalizedException {
    public SAPWebServiceException(Thesaurus thesaurus, MessageSeed messageSeed) {
        super(thesaurus, messageSeed);
    }

    public SAPWebServiceException(Thesaurus thesaurus, MessageSeed messageSeed, Object... args) {
        super(thesaurus, messageSeed, args);
    }

    public SAPWebServiceException(Thesaurus thesaurus, MessageSeed messageSeed, Throwable cause) {
        super(thesaurus, messageSeed, cause);
    }

    public SAPWebServiceException(Thesaurus thesaurus, MessageSeed messageSeed, Throwable cause, Object... args) {
        super(thesaurus, messageSeed, cause, args);
    }

    public static SAPWebServiceException endpointsNotProcessed(Thesaurus thesaurus, EndPointConfiguration... endPointConfigurations) {
        return new SAPWebServiceException(thesaurus, MessageSeeds.WEB_SERVICE_ENDPOINTS_NOT_PROCESSED, Arrays.stream(endPointConfigurations)
                .map(EndPointConfiguration::getName)
                .collect(Collectors.joining(", ")));
    }
}
