package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.metering.MeteringTranslationService;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.readings.ReadingQuality;

import javax.inject.Inject;

public class ReadingQualityInfoFactory {

    private final MeteringTranslationService meteringTranslationService;

    @Inject
    public ReadingQualityInfoFactory(MeteringTranslationService meteringTranslationService) {
        this.meteringTranslationService = meteringTranslationService;
    }

    public ReadingQualityInfo asInfo(ReadingQuality readingQuality) {
        ReadingQualityInfo info = new ReadingQualityInfo();
        ReadingQualityType type = readingQuality.getType();
        info.cimCode = type.getCode();
        info.systemName = type.system().map(meteringTranslationService::getDisplayName).orElse("");
        info.categoryName = type.category().map(meteringTranslationService::getDisplayName).orElse("");
        info.indexName = type.qualityIndex().map(meteringTranslationService::getDisplayName).orElse("");
        info.comment = readingQuality.getComment();
        return info;
    }
}
