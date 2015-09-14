package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.device.data.tasks.ConnectionTask;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;

import java.util.stream.Stream;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-09-04 (10:23)
 */
public enum ConnectionTaskSuccessIndicatorTranslationKeys implements TranslationKey {

    SUCCESS(ConnectionTask.SuccessIndicator.SUCCESS, "Success"),
    FAILURE(ConnectionTask.SuccessIndicator.FAILURE, "Failure"),
    NOT_APPLICABLE(ConnectionTask.SuccessIndicator.NOT_APPLICABLE, "Not applicable");

    private ConnectionTask.SuccessIndicator successIndicator;
    private String defaultFormat;

    ConnectionTaskSuccessIndicatorTranslationKeys(ConnectionTask.SuccessIndicator successIndicator, String defaultFormat) {
        this.successIndicator = successIndicator;
        this.defaultFormat = defaultFormat;
    }

    @Override
    public String getKey() {
        return this.successIndicator.name();
    }

    @Override
    public String getDefaultFormat() {
        return this.defaultFormat;
    }

    public static ConnectionTaskSuccessIndicatorTranslationKeys from(ConnectionTask.SuccessIndicator successIndicator) {
        return Stream
                .of(values())
                .filter(each -> each.successIndicator.equals(successIndicator))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("Translation missing for success indicator: " + successIndicator));
    }

    public static String translationFor(ConnectionTask.SuccessIndicator successIndicator, Thesaurus thesaurus) {
        return thesaurus.getFormat(from(successIndicator)).format();
    }

}