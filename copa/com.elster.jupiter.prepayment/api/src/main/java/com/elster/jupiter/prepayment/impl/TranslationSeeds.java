/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.prepayment.impl;

import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.servicecall.ServiceCall;

/**
 * Created by bvn on 7/13/15.
 */
public enum TranslationSeeds implements TranslationKey {

    CPS_DOMAIN_NAME(ServiceCall.class.getName(), "Service call"),
    CONNECTION_TASK_STATUS_INCOMPLETE("Incomplete" , "Incomplete"),
    CONNECTION_TASK_STATUS_ACTIVE("Active", "Active"),
    CONNECTION_TASK_STATUS_INACTIVE("Inactive", "Inactive"),
    MINIMIZE_CONNECTIONS("MinimizeConnections", "Minimize connections"),
    AS_SOON_AS_POSSIBLE("AsSoonAsPossible", "As soon as possible"),
    CALL_BACK_URL("callback", "Callback URL"),
    PROVIDED_RESPONSE("providedResponse", "Provided response"),
    BREAKER_STATUS("breakerStatus", "Breaker status"),
    COMPLETION_OPTIONS_MESSAGE_HANDLER(CompletionOptionsMessageHandlerFactory.COMPLETION_OPTIONS_TASK_SUBSCRIBER, CompletionOptionsMessageHandlerFactory.COMPLETION_OPTIONS_TASK_SUBSCRIBER_DISPLAYNAME)
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
