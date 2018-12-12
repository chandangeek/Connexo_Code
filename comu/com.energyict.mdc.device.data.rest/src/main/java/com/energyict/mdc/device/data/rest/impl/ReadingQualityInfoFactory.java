/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.metering.MeteringTranslationService;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.readings.ReadingQuality;

import javax.inject.Inject;

public class ReadingQualityInfoFactory {

    private final MeteringTranslationService meteringTranslationService;

    @Inject
    public ReadingQualityInfoFactory(MeteringTranslationService meteringTranslationService1) {
        this.meteringTranslationService = meteringTranslationService1;
    }

    public ReadingQualityInfo fromReadingQualityType(ReadingQualityType type) {
        ReadingQualityInfo result = new ReadingQualityInfo();
        result.cimCode = type.getCode();
        result.systemName = type.system().map(meteringTranslationService::getDisplayName).orElse("");
        result.categoryName = type.category().map(meteringTranslationService::getDisplayName).orElse("");
        result.indexName = type.qualityIndex().map(meteringTranslationService::getDisplayName).orElse("");
        return result;
    }

    public ReadingQualityInfo fromReadingQuality(ReadingQuality readingQuality) {
        ReadingQualityInfo result = fromReadingQualityType(readingQuality.getType());
        result.comment = readingQuality.getComment();
        return result;
    }
}
