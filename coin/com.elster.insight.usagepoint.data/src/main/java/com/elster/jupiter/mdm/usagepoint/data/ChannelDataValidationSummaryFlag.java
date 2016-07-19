package com.elster.jupiter.mdm.usagepoint.data;

import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;

import java.util.function.Predicate;

public enum ChannelDataValidationSummaryFlag implements TranslationKey {
    // The order is important; each previous one must overrule the next
    // NOT_VALIDATED is processed in another way so must be the last one in order not to be affected
    MISSING("statisticsMissing", "Missing", ReadingQualityType::isMissing),
    SUSPECT("statisticsSuspect", "Suspect", ReadingQualityType::isSuspect),
    ESTIMATED("statisticsEstimated", "Estimated", ReadingQualityType::hasEstimatedCategory),
    EDITED("statisticsEdited", "Edited", ReadingQualityType::hasEditCategory),
    VALID("statisticsValid", "Valid", type -> type.qualityIndex().orElse(null) == QualityCodeIndex.DATAVALID),
    NOT_VALIDATED("statisticsNotValidated", "Not validated", type -> true);

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

    public String getDisplayName(Thesaurus thesaurus) {
        return thesaurus.getString(key, translation);
    }

    public Predicate<ReadingQualityType> getQualityTypePredicate() {
        return qualityTypePredicate;
    }
}
