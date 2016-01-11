package com.elster.jupiter.metering.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.ids.FieldSpec;
import com.elster.jupiter.ids.IdsService;
import com.elster.jupiter.ids.TimeSeriesDataStorer;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.CimChannel;
import com.elster.jupiter.metering.EventType;
import com.elster.jupiter.metering.MultiplierType;
import com.elster.jupiter.metering.MultiplierUsage;
import com.elster.jupiter.metering.ProcessStatus;
import com.elster.jupiter.metering.ReadingStorer;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.StorerProcess;
import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.metering.readings.IntervalReading;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.streams.Predicates;
import com.google.common.collect.Maps;
import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.elster.jupiter.util.streams.Currying.perform;
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
    private Map<Pair<ChannelContract, Instant>, Object[]> previousReadings;
    private Map<ChannelContract, List<Derivation>> deltaDerivations;
    private Map<ChannelContract, List<Derivation>> multipliedDerivations;

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

    private ReadingStorerImpl(IdsService idsService, EventService eventService, UpdateBehaviour updateBehaviour, StorerProcess storerProcess) {
        this.idsService = idsService;
        this.eventService = eventService;
        this.storer = updateBehaviour.createTimeSeriesStorer(idsService);
        this.storerProcess = storerProcess;
    }

    static ReadingStorer createNonOverrulingStorer(IdsService idsService, EventService eventService) {
        return new ReadingStorerImpl(idsService, eventService, Behaviours.INSERT_ONLY, StorerProcess.DEFAULT);
    }

    static ReadingStorer createOverrulingStorer(IdsService idsService, EventService eventService) {
        return new ReadingStorerImpl(idsService, eventService, Behaviours.OVERRULE, StorerProcess.DEFAULT);
    }

    static ReadingStorer createUpdatingStorer(IdsService idsService, EventService eventService) {
        return new ReadingStorerImpl(idsService, eventService, Behaviours.UPDATE, StorerProcess.DEFAULT);
    }

    static ReadingStorer createUpdatingStorer(IdsService idsService, EventService eventService, StorerProcess storerProcess) {
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
        };
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
                .forEach(i -> {
                    values[i] = valuesToAdd[i];
                });

        ProcessStatus processStatus;
        if (values[0] != null && !Objects.equals(storer.doNotUpdateMarker(), values[0])) {
            processStatus = status.or(new ProcessStatus((Long) values[0]));
        } else {
            processStatus = status;
        }
        values[0] = processStatus.getBits();
        if (reading instanceof IntervalReading) {
            IntervalReading intervalReading = (IntervalReading) reading;
            long bits = !(values[1] instanceof Long) ? 0L : (long) values[1];
            bits |= intervalReading.getProfileStatus().getBits();
            values[1] = bits;
        }
        addScope(channel, reading.getTimeStamp());
    }

    @Override
    public void execute() {
        doDeltas();
        doMultiplications();
        consolidatedValues.entrySet().stream()
                .forEach(entry -> {
                    ChannelContract channel = entry.getKey().getFirst();
                    Instant timestamp = entry.getKey().getLast();
                    Object[] values = entry.getValue();
                    channel.validateValues(readings.get(entry.getKey()), values);
                    storer.add(channel.getTimeSeries(), timestamp, values);
                });
        storer.execute();
        eventService.postEvent(EventType.READINGS_CREATED.topic(), this);
    }

    private void doDeltas() {
        Map<Pair<ChannelContract, Instant>, Object[]> valuesView = Maps.filterKeys(this.consolidatedValues, pair -> pair.getFirst().isRegular());
        deltaDerivations = valuesView.keySet()
                .stream()
                .map(Pair::getFirst)
                .distinct()
                .collect(Collectors.toMap(
                        Function.identity(),
                        this::getDerivations
                ));

        previousReadings = new HashMap<>();
        List<Pair<ChannelContract, Instant>> needed = valuesView.keySet()
                .stream()
                .map(pair -> Pair.of(pair.getFirst(), pair.getFirst().getPreviousDateTime(pair.getLast())))
                .collect(Collectors.toList());
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

    private List<Derivation> getDerivations(ChannelContract channel) {
        return decorate(channel.getReadingTypes().stream())
                .zipWithIndex()
                .map(pair -> new Derivation(channel, pair.getFirst(), channel.getDerivationRule(pair.getFirst()), pair.getLast().intValue()))
                .filter(derivation -> derivation.getDerivationRule().isDelta())
                .collect(Collectors.toList());
    }

    private void updateExistingRecords(Map<Pair<ChannelContract, Instant>, Object[]> valuesView) {
        List<Pair<ChannelContract, Instant>> mayNeedToUpdateTimes = valuesView.keySet()
                .stream()
                .map(pair -> Pair.of(pair.getFirst(), pair.getFirst().getNextDateTime(pair.getLast())))
                .filter(not(consolidatedValues::containsKey))
                .collect(Collectors.toList());
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
        if (currentBulk != null && previousBulk != null) {
            BigDecimal delta = currentBulk.subtract(previousBulk);
            if (derivation.getDerivationRule().isMultiplied()) {
                IReadingType bulkReadingType = (IReadingType) derivation.getReadingType().getBulkReadingType().get();
                BigDecimal multiplier = getMultiplier(channel, instant, bulkReadingType);
                delta = delta.multiply(multiplier);
            }
            toUpdate[index] = delta;
            return true;
        }
        return false;
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
        derivations.forEach(derivation -> {
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
        return channelContract.getMeterActivation().getMultiplier(multiplierType).get();
    }

    private BigDecimal getMultiplier(ChannelContract channelContract, Instant instant, IReadingType target) {
        MultiplierType multiplierType = getMultiplierType(channelContract, target, instant).get();
        return channelContract.getMeterActivation().getMultiplier(multiplierType).orElse(BigDecimal.ONE);
    }

    private Optional<MultiplierType> getMultiplierType(ChannelContract channel, ReadingType source, ReadingType target, Instant instant) {
        return channel.getMeterActivation()
                .getMultiplierUsages(instant)
                .stream()
                .filter(on(MultiplierUsage::getMeasured).test(source::equals))
                .filter(on(MultiplierUsage::getCalculated).test(optional -> optional.map(target::equals).orElse(false)))
                .findAny()
                .map(MultiplierUsage::getMultiplierType);
    }

    private Optional<MultiplierType> getMultiplierType(ChannelContract channel, ReadingType target, Instant instant) {
        return channel.getMeterActivation()
                .getMultiplierUsages(instant)
                .stream()
                .filter(on(MultiplierUsage::getCalculated).test(optional -> optional.map(target::equals).orElse(false)))
                .findAny()
                .map(MultiplierUsage::getMultiplierType);
    }

    private void calculateDelta(ChannelContract channel) {
        deltaDerivations.get(channel)
                .stream()
                .forEach(perform(this::applyDerivation).on(channel));
    }

    private void applyDerivation(ChannelContract channel, Derivation derivation) {
        int slotOffset = channel.getRecordSpecDefinition().slotOffset();
        consolidatedValues.entrySet()
                .stream()
                .filter(entry -> entry.getKey().getFirst().equals(channel))
                .forEach(entry -> {
                    if (Objects.equals(entry.getValue()[slotOffset + derivation.index], storer.doNotUpdateMarker())) {
                        Object[] previousReading = previousReadings.get(entry.getKey().withLast(Channel::getPreviousDateTime));
                        if (previousReading != null) {
                            int bulkIndex = slotOffset + derivation.index + 1;
                            BigDecimal previous = (BigDecimal) previousReading[bulkIndex];
                            BigDecimal current = (BigDecimal) entry.getValue()[bulkIndex];
                            entry.getValue()[slotOffset + derivation.index] = delta(previous, current);
                        }
                    }
                });
    }

    private BigDecimal delta(BigDecimal previous, BigDecimal current) {
        if (previous == null || current == null) {
            return null;
        }
        return current.subtract(previous);
    }

    private Map<Pair<ChannelContract, Instant>, Object[]> readFromDb(List<Pair<ChannelContract, Instant>> needed) {
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
}
