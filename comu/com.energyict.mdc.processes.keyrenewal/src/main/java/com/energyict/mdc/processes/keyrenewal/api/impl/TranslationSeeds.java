/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.processes.keyrenewal.api.impl;

import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.servicecall.ServiceCall;
import com.energyict.mdc.processes.keyrenewal.api.impl.csr.CertificateRequestForCSRHandlerFactory;

public enum TranslationSeeds implements TranslationKey {

    CPS_DOMAIN_NAME(ServiceCall.class.getName(), "Service call"),
    CALL_BACK_SUCCESS_URL("callbackSuccess", "Callback success URL"),
    CALL_BACK_ERROR_URL("callbackError", "Callback error URL"),
    PROVIDED_RESPONSE("providedResponse", "Provided response"),
    COMPLETION_OPTIONS_MESSAGE_HANDLER(CompletionOptionsMessageHandlerFactory.COMPLETION_OPTIONS_TASK_SUBSCRIBER, CompletionOptionsMessageHandlerFactory.COMPLETION_OPTIONS_TASK_SUBSCRIBER_DISPLAYNAME),
    CERTIFICATE_REQUEST_FOR_CSR_MESSAGE_HANDLER(CertificateRequestForCSRHandlerFactory.CERTIFICATE_REQUEST_FOR_CSR_TASK_SUBSCRIBER, CertificateRequestForCSRHandlerFactory.CERTIFICATE_REQUEST_FOR_CSR_TASK_SUBSCRIBER_DISPLAYNAME),
    ;
    private String key;
    private final String defaultFormat;

    TranslationSeeds(String key, String defaultFormat) {
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
