/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimators.impl;

import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.validation.ValidationService;

import com.google.common.collect.Range;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static com.elster.jupiter.util.streams.Currying.use;

public class DataCacheService {

    private final ValidationService validationService;

    private Map<Channel, CachedData> dataCache = new HashMap<>();
    private Map<Channel, Optional<Instant>> lastValidationCache = new HashMap<>();

    public DataCacheService(ValidationService validationService) {
        this.validationService = validationService;
    }

    public void fetchData(Channel channel, Range<Instant> period) {
        dataCache.computeIfAbsent(channel, use(this::computeData).with(period));
    }

    private CachedData computeData(Channel channel, Range<Instant> period) {
        List<BaseReadingRecord> readings = channel.getReadings(period);
        List<ReadingQualityRecord> readingQualityRecords = channel.findReadingQualities().inTimeInterval(period).actual().collect();
        Optional<Instant> lastChecked = lastValidationCache.computeIfAbsent(channel, validationService::getLastChecked);
        return new CachedData(period, readings, readingQualityRecords, lastChecked);
    }

    public Optional<Instant> getLastChecked(Channel channel) {
        return lastValidationCache.computeIfAbsent(channel, validationService::getLastChecked);
    }

    public Stream<BaseReadingRecord> getReadings(Channel channel, Range<Instant> period) {
        Stream<BaseReadingRecord> stream;
        CachedData rangeListPair = dataCache.computeIfAbsent(channel, use(this::computeData).with(period));
        if (rangeListPair.interval.encloses(period)) {
            stream = rangeListPair.readingRecords.stream().filter(readingRecord -> period.contains(readingRecord.getTimeStamp()));
        } else {
            Range<Instant> newDataSpan = rangeListPair.interval.span(period);
            rangeListPair = computeData(channel, newDataSpan);
            dataCache.put(channel, rangeListPair);
            stream = rangeListPair.readingRecords.stream();
        }
        return stream;
    }

    public Optional<BaseReadingRecord> getReading(Channel channel, Instant instant) {
        CachedData cachedData = dataCache.get(channel);
        if (cachedData != null) {
            Optional<BaseReadingRecord> first = cachedData.readingRecords.stream().filter(br -> br.getTimeStamp().equals(instant)).findFirst();
            return first.isPresent() ? first : channel.getReading(instant);
        } else {
            return channel.getReading(instant);
        }
    }

    public Stream<ReadingQualityRecord> getReadingQualities(Channel channel) {
        if (dataCache.containsKey(channel)) {
            return dataCache.get(channel).readingQualityRecords.stream();
        } else {
            return Stream.empty();
        }
    }

    private static class CachedData {
        private Range<Instant> interval;
        private List<BaseReadingRecord> readingRecords;
        private List<ReadingQualityRecord> readingQualityRecords;
        private Optional<Instant> lastChecked;

        public CachedData(Range<Instant> interval, List<BaseReadingRecord> readingRecords, List<ReadingQualityRecord> readingQualityRecords, Optional<Instant> lastChecked) {
            this.interval = interval;
            this.readingRecords = readingRecords;
            this.readingQualityRecords = readingQualityRecords;
            this.lastChecked = lastChecked;
        }
    }
}
