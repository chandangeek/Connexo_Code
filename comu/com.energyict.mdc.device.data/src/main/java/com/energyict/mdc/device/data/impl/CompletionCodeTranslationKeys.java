package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.energyict.mdc.device.data.tasks.history.CompletionCode;

import java.util.stream.Stream;

public enum CompletionCodeTranslationKeys implements TranslationKey {

    OK(CompletionCode.Ok, "Successful"),
    NOT_EXECUTED(CompletionCode.NotExecuted, "Not executed"),
    CONFIGURATION_WARNING(CompletionCode.ConfigurationWarning, "Configuration warning"),
    CONFIGURATION_ERROR(CompletionCode.ConfigurationError, "Configuration error"),
    PROTOCOL_ERROR(CompletionCode.ProtocolError, "Protocol error"),
    TIME_ERROR(CompletionCode.TimeError, "Time error"),
    CONNECTION_ERROR(CompletionCode.ConnectionError, "Connection error"),
    UNEXPECTED_ERROR(CompletionCode.UnexpectedError, "Unexpected error"),
    IO_ERROR(CompletionCode.IOError, "I/O error");

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
