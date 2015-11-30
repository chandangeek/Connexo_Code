package com.elster.jupiter.metering.impl;

import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.cbo.ReadingTypeCodeBuilder;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeterConfiguration;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePointConfiguration;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.Pair;
import com.google.common.collect.ImmutableList;

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

    private MeterActivation meterActivation;
    private List<IReadingType> readingTypes = new ArrayList<>();
    private final DataModel dataModel;
    private final Provider<ChannelImpl> channelFactory;

    @Inject
    public ChannelBuilderImpl(DataModel dataModel, Provider<ChannelImpl> channelFactory) {
        this.dataModel = dataModel;
        this.channelFactory = channelFactory;
    }

    @Override
    public ChannelBuilder meterActivation(MeterActivation meterActivation) {
        this.meterActivation = meterActivation;
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
            return channelFactory.get().init(meterActivation, readingTypes, (rt1, rt2) -> DerivationRule.MEASURED);
        }
        return channelFactory.get().init(meterActivation, buildReadingTypes());
    }

    private List<IReadingType> buildReadingTypesOld() {
        if (readingTypes.size() != 1) {
            return readingTypes;
        }
        IReadingType readingType = readingTypes.get(0);
        if (!readingType.isRegular() || !readingType.isCumulative()) {
            return readingTypes;
        }
        // special case of cumulative reading type in load profile, store delta's in first slot
        ReadingTypeCodeBuilder builder = readingType.builder();
        builder.accumulate(Accumulation.DELTADELTA);
        Optional<ReadingTypeImpl> delta = dataModel.mapper(ReadingTypeImpl.class).getOptional(builder.code());
        if (delta.isPresent()) {
            return ImmutableList.of(delta.get(), readingType);
        } else {
            return readingTypes;
        }
    }

    private List<IReadingType> buildReadingTypes() {
        // check multipliers
        // then check if bulk -> delta is possible

        Stream<Pair<ReadingType, ReadingType>> meterMultipliers = meterActivation.getMeter()
                .flatMap(meter -> meter.getConfiguration(meterActivation.getStart()))
                .map(MeterConfiguration::getReadingTypeConfigs)
                .orElseGet(Collections::emptyList)
                .stream()
                .filter(multiplier -> multiplier.getCalculated() != null)
                .filter(multiplier -> readingTypes.contains(multiplier.getMeasured()))
                .map(multiplier -> Pair.of(multiplier.getMeasured(), multiplier.getCalculated()));
        Stream<Pair<ReadingType, ReadingType>> usagePointMultipliers = meterActivation.getUsagePoint()
                .flatMap(usagePoint -> usagePoint.getConfiguration(meterActivation.getStart()))
                .map(UsagePointConfiguration::getReadingTypeConfigs)
                .orElseGet(Collections::emptyList)
                .stream()
                .filter(multiplier -> multiplier.getCalculated() != null)
                .filter(multiplier -> readingTypes.contains(multiplier.getMeasured()))
                .map(multiplier -> Pair.of(multiplier.getMeasured(), multiplier.getCalculated()));
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