package com.elster.jupiter.mdm.usagepoint.data;

import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.nls.TranslationKey;

import java.util.function.Predicate;

public enum ChannelDataValidationSummaryValidFlags implements TranslationKey, IChannelDataValidationSummaryFlag {
    CONFIRMED("statisticsConfirmed", "Confirmed", type -> type.qualityIndex().orElse(null) == QualityCodeIndex.ACCEPTED),
    INFORMATIVE("statisticsInformative", "Informative", type -> type.hasValidationCategory() && !type.isSuspect()),
    ESTIMATED("statisticsEstimated", "Estimated", ReadingQualityType::hasEstimatedCategory),
    VALID("statisticsNotSuspect", "Not suspect", type -> true);

    private String key, translation;
    private Predicate<ReadingQualityType> qualityTypePredicate;

    ChannelDataValidationSummaryValidFlags(String key, String translation, Predicate<ReadingQualityType> qualityTypePredicate) {
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
