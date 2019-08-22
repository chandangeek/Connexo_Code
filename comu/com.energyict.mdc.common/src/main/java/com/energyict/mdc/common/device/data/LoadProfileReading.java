/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.device.data;

import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.validation.DataValidationStatus;

import aQute.bnd.annotation.ConsumerType;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Created by bvn on 8/1/14.
 */
@ConsumerType
public interface LoadProfileReading {

    Range<Instant> getRange();

    Map<Channel, IntervalReadingRecord> getChannelValues();

    Map<Channel, DataValidationStatus> getChannelValidationStates();

    Instant getReadingTime();

    void setReadingQualities(Channel channel, List<? extends ReadingQualityRecord> readingQualities);

    Map<Channel, List<? extends ReadingQualityRecord>> getReadingQualities();

}