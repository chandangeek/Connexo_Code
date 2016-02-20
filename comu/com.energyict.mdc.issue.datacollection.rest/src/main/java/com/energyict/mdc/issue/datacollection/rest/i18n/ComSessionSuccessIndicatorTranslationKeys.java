package com.energyict.mdc.issue.datacollection.rest.i18n;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.energyict.mdc.device.data.tasks.history.ComSession;

import java.util.stream.Stream;

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
        return ComSession.class.getSimpleName() + "." + this.successIndicator.name();
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
