package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.metering.readings.Reading;

import com.google.common.collect.Range;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Optional;

public class RegisterReadingWithValidationStatus extends ReadingWithValidationStatus<ReadingRecord> {

    public RegisterReadingWithValidationStatus(ZonedDateTime readingTimeStamp, ChannelGeneralValidation channelGeneralValidation) {
        super(readingTimeStamp, channelGeneralValidation);
    }

    public Optional<String> getText() {
        return getPersistedReadingRecord().map(Reading::getText);
    }

    public Optional<Range<Instant>> getBillingPeriod() {
        return getPersistedReadingRecord().flatMap(BaseReading::getTimePeriod);
    }
}
