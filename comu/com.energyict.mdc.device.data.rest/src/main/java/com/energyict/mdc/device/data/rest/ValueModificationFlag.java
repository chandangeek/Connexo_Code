package com.energyict.mdc.device.data.rest;


import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.metering.readings.ReadingQuality;
import com.energyict.mdc.device.data.rest.impl.ReadingModificationFlag;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public enum ValueModificationFlag {
    EDITED,
    ESTIMATED
    ;

    public static ValueModificationFlag getModificationFlag(Collection<? extends ReadingQuality> readingQualities, ReadingModificationFlag readingModificationFlag) {
        List<QualityCodeIndex> qualityCodeIndex = readingQualities.stream().filter(Objects::nonNull).map(quality -> quality.getType().qualityIndex())
                .filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());
        return getValueModificationFlag(qualityCodeIndex, readingModificationFlag);
    }

    private static ValueModificationFlag getValueModificationFlag(List<QualityCodeIndex>  qualityCodeIndex, ReadingModificationFlag readingModificationFlag) {
        if(qualityCodeIndex.contains(QualityCodeIndex.EDITGENERIC) && ReadingModificationFlag.EDITED.equals(readingModificationFlag)) {
            return ValueModificationFlag.EDITED;
        }
        return null;
    }
}
