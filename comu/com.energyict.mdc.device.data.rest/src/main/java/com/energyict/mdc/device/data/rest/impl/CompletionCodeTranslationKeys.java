/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.energyict.mdc.device.data.tasks.history.CompletionCode;

import java.util.stream.Stream;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-09-04 (10:23)
 */
public enum CompletionCodeTranslationKeys implements TranslationKey {

    OK(CompletionCode.Ok, "Successful"),
    CONFIGURATION_WARNING(CompletionCode.ConfigurationWarning, "Configuration warning"),
    NOT_EXECUTED(CompletionCode.NotExecuted, "Not executed"),
    PROTOCOL_ERROR(CompletionCode.ProtocolError, "Protocol error"),
    CONFIGURATION_ERROR(CompletionCode.ConfigurationError, "Configuration error"),
    IO_ERROR(CompletionCode.IOError, "I/O error"),
    UNEXPECTED_ERROR(CompletionCode.UnexpectedError, "Unexpected error"),
    TIME_ERROR(CompletionCode.TimeError, "Time error"),
    INIT_ERROR(CompletionCode.InitError, "Initialization error"),
    TIMEOUT_ERROR(CompletionCode.TimeoutError, "Timeout error"),
    CONNECTION_ERROR(CompletionCode.ConnectionError, "Connection error")
    ;

    private CompletionCode completionCode;
    private String defaultFormat;

    CompletionCodeTranslationKeys(CompletionCode completionCode, String defaultFormat) {
        this.completionCode = completionCode;
        this.defaultFormat = defaultFormat;
    }

    @Override
    public String getKey() {
        return this.completionCode.name();
    }

    @Override
    public String getDefaultFormat() {
        return this.defaultFormat;
    }

    public static CompletionCodeTranslationKeys from(CompletionCode completionCode) {
        return Stream
                .of(values())
                .filter(each -> each.completionCode.equals(completionCode))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("Translation missing for completion code: " + completionCode));
    }

    public static String translationFor(CompletionCode completionCode, Thesaurus thesaurus) {
        return thesaurus.getFormat(from(completionCode)).format();
    }

}