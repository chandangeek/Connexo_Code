/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data;

import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.metering.ReadingQualityType;

import java.util.function.Predicate;

public enum ValidChannelDataSummaryFlags implements IChannelDataCompletionSummaryFlag {
    CONFIRMED("statisticsConfirmed", "Confirmed", type -> type.qualityIndex().orElse(null) == QualityCodeIndex.ACCEPTED),
    ESTIMATED("statisticsEstimated", "Estimated", ReadingQualityType::hasEstimatedCategory),
    INFORMATIVE("statisticsInformative", "Informative", ReadingQualityType::hasValidationCategory),
    // VALID is processed in another way so predicate is not important
    VALID("statisticsNotSuspect", "Not suspect", type -> true);

    private String key, translation;
    private Predicate<ReadingQualityType> qualityTypePredicate;

    ValidChannelDataSummaryFlags(String key, String translation, Predicate<ReadingQualityType> qualityTypePredicate) {
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
