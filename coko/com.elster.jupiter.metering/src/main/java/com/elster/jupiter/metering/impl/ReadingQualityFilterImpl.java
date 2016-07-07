package com.elster.jupiter.metering.impl;

import com.elster.jupiter.cbo.QualityCodeCategory;
import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.CimChannel;
import com.elster.jupiter.metering.ReadingQualityFilter;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingQualityWithTypeFilter;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.QueryStream;
import com.elster.jupiter.util.conditions.Order;

import com.google.common.collect.Range;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.conditions.Where.where;

class ReadingQualityFilterImpl implements ReadingQualityFilter {
    private QueryStream<ReadingQualityRecord> stream;

    ReadingQualityFilterImpl(DataModel dataModel, Channel channel) {
        stream = dataModel.stream(ReadingQualityRecord.class)
                .filter(where("channel").isEqualTo(channel));
    }

    ReadingQualityFilterImpl(DataModel dataModel, CimChannel cimChannel) {
        this(dataModel, cimChannel.getChannel());
        stream = stream.filter(where("readingType").isEqualTo(cimChannel.getReadingType()));
    }

    @Override
    public ReadingQualityFilter inTimeInterval(Range<Instant> interval) {
        if (!Range.<Instant>all().equals(interval)) {
            stream = stream.filter(where("readingTimestamp").in(interval));
        }
        return this;
    }

    @Override
    public ReadingQualityFilter atTimestamp(Instant timestamp) {
        stream = stream.filter(where("readingTimestamp").isEqualTo(timestamp));
        return this;
    }

    @Override
    public ReadingQualityWithTypeFilter ofQualitySystems(Set<QualityCodeSystem> systems) {
        return new ReadingQualityWithTypeFilterImpl(this).ofQualitySystems(systems);
    }

    @Override
    public ReadingQualityWithTypeFilter ofQualityIndices(Set<QualityCodeIndex> indices) {
        return new ReadingQualityWithTypeFilterImpl(this).ofQualityIndices(indices);
    }

    @Override
    public ReadingQualityWithTypeFilter ofQualityIndices(QualityCodeCategory category, Set<Integer> indices) {
        return new ReadingQualityWithTypeFilterImpl(this).ofQualityIndices(category, indices);
    }

    @Override
    public ReadingQualityWithTypeFilter ofAnyQualityIndexInCategories(Set<QualityCodeCategory> categories) {
        return new ReadingQualityWithTypeFilterImpl(this).ofAnyQualityIndexInCategories(categories);
    }

    @Override
    public ReadingQualityWithTypeFilter ofQualitySystem(QualityCodeSystem system) {
        return new ReadingQualityWithTypeFilterImpl(this).ofQualitySystem(system);
    }

    @Override
    public ReadingQualityWithTypeFilter ofQualityIndex(QualityCodeIndex index) {
        return new ReadingQualityWithTypeFilterImpl(this).ofQualityIndex(index);
    }

    @Override
    public ReadingQualityWithTypeFilter ofQualityIndex(QualityCodeCategory category, int index) {
        return new ReadingQualityWithTypeFilterImpl(this).ofQualityIndex(category, index);
    }

    @Override
    public ReadingQualityWithTypeFilter ofAnyQualityIndexInCategory(QualityCodeCategory category) {
        return new ReadingQualityWithTypeFilterImpl(this).ofAnyQualityIndexInCategory(category);
    }

    @Override
    public ReadingQualityFilter actual() {
        stream = stream.filter(where("actual").isEqualTo(true));
        return this;
    }

    @Override
    public ReadingQualityFilter sorted() {
        stream = stream.sorted(Order.ascending("readingTimestamp"));
        return this;
    }

    @Override
    public List<ReadingQualityRecord> collect() {
        return stream.collect(Collectors.toList());
    }

    public Optional<ReadingQualityRecord> findFirst() {
        return stream.findFirst();
    }

    ReadingQualityFilter ofTypeCode(String regexp) {
        stream = stream.filter(where("typeCode").matches(regexp, ""));
        return this;
    }
}
