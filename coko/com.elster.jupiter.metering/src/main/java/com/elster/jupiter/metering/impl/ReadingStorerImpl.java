/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.ids.FieldSpec;
import com.elster.jupiter.ids.IdsService;
import com.elster.jupiter.ids.TimeSeriesDataStorer;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.CimChannel;
import com.elster.jupiter.metering.EventType;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterConfiguration;
import com.elster.jupiter.metering.MeterReadingTypeConfiguration;
import com.elster.jupiter.metering.MultiplierType;
import com.elster.jupiter.metering.MultiplierUsage;
import com.elster.jupiter.metering.ProcessStatus;
import com.elster.jupiter.metering.ReadingStorer;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.StorerProcess;
import com.elster.jupiter.metering.UsagePointConfiguration;
import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.metering.readings.IntervalReading;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.collections.ObserverContainer;
import com.elster.jupiter.util.collections.Subscription;
import com.elster.jupiter.util.collections.ThreadSafeObserverContainer;

import com.google.common.collect.Maps;
import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.elster.jupiter.util.Checks.is;
import static com.elster.jupiter.util.streams.Currying.perform;
import static com.elster.jupiter.util.streams.Currying.use;
import static com.elster.jupiter.util.streams.DecoratedStream.decorate;
import static com.elster.jupiter.util.streams.Predicates.not;
import static com.elster.jupiter.util.streams.Predicates.on;

class ReadingStorerImpl implements ReadingStorer {
    private static final ProcessStatus DEFAULTPROCESSSTATUS = ProcessStatus.of();
    private final TimeSeriesDataStorer storer;

    private final Map<CimChannel, Range<Instant>> scope = new HashMap<>();
    private final EventService eventService;
    private final Map<Pair<ChannelContract, Instant>, Object[]> consolidatedValues = new HashMap<>();
    private final Map<Pair<ChannelContract, Instant>, BaseReading> readings = new HashMap<>();
    private final StorerProcess storerProcess;
    private final IdsService idsService;
    private final Behaviours updateBehaviour;
    private Map<Pair<ChannelContract, Instant>, Object[]> previousReadings;
    private Map<ChannelContract, List<Derivation>> deltaDerivations;
    private Map<ChannelContract, List<Derivation>> multipliedDerivations;

    private ObserverContainer<OverflowListener> overflowListeners = new ThreadSafeObserverContainer<>();
    private ObserverContainer<BackflowListener> backflowListeners = new ThreadSafeObserverContainer<>();

    private static class Derivation {
        private final ChannelContract channel;
        private final IReadingType readingType;
        private final DerivationRule derivationRule;
        private final int index;

        public Derivation(ChannelContract channel, IReadingType readingType, DerivationRule derivationRule, int index) {
            this.channel = channel;
            this.readingType = readingType;
            this.derivationRule = derivationRule;
            this.index = index;
        }

        public IReadingType getReadingType() {
            return readingType;
        }

        public DerivationRule getDerivationRule() {
            return derivationRule;
        }

        public int getIndex() {
            return index;
        }

        public ChannelContract getChannel() {
            return channel;
        }

    }

    private enum FlowDetection {
        NORMAL, OVERFLOW, BACKFLOW
    }

    private ReadingStorerImpl(IdsService idsService, EventService eventService, Behaviours updateBehaviour, StorerProcess storerProcess) {
        this.idsService = idsService;
        this.eventService = eventService;
        this.storer = updateBehaviour.createTimeSeriesStorer(idsService);
        this.storerProcess = storerProcess;
        this.updateBehaviour = updateBehaviour;
    }

    static ReadingStorerImpl createNonOverrulingStorer(IdsService idsService, EventService eventService) {
        return new ReadingStorerImpl(idsService, eventService, Behaviours.INSERT_ONLY, StorerProcess.DEFAULT);
    }

    static ReadingStorerImpl createOverrulingStorer(IdsService idsService, EventService eventService) {
        return new ReadingStorerImpl(idsService, eventService, Behaviours.OVERRULE, StorerProcess.DEFAULT);
    }

    static ReadingStorerImpl createUpdatingStorer(IdsService idsService, EventService eventService) {
        return new ReadingStorerImpl(idsService, eventService, Behaviours.UPDATE, StorerProcess.DEFAULT);
    }

    static ReadingStorerImpl createUpdatingStorer(IdsService idsService, EventService eventService, StorerProcess storerProcess) {
        return new ReadingStorerImpl(idsService, eventService, Behaviours.UPDATE, storerProcess);
    }

    private interface UpdateBehaviour {
        TimeSeriesDataStorer createTimeSeriesStorer(IdsService idsService);
    }

    private enum Behaviours implements UpdateBehaviour {
        INSERT_ONLY {
            @Override
            public TimeSeriesDataStorer createTimeSeriesStorer(IdsService idsService) {
                return idsService.createNonOverrulingStorer();
            }
        },

        OVERRULE {
            @Override
            public TimeSeriesDataStorer createTimeSeriesStorer(IdsService idsService) {
                return idsService.createOverrulingStorer();
            }
        },

        UPDATE {
            @Override
            public TimeSeriesDataStorer createTimeSeriesStorer(IdsService idsService) {
                return idsService.createUpdatingStorer();
            }

            @Override
            Set<Pair<ChannelContract, Instant>> determineNeed(Map<Pair<ChannelContract, Instant>, Object[]> valuesView) {
                HashSet<Pair<ChannelContract, Instant>> needed = new HashSet<>(valuesView.keySet());
                needed.addAll(super.determineNeed(valuesView));
                return needed;
            }
        };

        Set<Pair<ChannelContract, Instant>> determineNeed(Map<Pair<ChannelContract, Instant>, Object[]> valuesView) {
            return valuesView.keySet()
                    .stream()
                    .map(pair -> Pair.of(pair.getFirst(), pair.getFirst().getPreviousDateTime(pair.getLast())))
                    .collect(Collectors.toSet());
        }

    }

    @Override
    public void addReading(CimChannel channel, BaseReading reading) {
        addReading(channel, reading, DEFAULTPROCESSSTATUS);
    }

    @Override
    public void addReading(CimChannel channel, BaseReading reading, ProcessStatus status) {
        ChannelContract channelContract = (ChannelContract) channel.getChannel();
        Pair<ChannelContract, Instant> key = Pair.of(channelContract, reading.getTimeStamp());
        int offset = channel.isRegular() ? 2 : 1;

        List<? extends FieldSpec> fieldSpecs = channelContract.getTimeSeries().getRecordSpec().getFieldSpecs();

        Object[] values = consolidatedValues.computeIfAbsent(key, k -> {
            Object[] newValues = new Object[fieldSpecs.size()];
            IntStream.range(0, newValues.length).forEach(i -> newValues[i] = storer.doNotUpdateMarker());
            return newValues;
        });
        Object[] valuesToAdd = channelContract.toArray(reading, channel.getReadingType(), status);

        IntStream.range(offset, values.length)
                .filter(i -> valuesToAdd[i] != null)
                .forEach(i -> values[i] = valuesToAdd[i]);

        ProcessStatus processStatus;
        if (values[0] != null && !Objects.equals(storer.doNotUpdateMarker(), values[0])) {
            processStatus = status.or(new ProcessStatus((Long) values[0]));
        } else {
            processStatus = status;
        }
        values[0] = processStatus.getBits();
        if (reading instanceof IntervalReading) {
            values[1] = 0L; //The 'profile status' is no longer used. Its usage has been replaced by reading qualities.
        }
        addScope(channel, reading.getTimeStamp());
        readings.put(key, reading);
    }

    @Override
    public void execute(QualityCodeSystem system) {
        doDeltas();
        doMultiplications();
        consolidatedValues.entrySet()
                .forEach(entry -> {
                    ChannelContract channel = entry.getKey().getFirst();
                    Instant timestamp = entry.getKey().getLast();
                    Object[] values = entry.getValue();
                    channel.validateValues(readings.get(entry.getKey()), values);
                    overflowBackflowDetection(system, channel, timestamp, values);
                    storer.add(channel.getTimeSeries(), timestamp, values);
                });
        storer.execute();
        eventService.postEvent(EventType.READINGS_CREATED.topic(), this);
    }

    private void overflowBackflowDetection(QualityCodeSystem system, ChannelContract channel, Instant timestamp, Object[] values) {
        // for each readingType, that has an overflow value configured at the time of the reading, check overflow
        HashSet<ReadingType> readingTypes = new HashSet<>(channel.getReadingTypes());
        List<MeterReadingTypeConfiguration> meterReadingTypeConfigurations = getMeterReadingTypeConfigurations(channel, timestamp);
        meterReadingTypeConfigurations
                .stream()
                .filter(meterReadingTypeConfiguration -> readingTypes.contains(meterReadingTypeConfiguration.getMeasured()))
                .filter(meterReadingTypeConfiguration -> meterReadingTypeConfiguration.getOverflowValue().isPresent())
                .forEach(meterReadingTypeConfiguration -> checkOverflowOrBackflow(system, meterReadingTypeConfiguration, channel, timestamp, values));
    }

    private void checkOverflowOrBackflow(QualityCodeSystem system, MeterReadingTypeConfiguration meterReadingTypeConfiguration,
                                         ChannelContract channel, Instant timestamp, Object[] values) {
        int slotOffset = channel.getRecordSpecDefinition().slotOffset();
        ReadingType readingType = meterReadingTypeConfiguration.getMeasured();
        CimChannel cimChannel = channel.getCimChannel(readingType).get();
        int valueIndex = slotOffset + channel.getReadingTypes().indexOf(readingType);
        if ((values[valueIndex] instanceof BigDecimal)) {
            BigDecimal value = (BigDecimal) values[valueIndex];
            BigDecimal overflowValue = meterReadingTypeConfiguration.getOverflowValue().get();
            Object[] previousValues = getPreviousValues(channel, timestamp);
            BigDecimal previousValue = previousValues != null && previousValues[valueIndex] instanceof BigDecimal ? (BigDecimal) previousValues[valueIndex] : null;
            switch (flowDetection(cimChannel, overflowValue, value, previousValue)) {
                case BACKFLOW:
                    backflowListeners.notify(listener -> listener.backflowOccurred(system, cimChannel, timestamp, value, overflowValue));
                    break;
                case OVERFLOW:
                    overflowListeners.notify(listener -> listener.overflowOccurred(system, cimChannel, timestamp, value, overflowValue));
                    break;
                default:
            }
        }
    }

    private static FlowDetection flowDetection(CimChannel cimChannel, BigDecimal overflowValue, BigDecimal value, BigDecimal previousValue) {
        if (value.compareTo(overflowValue) > 0) {
            return FlowDetection.OVERFLOW;
        }
        if (previousValue != null && cimChannel.getReadingType().isCumulative()) {
            BigDecimal diff = previousValue.subtract(value).abs();
            BigDecimal halfOfRange = overflowValue.divide(BigDecimal.valueOf(2), RoundingMode.HALF_UP);
            if (is(value).smallerThan(previousValue)) {
                if (diff.compareTo(halfOfRange) < 0) {
                    return FlowDetection.BACKFLOW;
                } else {
                    return FlowDetection.OVERFLOW;
                }
            } else if (is(diff).greaterThan(halfOfRange)) {
                return FlowDetection.BACKFLOW;
            }
        }
        return FlowDetection.NORMAL;
    }

    private Object[] getPreviousValues(ChannelContract channel, Instant timestamp) {
        if (channel.isRegular()) {
            Instant previousDateTime = channel.getPreviousDateTime(timestamp);
            return previousReadings.get(Pair.of(channel, previousDateTime));
        }
        return null;
    }

    private List<MeterReadingTypeConfiguration> getMeterReadingTypeConfigurations(ChannelContract channel, Instant timestamp) {
        return channel.getChannelsContainer()
                .getMeter()
                .flatMap(use(Meter::getConfiguration).with(timestamp))
                .map(MeterConfiguration::getReadingTypeConfigs)
                .orElseGet(Collections::emptyList);
    }

    private void doDeltas() {
        Map<Pair<ChannelContract, Instant>, Object[]> valuesView = Maps.filterKeys(consolidatedValues, pair -> pair.getFirst().isRegular());
        deltaDerivations = valuesView.keySet()
                .stream()
                .map(Pair::getFirst)
                .distinct()
                .collect(Collectors.toMap(
                        Function.identity(),
                        this::getDerivations
                ));

        previousReadings = new HashMap<>();
        Set<Pair<ChannelContract, Instant>> needed = determineNeed(valuesView);
        previousReadings.putAll(Maps.filterKeys(valuesView, needed::contains));
        previousReadings.putAll(readFromDb(needed));

        // ok we've got all the previous readings that are available

        valuesView.keySet()
                .stream()
                .map(Pair::getFirst)
                .distinct()
                .forEach(this::calculateDelta);

        updateExistingRecords(valuesView);
    }

    private Set<Pair<ChannelContract, Instant>> determineNeed(Map<Pair<ChannelContract, Instant>, Object[]> valuesView) {
        return updateBehaviour.determineNeed(valuesView);
    }

    private List<Derivation> getDerivations(ChannelContract channel) {
        return decorate(channel.getReadingTypes().stream())
                .zipWithIndex()
                .map(pair -> new Derivation(channel, pair.getFirst(), channel.getDerivationRule(pair.getFirst()), pair.getLast().intValue()))
                .filter(derivation -> derivation.getDerivationRule().isDelta())
                .collect(Collectors.toList());
    }

    private void updateExistingRecords(Map<Pair<ChannelContract, Instant>, Object[]> valuesView) {
        Set<Pair<ChannelContract, Instant>> mayNeedToUpdateTimes = valuesView.keySet()
                .stream()
                .map(pair -> Pair.of(pair.getFirst(), pair.getFirst().getNextDateTime(pair.getLast())))
                .filter(not(consolidatedValues::containsKey))
                .collect(Collectors.toSet());
        Map<Pair<ChannelContract, Instant>, Object[]> mayNeedToUpdate = readFromDb(mayNeedToUpdateTimes);
        TimeSeriesDataStorer updatingStorer = Behaviours.UPDATE.createTimeSeriesStorer(idsService);
        mayNeedToUpdate.entrySet()
                .stream()
                .filter(this::updateEntryIfNeeded)
                .forEach(entry -> updatingStorer.add(entry.getKey().getFirst().getTimeSeries(), entry.getKey().getLast(), entry.getValue()));
        updatingStorer.execute();
    }

    private boolean updateEntryIfNeeded(Map.Entry<Pair<ChannelContract, Instant>, Object[]> entry) {
        ChannelContract channel = entry.getKey().getFirst();
        Instant instant = entry.getKey().getLast();
        Object[] toUpdate = entry.getValue();
        Instant previousInstant = channel.getPreviousDateTime(instant);
        Object[] previous = consolidatedValues.get(Pair.of(channel, previousInstant));
        return previous != null && doDeltaUpdates(channel, instant, toUpdate, previous);
    }

    private boolean doDeltaUpdates(ChannelContract channel, Instant instant, Object[] toUpdate, Object[] previous) {
        return deltaDerivations.get(channel)
                .stream()
                .map(derivation -> doDeltaUpdate(channel, instant, toUpdate, previous, derivation))
                .reduce(false, (a, b) -> a | b);
    }

    private boolean doDeltaUpdate(ChannelContract channel, Instant instant, Object[] toUpdate, Object[] previous, Derivation derivation) {
        int index = derivation.getIndex() + channel.getRecordSpecDefinition().slotOffset();
        BigDecimal previousBulk = getBigDecimal(previous[index + 1]);
        BigDecimal currentBulk = getBigDecimal(toUpdate[index + 1]);
        IReadingType bulkReadingType = (IReadingType) derivation.getReadingType().getBulkReadingType().get();

        Function<BigDecimal, BigDecimal> overflowCorrection = getOverflowCorrection(channel, bulkReadingType, instant, previousBulk, currentBulk);

        if (currentBulk != null && previousBulk != null) {
            BigDecimal delta = currentBulk.subtract(previousBulk);
            delta = overflowCorrection.apply(delta);
            if (derivation.getDerivationRule().isMultiplied()) {
                BigDecimal multiplier = getMultiplier(channel, instant, bulkReadingType);
                delta = delta.multiply(multiplier);
            }
            toUpdate[index] = delta;
            return true;
        }
        return false;
    }

    private Function<BigDecimal, BigDecimal> getOverflowCorrection(ChannelContract channel, IReadingType bulkReadingType, Instant instant, BigDecimal previousBulk, BigDecimal currentBulk) {
        if (currentBulk == null || previousBulk == null) {
            return Function.identity();
        }
        Optional<MeterReadingTypeConfiguration> meterReadingTypeConfiguration = channel.getChannelsContainer()
                .getMeter()
                .flatMap(meter -> meter.getConfiguration(instant))
                .flatMap(use(MeterConfiguration::getReadingTypeConfiguration).with(bulkReadingType));
        Optional<BigDecimal> overflowValue = meterReadingTypeConfiguration
                .flatMap(MeterReadingTypeConfiguration::getOverflowValue);

        return overflowValue.map(value -> {
            FlowDetection flowDetection = flowDetection(channel.getCimChannel(bulkReadingType).get(), overflowValue.get(), currentBulk, previousBulk);
            if (is(currentBulk).smallerThan(previousBulk) && FlowDetection.OVERFLOW.equals(flowDetection)) {
                int fractionDigits = meterReadingTypeConfiguration.get().getNumberOfFractionDigits().orElse(0);
                BigDecimal rollOver = BigDecimal.valueOf(1, fractionDigits).add(overflowValue.get());
                return (Function<BigDecimal, BigDecimal>) bd -> bd.add(rollOver);
            }
            if (is(currentBulk).greaterThan(previousBulk) && FlowDetection.BACKFLOW.equals(flowDetection)) {
                int fractionDigits = meterReadingTypeConfiguration.get().getNumberOfFractionDigits().orElse(0);
                BigDecimal rollOver = BigDecimal.valueOf(1, fractionDigits).add(overflowValue.get());
                return (Function<BigDecimal, BigDecimal>) bd -> bd.subtract(rollOver);
            }
            return Function.<BigDecimal>identity();
        }).orElse(Function.identity());
    }

    private void doMultiplications() {
        multipliedDerivations = this.consolidatedValues.keySet()
                .stream()
                .map(Pair::getFirst)
                .distinct()
                .collect(Collectors
                        .toMap(
                                Function.identity(),
                                channelContract -> decorate(channelContract.getReadingTypes().stream())
                                        .zipWithIndex()
                                        .map(pair -> new Derivation(channelContract, pair.getFirst(), channelContract.getDerivationRule(pair.getFirst()), pair.getLast().intValue()))
                                        .filter(derivation -> derivation.getDerivationRule().isMultiplied())
                                        .collect(Collectors.toList())
                        ));
        if (multipliedDerivations.isEmpty()) {
            return;
        }

        multipliedDerivations.forEach(this::doMultiplications);
    }

    private void doMultiplications(ChannelContract channelContract, List<Derivation> derivations) {
        List<IReadingType> readingTypes = channelContract.getReadingTypes();
        derivations.forEach(derivation -> doMultiplication(channelContract, readingTypes, derivation));
    }

    private void doMultiplication(ChannelContract channelContract, List<IReadingType> readingTypes, Derivation derivation) {
        consolidatedValues.entrySet()
                .stream()
                .filter(entry -> entry.getKey().getFirst().equals(channelContract))
                .forEach(entry -> {
                    Object[] values = entry.getValue();
                    Instant instant = entry.getKey().getLast();
                    int slotOffset = channelContract.getRecordSpecDefinition().slotOffset();
                    int index = derivation.getIndex();
                    if (values[index + slotOffset + 1] instanceof BigDecimal) {
                        if (DerivationRule.MULTIPLIED.equals(derivation.getDerivationRule())) {
                            if (values[index + slotOffset + 1] != storer.doNotUpdateMarker()) {
                                BigDecimal multiplier = getMultiplier(channelContract, instant, readingTypes.get(index + 1), readingTypes.get(index));
                                values[index + slotOffset] = ((BigDecimal) values[index + slotOffset + 1]).multiply(multiplier);
                            }
                        } else if (DerivationRule.MULTIPLIED_DELTA.equals(derivation.getDerivationRule())) {
                            if (values[index + slotOffset] != storer.doNotUpdateMarker()) {
                                IReadingType target = (IReadingType) readingTypes.get(index).getBulkReadingType().get();
                                BigDecimal multiplier = getMultiplier(channelContract, instant, target);
                                values[index + slotOffset] = ((BigDecimal) values[index + slotOffset]).multiply(multiplier);
                            }
                        }
                    }
                });
    }

    private BigDecimal getBigDecimal(Object object) {
        if (object instanceof BigDecimal) {
            return (BigDecimal) object;
        }
        return null;
    }

    private BigDecimal getMultiplier(ChannelContract channelContract, Instant instant, IReadingType source, IReadingType target) {
        MultiplierType multiplierType = getMultiplierType(channelContract, source, target, instant).get();
        return channelContract.getChannelsContainer().getMultiplier(multiplierType).get();
    }

    private BigDecimal getMultiplier(ChannelContract channelContract, Instant instant, IReadingType target) {
        MultiplierType multiplierType = getMultiplierType(channelContract, target, instant).get();
        return channelContract.getChannelsContainer().getMultiplier(multiplierType).orElse(BigDecimal.ONE);
    }

    private Optional<MultiplierType> getMultiplierType(ChannelContract channel, ReadingType source, ReadingType target, Instant instant) {
        return getMultiplierUsages(channel.getChannelsContainer(), instant)
                .stream()
                .filter(on(MultiplierUsage::getMeasured).test(source::equals))
                .filter(on(MultiplierUsage::getCalculated).test(optional -> optional.map(target::equals).orElse(false)))
                .findAny()
                .map(MultiplierUsage::getMultiplierType);
    }

    private Optional<MultiplierType> getMultiplierType(ChannelContract channel, ReadingType target, Instant instant) {
        return getMultiplierUsages(channel.getChannelsContainer(), instant)
                .stream()
                .filter(on(MultiplierUsage::getCalculated).test(optional -> optional.map(target::equals).orElse(false)))
                .findAny()
                .map(MultiplierUsage::getMultiplierType);
    }

    private List<MultiplierUsage> getMultiplierUsages(ChannelsContainer channelsContainer, Instant instant) {
        Stream<MultiplierUsage> meterMultipliers = channelsContainer.getMeter()
                .flatMap(meter -> meter.getConfiguration(instant))
                .map(MeterConfiguration::getReadingTypeConfigs)
                .orElseGet(Collections::emptyList)
                .stream()
                .map(MultiplierUsage.class::cast)
                .filter(multiplier -> multiplier.getCalculated().isPresent());
        Stream<MultiplierUsage> usagePointMultipliers = channelsContainer.getUsagePoint()
                .flatMap(usagePoint -> usagePoint.getConfiguration(instant))
                .map(UsagePointConfiguration::getReadingTypeConfigs)
                .orElseGet(Collections::emptyList)
                .stream()
                .map(MultiplierUsage.class::cast)
                .filter(multiplier -> multiplier.getCalculated().isPresent());
        return Stream.of(meterMultipliers, usagePointMultipliers)
                .flatMap(Function.identity())
                .collect(Collectors.toList());
    }

    private void calculateDelta(ChannelContract channel) {
        deltaDerivations.get(channel)
                .forEach(perform(this::applyDerivation).on(channel));
    }

    private void applyDerivation(ChannelContract channel, Derivation derivation) {
        int slotOffset = channel.getRecordSpecDefinition().slotOffset();
        consolidatedValues.entrySet()
                .stream()
                .filter(entry -> entry.getKey().getFirst().equals(channel))
                .forEach(entry -> {
                    Object[] consolidatedEntry = entry.getValue();
                    if (Objects.equals(consolidatedEntry[slotOffset + derivation.index], storer.doNotUpdateMarker())) {
                        Object[] previousReading = previousReadings.get(entry.getKey().withLast(Channel::getPreviousDateTime));
                        Object[] currentReading = previousReadings.get(entry.getKey());
                        int bulkIndex = slotOffset + derivation.index + 1;
                        getValue(previousReading, null, bulkIndex)
                                .ifPresent(previous -> getValue(consolidatedEntry, currentReading, bulkIndex)
                                        .ifPresent(current -> {
                                            IReadingType bulkReadingType = channel.getReadingTypes()
                                                    .get(derivation.index + 1);
                                            Instant instant = entry.getKey().getLast();
                                            Function<BigDecimal, BigDecimal> overflowCorrection = getOverflowCorrection(channel, bulkReadingType, instant, previous, current);

                                            consolidatedEntry[slotOffset + derivation.index] = overflowCorrection.apply(delta(previous, current));
                                        }));
                    }
                });
    }

    private Optional<BigDecimal> getValue(Object[] consolidatedEntry, Object[] currentEntry, int index) {
        if (consolidatedEntry != null && consolidatedEntry[index] instanceof BigDecimal) {
            return Optional.of((BigDecimal) consolidatedEntry[index]);
        }
        if (currentEntry != null && currentEntry[index] instanceof BigDecimal) {
            return Optional.of((BigDecimal) currentEntry[index]);
        }
        return Optional.empty();
    }

    private BigDecimal delta(BigDecimal previous, BigDecimal current) {
        if (previous == null || current == null) {
            return null;
        }
        return current.subtract(previous);
    }

    private Map<Pair<ChannelContract, Instant>, Object[]> readFromDb(Set<Pair<ChannelContract, Instant>> needed) {
        return needed.stream()
                .filter(not(consolidatedValues::containsKey))
                .map(pair -> Pair.of(pair, pair.getFirst().getReading(pair.getLast())))
                .filter(pair -> pair.getLast().isPresent())
                .map(pair -> pair.withLast(Optional::get))
                .map(pair -> pair.withLast(pair.getFirst().getFirst()::toArray))
                .collect(Collectors.toMap(Pair::getFirst, Pair::getLast));
    }

    @Override
    public boolean overrules() {
        return storer.overrules();
    }

    @Override
    public Map<CimChannel, Range<Instant>> getScope() {
        return Collections.unmodifiableMap(scope);
    }

    private void addScope(CimChannel channel, Instant timestamp) {
        scope.merge(channel, Range.singleton(timestamp), Range::span);
    }

    @Override
    public boolean processed(Channel channel, Instant instant) {
        return storer.processed(((ChannelContract) channel).getTimeSeries(), instant);
    }

    @Override
    public StorerProcess getStorerProcess() {
        return storerProcess;
    }

    Subscription subscribe(OverflowListener observer) {
        return overflowListeners.subscribe(observer);
    }

    Subscription subscribe(BackflowListener observer) {
        return backflowListeners.subscribe(observer);
    }
}
