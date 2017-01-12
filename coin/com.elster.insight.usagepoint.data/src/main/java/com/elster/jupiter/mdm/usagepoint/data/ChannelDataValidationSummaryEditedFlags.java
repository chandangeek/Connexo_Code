package com.elster.jupiter.mdm.usagepoint.data;

import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.nls.TranslationKey;

import java.util.function.Predicate;

public enum ChannelDataValidationSummaryEditedFlags implements TranslationKey, IChannelDataValidationSummaryFlag {
    ADDED("statisticsAdded", "Added", type -> type.qualityIndex().orElse(null) == QualityCodeIndex.ADDED),
    EDITED("statisticsEdited", "Edited", type -> type.qualityIndex().orElse(null) == QualityCodeIndex.EDITGENERIC),
    REMOVED("statisticsRemoved", "Removed", type -> type.qualityIndex().orElse(null) == QualityCodeIndex.REJECTED);

    private String key, translation;
    private Predicate<ReadingQualityType> qualityTypePredicate;

    ChannelDataValidationSummaryEditedFlags(String key, String translation, Predicate<ReadingQualityType> qualityTypePredicate) {
        this.key = key;
        this.translation = translation;
        this.qualityTypePredicate = qualityTypePredicate;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getDefaultFormat() {
        return translation;
    }

    public Predicate<ReadingQualityType> getQualityTypePredicate() {
        return qualityTypePredicate;
    }
}
