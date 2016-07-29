package com.energyict.mdc.usagepoint.data.rest.impl;

import com.elster.jupiter.validation.ValidationAction;
import com.energyict.mdc.common.rest.IntervalInfo;

import com.google.common.collect.Range;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

public class ChannelDataInfoFactory {

    public ChannelDataInfo asInfo(IntervalReadingWithValidationStatus readingRecord, Map<Range<Instant>, Instant> lastCheckedMap) {
        ChannelDataInfo channelDataInfo = new ChannelDataInfo();
        channelDataInfo.interval = IntervalInfo.from(readingRecord.getTimePeriod());
        channelDataInfo.value = readingRecord.getValue();
        Optional<Instant> lastChecked = lastCheckedMap.entrySet().stream()
                .filter(entry -> entry.getKey().contains(readingRecord.getTimeStamp()))
                .findAny()
                .map(Map.Entry::getValue);
        channelDataInfo.readingTime = readingRecord.getTimeStamp();
        channelDataInfo.validationResult = readingRecord.getValidationStatus(lastChecked.orElse(Instant.MIN));
        channelDataInfo.dataValidated = !readingRecord.getTimeStamp().isAfter(lastChecked.orElse(Instant.MIN));
        channelDataInfo.validationAction = ValidationAction.FAIL;
        return channelDataInfo;
    }
}
