package com.elster.jupiter.metering.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.ids.FieldSpec;
import com.elster.jupiter.ids.IdsService;
import com.elster.jupiter.ids.TimeSeriesDataStorer;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.CimChannel;
import com.elster.jupiter.metering.EventType;
import com.elster.jupiter.metering.ProcessStatus;
import com.elster.jupiter.metering.ReadingStorer;
import com.elster.jupiter.metering.StorerProcess;
import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.metering.readings.IntervalReading;
import com.elster.jupiter.util.Pair;
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
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.elster.jupiter.util.streams.Currying.perform;
import static com.elster.jupiter.util.streams.DecoratedStream.decorate;
import static com.elster.jupiter.util.streams.Predicates.not;

class ReadingStorerImpl implements ReadingStorer {
    private static final ProcessStatus DEFAULTPROCESSSTATUS = ProcessStatus.of();
    private final TimeSeriesDataStorer storer;

    private final Map<CimChannel, Range<Instant>> scope = new HashMap<>();
    private final EventService eventService;
    private final Map<Pair<ChannelContract, Instant>, Object[]> consolidatedValues = new HashMap<>();
    private final Map<Pair<ChannelContract, Instant>, BaseReading> readings = new HashMap<>();
    private final StorerProcess storerProcess;
    private Map<ChannelContract, List<Derivation>> deltaDerivations = new HashMap<>();
    private Map<Pair<ChannelContract, Instant>, Object[]> previous;

    private static class Derivation {
        private final IReadingType readingType;
        private final DerivationRule derivationRule;
        private final int index;

        public Derivation(IReadingType readingType, DerivationRule derivationRule, int index) {
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
    }

    private ReadingStorerImpl(IdsService idsService, EventService eventService, UpdateBehaviour updateBehaviour, StorerProcess storerProcess) {
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

        /*
TODO
        We need to detect whether any columns have derivation rules that require delta calculation.
        If any such column exists we'll need to determine which points in time we'll need to check for existing data,
        since that existing data, if available will allow us to calculate delta values.
        Likely some of the needed data are in the values we're about to write,
        so we'll need to set data from both these sources up so its origin is transparent to the delta calculation itself.

        Secondly we'll need to apply multipliers if any derivation rules are multiplied. So if any such fields exist
        we should figure out which multipliers apply, taking into account multipliers may change over time.

        All these additional calculations should be added to the consolidated values. However, whenever a value was already supplied no calculation is necessary.

         */

        doDerivations();
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

    private void doDerivations() {
        previous = new HashMap<>();

        Map<Pair<ChannelContract, Instant>, Object[]> valuesView = Maps.filterKeys(this.consolidatedValues, pair -> pair.getFirst().isRegular());
        List<Pair<ChannelContract, Instant>> needed = valuesView.keySet()
                .stream()
                .filter(pair11 -> pair11.getFirst().isRegular())
                .map(pair -> Pair.of(pair.getFirst(), pair.getFirst().getPreviousDateTime(pair.getLast())))
                .collect(Collectors.toList());

        previous.putAll(Maps.filterKeys(valuesView, needed::contains));
        previous.putAll(readFromDb(needed));

        // ok we've got all the previous readings that are available

        deltaDerivations = valuesView.keySet()
                .stream()
                .map(Pair::getFirst)
                .distinct()
                .collect(Collectors
                        .toMap(
                                Function.identity(),
                                channelContract -> decorate(channelContract.getReadingTypes().stream())
                                        .zipWithIndex()
                                        .map(pair -> new Derivation(pair.getFirst(), channelContract.getDerivationRule(pair.getFirst()), pair.getLast().intValue()))
                                        .filter(derivation -> derivation.getDerivationRule().isDelta())
                                        .collect(Collectors.toList())
                        ));

        valuesView.keySet()
                .stream()
                .map(Pair::getFirst)
                .distinct()
                .forEach(this::calculateDelta);
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
                        Object[] previousReading = previous.get(entry.getKey().withLast(Channel::getPreviousDateTime));
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
