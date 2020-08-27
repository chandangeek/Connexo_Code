/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.cbo.QualityCodeCategory;
import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.CimChannel;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingQualityFetcher;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.ReadingQualityWithTypeFetcher;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.QueryStream;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;

import com.google.common.collect.Range;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.conditions.Where.where;

class ReadingQualityFetcherImpl implements ReadingQualityFetcher {
    private QueryStream<ReadingQualityRecord> stream;
    private boolean channelConditionAdded;
    private Meter meter;

    ReadingQualityFetcherImpl(DataModel dataModel, Meter meter) {
        stream = dataModel.stream(ReadingQualityRecord.class);
        this.meter = meter;
    }

    ReadingQualityFetcherImpl(DataModel dataModel, Channel channel) {
        stream = dataModel.stream(ReadingQualityRecord.class)
                .filter(where("channel").isEqualTo(channel));
        channelConditionAdded = true;
    }

    ReadingQualityFetcherImpl(DataModel dataModel, CimChannel cimChannel) {
        this(dataModel, cimChannel.getChannel());
        forReadingType(cimChannel.getReadingType());
    }

    @Override
    public ReadingQualityFetcher inChannels(Set<Channel> channels) {
        stream = stream.filter(where("channel").in(new ArrayList<>(channels)));
        channelConditionAdded = true;
        return this;
    }

    @Override
    public ReadingQualityFetcher forReadingTypes(Set<ReadingType> readingTypes) {
        List<Long> ids = readingTypes.stream()
                .map(IReadingType.class::cast)
                .map(IReadingType::getId)
                .collect(Collectors.toList());
        stream = stream.filter(where("readingTypeId").in(ids));
        return this;
    }

    @Override
    public ReadingQualityFetcher forReadingType(ReadingType readingType) {
        stream = stream.filter(where("readingTypeId").isEqualTo(((IReadingType) readingType).getId()));
        return this;
    }

    @Override
    public ReadingQualityFetcher inScope(Map<Channel, Range<Instant>> scope) {
        return inReverseScope(scope.entrySet().stream()
                .collect(Collectors.groupingBy(Map.Entry::getValue,
                        Collectors.mapping(Map.Entry::getKey,
                                Collectors.toSet()))));
    }

    private ReadingQualityFetcher inReverseScope(Map<Range<Instant>, Set<Channel>> reverseScope) {
        reverseScope.entrySet().stream()
                .map(rangeAndChannelSet -> where("readingTimestamp").in(rangeAndChannelSet.getKey())
                        .and(where("channel").in(new ArrayList<>(rangeAndChannelSet.getValue()))))
                .reduce(Condition::or)
                .ifPresent(condition -> stream = stream.filter(condition));
        channelConditionAdded = true;
        return this;
    }

    @Override
    public ReadingQualityFetcher inTimeInterval(Range<Instant> interval) {
        if (!Range.<Instant>all().equals(interval)) {
            stream = stream.filter(where("readingTimestamp").in(interval));
        }
        return this;
    }

    @Override
    public ReadingQualityFetcher atTimestamp(Instant timestamp) {
        stream = stream.filter(where("readingTimestamp").isEqualTo(timestamp));
        return this;
    }

    @Override
    public ReadingQualityWithTypeFetcher ofQualityType(ReadingQualityType readingQualityType) {
        return new ReadingQualityWithTypeFetcherImpl(this).ofQualityType(readingQualityType);
    }

    @Override
    public ReadingQualityWithTypeFetcher ofQualityTypes(Set<ReadingQualityType> readingQualityTypes) {
        return new ReadingQualityWithTypeFetcherImpl(this).ofQualityTypes(readingQualityTypes);
    }

    @Override
    public ReadingQualityWithTypeFetcher ofQualitySystems(Set<QualityCodeSystem> systems) {
        return new ReadingQualityWithTypeFetcherImpl(this).ofQualitySystems(systems);
    }

    @Override
    public ReadingQualityWithTypeFetcher ofQualityIndices(Set<QualityCodeIndex> indices) {
        return new ReadingQualityWithTypeFetcherImpl(this).ofQualityIndices(indices);
    }

    @Override
    public ReadingQualityWithTypeFetcher ofQualityIndices(QualityCodeCategory category, Set<Integer> indices) {
        return new ReadingQualityWithTypeFetcherImpl(this).ofQualityIndices(category, indices);
    }

    @Override
    public ReadingQualityWithTypeFetcher ofAnyQualityIndexInCategories(Set<QualityCodeCategory> categories) {
        return new ReadingQualityWithTypeFetcherImpl(this).ofAnyQualityIndexInCategories(categories);
    }

    @Override
    public ReadingQualityWithTypeFetcher ofQualitySystem(QualityCodeSystem system) {
        return new ReadingQualityWithTypeFetcherImpl(this).ofQualitySystem(system);
    }

    @Override
    public ReadingQualityWithTypeFetcher ofQualityIndex(QualityCodeIndex index) {
        return new ReadingQualityWithTypeFetcherImpl(this).ofQualityIndex(index);
    }

    @Override
    public ReadingQualityWithTypeFetcher ofQualityIndex(QualityCodeCategory category, int index) {
        return new ReadingQualityWithTypeFetcherImpl(this).ofQualityIndex(category, index);
    }

    @Override
    public ReadingQualityWithTypeFetcher ofAnyQualityIndexInCategory(QualityCodeCategory category) {
        return new ReadingQualityWithTypeFetcherImpl(this).ofAnyQualityIndexInCategory(category);
    }

    @Override
    public ReadingQualityFetcher actual() {
        stream = stream.filter(where("actual").isEqualTo(true));
        return this;
    }

    @Override
    public ReadingQualityFetcher sorted() {
        stream = stream.sorted(Order.ascending("readingTimestamp"));
        return this;
    }

    @Override
    public List<ReadingQualityRecord> collect() {
        addChannelConditionIfNotYet();
        return stream.collect(Collectors.toList());
    }

    public Optional<ReadingQualityRecord> findFirst() {
        addChannelConditionIfNotYet();
        return stream.findFirst();
    }

    private void addChannelConditionIfNotYet() {
        if (!channelConditionAdded) {
            stream = stream.join(Channel.class)
                    .join(ChannelsContainer.class)
                    .join(MeterActivation.class)
                    .filter(where("channel.channelsContainer.meterActivation.meter").isEqualTo(meter));
        }
    }

    ReadingQualityFetcher ofTypeCode(String regexp) {
        stream = stream.filter(where("typeCode").matches(regexp, ""));
        return this;
    }
}
