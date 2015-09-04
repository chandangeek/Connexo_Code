package com.energyict.mdc.dashboard.rest.status.impl;

import com.energyict.mdc.device.data.tasks.history.ComSession;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;

import java.util.stream.Stream;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-09-04 (10:23)
 */
public enum ComSessionSuccessIndicatorTranslationKeys implements TranslationKey {

    SUCCESS(ComSession.SuccessIndicator.Success, "Success"),
    BROKEN(ComSession.SuccessIndicator.Broken, "Broken"),
    SETUP_ERROR(ComSession.SuccessIndicator.SetupError, "Setup error");

    private ComSession.SuccessIndicator successIndicator;
    private String defaultFormat;

    ComSessionSuccessIndicatorTranslationKeys(ComSession.SuccessIndicator successIndicator, String defaultFormat) {
        this.successIndicator = successIndicator;
        this.defaultFormat = defaultFormat;
    }

    @Override
    public String getKey() {
        return ComSession.SuccessIndicator.class.getSimpleName() + "." + this.successIndicator.name();
    }

    @Override
    public String getDefaultFormat() {
        return this.defaultFormat;
    }

    public static ComSessionSuccessIndicatorTranslationKeys from(ComSession.SuccessIndicator successIndicator) {
        return Stream
                .of(values())
                .filter(each -> each.successIndicator.equals(successIndicator))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("Translation missing for success indicator: " + successIndicator));
    }

    public static String translationFor(ComSession.SuccessIndicator successIndicator, Thesaurus thesaurus) {
        return thesaurus.getFormat(from(successIndicator)).format();
    }

}