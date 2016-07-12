package com.energyict.mdc.usagepoint.data.rest.impl;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.energyict.mdc.common.rest.IntervalInfo;

import javax.inject.Inject;
import java.util.stream.Collectors;

public class ChannelDataInfoFactory {

    private final Thesaurus thesaurus;

    @Inject
    public ChannelDataInfoFactory(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    public ChannelDataInfo createChannelDataInfo(IntervalReadingRecord readingRecord) {
        ChannelDataInfo channelIntervalInfo = new ChannelDataInfo();
        channelIntervalInfo.interval = IntervalInfo.from(readingRecord.getTimePeriod().get());
        channelIntervalInfo.readingTime = readingRecord.getTimeStamp();
        channelIntervalInfo.value = readingRecord.getValue();
        channelIntervalInfo.readingQualities = readingRecord.getReadingQualities()
                .stream()
                .filter(ReadingQualityRecord::isActual)
                .distinct()
                .filter(record -> record.getType().system().isPresent())
                .filter(record -> record.getType().category().isPresent())
                .filter(record -> record.getType().qualityIndex().isPresent())
                .filter(record -> (record.getType().getSystemCode() == QualityCodeSystem.ENDDEVICE.ordinal()))
                .map(rq -> getSimpleName(rq.getType()))
                .collect(Collectors.toList());
        return channelIntervalInfo;
    }

    private String getSimpleName(ReadingQualityType type) {
        TranslationKey translationKey = type.qualityIndex().get().getTranslationKey();
        return thesaurus.getStringBeyondComponent(translationKey.getKey(), translationKey.getDefaultFormat());
    }
}