package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.IdWithNameInfo;

public class ReadingQualityInfo {
    public String id;
    public String name;
    public IdWithNameInfo application;

    public ReadingQualityInfo(ReadingQualityType type, Thesaurus thesaurus) {
        this.id = type.getCode();
        this.name = thesaurus.getStringBeyondComponent(type.qualityIndex().get().getTranslationKey().getKey(), type.qualityIndex().get().getTranslationKey().getDefaultFormat());
        if (type.system().isPresent()) {
            IdWithNameInfo applicationInfo = new IdWithNameInfo();
            applicationInfo.id = type.system().get().name();
            applicationInfo.name = type.system().get() == QualityCodeSystem.MDC ? "MultiSense" : "Insight";
            this.application = applicationInfo;
        }
    }
}
