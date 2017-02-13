/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data;

import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.metering.ReadingQualityType;

import java.util.function.Predicate;

public enum ChannelDataModificationSummaryFlags implements IChannelDataCompletionSummaryFlag {
    ADDED("statisticsAdded", "Added", type -> type.qualityIndex().orElse(null) == QualityCodeIndex.ADDED),
    EDITED("edited", "Edited", type -> type.qualityIndex().orElse(null) == QualityCodeIndex.EDITGENERIC),
    REMOVED("statisticsRemoved", "Removed", type -> type.qualityIndex().orElse(null) == QualityCodeIndex.REJECTED);

    private String key, translation;
    private Predicate<ReadingQualityType> qualityTypePredicate;

    ChannelDataModificationSummaryFlags(String key, String translation, Predicate<ReadingQualityType> qualityTypePredicate) {
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
