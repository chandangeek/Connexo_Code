package com.elster.jupiter.mdm.usagepoint.data;

import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.nls.TranslationKey;

import java.util.function.Predicate;

import static com.elster.jupiter.util.streams.Predicates.either;

public enum ChannelDataValidationSummaryFlag implements TranslationKey, IChannelDataValidationSummaryFlag {
    // NOT_VALIDATED and VALID is processed in another way so predicate is not used
    NOT_VALIDATED("statisticsNotValidated", "Not validated", type -> true),
    SUSPECT("statisticsSuspect", "Suspect", either(ReadingQualityType::isSuspect).or(ReadingQualityType::isError)),
    VALID("statisticsValid", "Valid", type -> true);

    private String key, translation;
    private Predicate<ReadingQualityType> qualityTypePredicate;

    ChannelDataValidationSummaryFlag(String key, String translation, Predicate<ReadingQualityType> qualityTypePredicate) {
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
