/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.usagepoint.data.rest.impl;

import com.elster.jupiter.metering.MeteringTranslationService;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.readings.ReadingQuality;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by dantonov on 30.03.2017.
 */
public class ReadingQualityInfoFactory {

    private final MeteringTranslationService meteringTranslationService;

    @Inject
    public ReadingQualityInfoFactory(MeteringTranslationService meteringTranslationService) {
        this.meteringTranslationService = meteringTranslationService;
    }

    public List<ReadingQualityInfo> asInfos(List<? extends ReadingQuality> readingQualities) {
        return readingQualities.stream()
                .map(ReadingQuality::getType)
                .distinct()
                .filter(type -> type.system().isPresent())
                .filter(type -> type.category().isPresent())
                .filter(type -> type.qualityIndex().isPresent())
                .map(this::asInfo)
                .collect(Collectors.toList());
    }

    private ReadingQualityInfo asInfo(ReadingQualityType type) {
        ReadingQualityInfo info = new ReadingQualityInfo();
        info.cimCode = type.getCode();
        info.systemName = type.system().map(meteringTranslationService::getDisplayName).orElse("");
        info.categoryName = type.category().map(meteringTranslationService::getDisplayName).orElse("");
        info.indexName = type.qualityIndex().map(meteringTranslationService::getDisplayName).orElse("");
        return info;
    }
}
