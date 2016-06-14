package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.util.Pair;

public class ReadingQualityInfo {
    public String id;
    public String name;
    public IdWithNameInfo application;

    public ReadingQualityInfo(ReadingQualityType type, Thesaurus thesaurus) {
        Pair<String, QualityCodeIndex> pair = Pair.of(type.getCode(), type.qualityIndex().get());
        this.id = pair.getFirst();
        this.name = thesaurus.getStringBeyondComponent(pair.getLast().getTranslationKey().getKey(), pair.getLast().getTranslationKey().getDefaultFormat());
        IdWithNameInfo applicationInfo = new IdWithNameInfo();
        applicationInfo.id = type.system().get().name();
        applicationInfo.name = applicationInfo.id.equals("MDC") ? "MultiSense" : "Insight";
        this.application = applicationInfo;
    }
}
