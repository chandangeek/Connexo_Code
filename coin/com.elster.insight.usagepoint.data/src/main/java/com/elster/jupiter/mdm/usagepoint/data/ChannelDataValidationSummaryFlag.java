package com.elster.jupiter.mdm.usagepoint.data;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;

public enum ChannelDataValidationSummaryFlag implements TranslationKey {
    NOT_VALIDATED("statisticsNotValidated", "Not validated"),
    VALID("statisticsValid", "Valid"),
    SUSPECT("statisticsSuspect", "Suspect"),
    MISSING("statisticsMissing", "Missing"),
    EDITED("statisticsEdited", "Edited"),
    ESTIMATED("statisticsEstimated", "Estimated");

    private String key, translation;

    ChannelDataValidationSummaryFlag(String key, String translation) {
        this.key = key;
        this.translation = translation;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getDefaultFormat() {
        return translation;
    }

    public String getDisplayName(Thesaurus thesaurus) {
        return thesaurus.getString(key, translation);
    }
}
