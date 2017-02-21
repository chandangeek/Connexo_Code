/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.cbo.ReadingTypeCodeBuilder;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.MeterConfiguration;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePointConfiguration;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.Pair;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.elster.jupiter.util.streams.DecoratedStream.decorate;
import static com.elster.jupiter.util.streams.Predicates.not;
import static java.util.Collections.singletonList;

public class ChannelBuilderImpl implements ChannelBuilder {

    private ChannelsContainer channelsContainer;
    private List<IReadingType> readingTypes = new ArrayList<>();
    private final DataModel dataModel;
    private final Provider<ChannelImpl> channelFactory;

    @Inject
    public ChannelBuilderImpl(DataModel dataModel, Provider<ChannelImpl> channelFactory) {
        this.dataModel = dataModel;
        this.channelFactory = channelFactory;
    }

    @Override
    public ChannelBuilder channelsContainer(ChannelsContainer channelsContainer) {
        this.channelsContainer = channelsContainer;
        return this;
    }

    @Override
    public ChannelBuilder readingTypes(ReadingType main, ReadingType... readingTypes) {
        decorate(Stream.of(Stream.of(main), Arrays.stream(readingTypes)))
                .flatMap(Function.identity())
                .filterSubType(IReadingType.class)
                .forEach(this.readingTypes::add);
        return this;
    }

    @Override
    public ChannelImpl build() {
        if (readingTypes.size() > 1) {
            return channelFactory.get().init(channelsContainer, readingTypes, (rt1, rt2) -> DerivationRule.MEASURED);
        }
        return channelFactory.get().init(channelsContainer, buildReadingTypes());
    }

    private List<IReadingType> buildReadingTypes() {
        // check multipliers
        // then check if bulk -> delta is possible

        Stream<Pair<ReadingType, ReadingType>> meterMultipliers = channelsContainer.getMeter()
                .flatMap(meter -> meter.getConfiguration(channelsContainer.getStart()))
                .map(MeterConfiguration::getReadingTypeConfigs)
                .orElseGet(Collections::emptyList)
                .stream()
                .filter(multiplier -> multiplier.getCalculated().isPresent())
                .filter(multiplier -> readingTypes.contains(multiplier.getMeasured()))
                .map(multiplier -> Pair.of(multiplier.getMeasured(), multiplier.getCalculated().get()));
        Stream<Pair<ReadingType, ReadingType>> usagePointMultipliers = channelsContainer.getUsagePoint()
                .flatMap(usagePoint -> usagePoint.getConfiguration(channelsContainer.getStart()))
                .map(UsagePointConfiguration::getReadingTypeConfigs)
                .orElseGet(Collections::emptyList)
                .stream()
                .filter(multiplier -> multiplier.getCalculated().isPresent())
                .filter(multiplier -> readingTypes.contains(multiplier.getMeasured()))
                .map(multiplier -> Pair.of(multiplier.getMeasured(), multiplier.getCalculated().get()));
        Map<ReadingType, List<Pair<ReadingType, ReadingType>>> multipliers = Stream.of(meterMultipliers, usagePointMultipliers)
                .flatMap(Function.identity())
                .collect(Collectors.groupingBy(Pair::getFirst));

        List<Pair<ReadingType, ReadingType>> toAdd = decorate(readingTypes.stream())
                .map(readingType -> Optional.ofNullable(multipliers.get(readingType)).orElseGet(() -> singletonList(Pair.of(readingType, readingType))))
                .map(List::stream)
                .flatMap(Function.identity())
                .map(this::possiblyDelta)
                .filter(not(readingTypes::contains)) // we don't want the ones we already had to start
                .collect(Collectors.toList());

        return decorate(
                Stream.of(
                        toAdd.stream()
                                .map(pair -> Stream.of(pair.getLast(), pair.getFirst()))
                                .flatMap(Function.identity()),
                        readingTypes.stream()
                ))
                .flatMap(Function.identity())
                .filterSubType(IReadingType.class)
                .distinct()
                .collect(Collectors.toList());
    }

    private Pair<ReadingType, ReadingType> possiblyDelta(Pair<ReadingType, ReadingType> readingType) {
        return Optional.of((IReadingType) readingType.getLast())
                .filter(ReadingType::isRegular)
                .filter(ReadingType::isCumulative)
                .flatMap(this::findDelta)
                .map(delta -> Pair.of(readingType.getFirst(), delta))
                .orElse(readingType);
    }

    private Optional<ReadingType> findDelta(IReadingType readingType) {
        ReadingTypeCodeBuilder builder = readingType.builder();
        builder.accumulate(Accumulation.DELTADELTA);
        return dataModel.mapper(IReadingType.class).getOptional(builder.code()).map(ReadingType.class::cast);
    }
}