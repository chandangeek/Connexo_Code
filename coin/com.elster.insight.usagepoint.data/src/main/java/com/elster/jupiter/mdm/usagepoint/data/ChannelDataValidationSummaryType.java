package com.elster.jupiter.mdm.usagepoint.data;

import com.elster.jupiter.nls.TranslationKey;

public enum ChannelDataValidationSummaryType implements TranslationKey {
    GENERAL("statisticsGeneral", "General"),
    EDITED("statisticsEdited", "Edited"),
    VALID("statisticsValid", "Valid");

    private String key, translation;

    ChannelDataValidationSummaryType(String key, String translation) {
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
}
