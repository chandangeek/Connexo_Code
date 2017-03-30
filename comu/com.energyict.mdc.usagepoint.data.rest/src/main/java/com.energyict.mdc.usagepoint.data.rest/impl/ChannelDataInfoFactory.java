/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.usagepoint.data.rest.impl;

import com.elster.jupiter.validation.ValidationAction;
import com.energyict.mdc.common.rest.IntervalInfo;

import com.google.common.collect.RangeMap;

import java.time.Instant;
import java.util.Optional;

public class ChannelDataInfoFactory {

    public ChannelDataInfo asInfo(IntervalReadingWithValidationStatus readingRecord, RangeMap<Instant, Instant> lastCheckedMap) {
        ChannelDataInfo channelDataInfo = new ChannelDataInfo();
        channelDataInfo.interval = IntervalInfo.from(readingRecord.getTimePeriod());
        channelDataInfo.value = readingRecord.getValue();
        Optional<Instant> lastChecked = Optional.ofNullable(lastCheckedMap.get(readingRecord.getTimeStamp()));
        // if not last checked found in the map this means that we had no meter attached at that time
        // the front-end will render gray bars for such intervals (agreement is if no readingTime field in the payload - display gray bar)
        lastChecked.ifPresent(instant -> channelDataInfo.readingTime = readingRecord.getTimeStamp());
        channelDataInfo.validationResult = readingRecord.getValidationStatus(lastChecked.orElse(Instant.MIN));
        channelDataInfo.dataValidated = !readingRecord.getTimeStamp().isAfter(lastChecked.orElse(Instant.MIN));
        channelDataInfo.validationAction = ValidationAction.FAIL;
        return channelDataInfo;
    }
}
