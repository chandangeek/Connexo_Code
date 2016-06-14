package com.elster.jupiter.metering.impl;

import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.ids.IdsService;
import com.elster.jupiter.ids.RecordSpec;
import com.elster.jupiter.ids.TimeSeries;
import com.elster.jupiter.ids.TimeSeriesDataStorer;
import com.elster.jupiter.ids.TimeSeriesEntry;
import com.elster.jupiter.ids.Vault;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.CimChannel;
import com.elster.jupiter.metering.EventType;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterConfiguration;
import com.elster.jupiter.metering.MeterReadingTypeConfiguration;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.MultiplierUsage;
import com.elster.jupiter.metering.ProcessStatus;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointConfiguration;
import com.elster.jupiter.metering.UsagePointReadingTypeConfiguration;
import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DoesNotExistException;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.streams.ExtraCollectors;
import com.elster.jupiter.util.streams.Functions;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;

import javax.inject.Inject;
import java.time.Clock;
import java.time.Instant;
import java.time.Period;
import java.time.ZoneId;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.elster.jupiter.util.Checks.is;
import static com.elster.jupiter.util.conditions.Where.where;
import static com.elster.jupiter.util.streams.Currying.use;
import static com.elster.jupiter.util.streams.Predicates.on;
import static com.elster.jupiter.util.streams.Predicates.self;

public final class ChannelImpl implements ChannelContract {

    static final int INTERVALVAULTID = 1;
    static final int IRREGULARVAULTID = 2;
    static final int DAILYVAULTID = 3;
    private static final Order ORDER_CHRONOLOGICAL = Order.ascending("readingTimestamp");

    // persistent fields
    private long id;
    private long version;
    @SuppressWarnings("unused")
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;
    @SuppressWarnings("unused")
    private String userName;

    // associations
    private Reference<ChannelsContainer> channelsContainer = ValueReference.absent();
    private Reference<TimeSeries> timeSeries = ValueReference.absent();
    private Reference<IReadingType> mainReadingType = ValueReference.absent();
    private DerivationRule mainDerivationRule;
    private Reference<IReadingType> bulkQuantityReadingType = ValueReference.absent();
    private DerivationRule bulkDerivationRule;
    private List<ReadingTypeInChannel> readingTypeInChannels = new ArrayList<>();

    private final IdsService idsService;
    private final MeteringService meteringService;
    @SuppressWarnings("unused")
    private final Clock clock;
    private final DataModel dataModel;
    private final EventService eventService;

    @Inject
    ChannelImpl(DataModel dataModel, IdsService idsService, MeteringService meteringService, Clock clock, EventService eventService) {
        this.dataModel = dataModel;
        this.idsService = idsService;
        this.meteringService = meteringService;
        this.clock = clock;
        this.eventService = eventService;
    }

    public ChannelImpl init(ChannelsContainer channelsContainer, List<IReadingType> readingTypes) {
        return init(channelsContainer, readingTypes, this::determineRule);
    }

    public ChannelImpl init(ChannelsContainer channelsContainer, List<IReadingType> readingTypes, BiFunction<IReadingType, IReadingType, DerivationRule> ruleDetermination) {
        this.channelsContainer.set(channelsContainer);
        this.mainReadingType.set(readingTypes.get(0));
        for (int index = 0; index < readingTypes.size(); index++) {
            DerivationRule rule = DerivationRule.MEASURED;
            IReadingType currentReadingType = readingTypes.get(index);
            if (readingTypes.size() > index + 1) {
                rule = ruleDetermination.apply(currentReadingType, readingTypes.get(index + 1));
            }
            if (index == 0) {
                mainDerivationRule = rule;
            } else if (index == 1 && (mainDerivationRule.isDelta() || mainReadingType.get().isBulkQuantityReadingType(currentReadingType))) {
                bulkQuantityReadingType.set(currentReadingType);
                bulkDerivationRule = rule;
            } else {
                ReadingTypeInChannel readingTypeInChannel = ReadingTypeInChannel.from(dataModel, this, currentReadingType, rule);
                this.readingTypeInChannels.add(readingTypeInChannel);
            }
        }
        this.timeSeries.set(createTimeSeries(channelsContainer.getZoneId()));
        return this;
    }

    private DerivationRule determineRule(ReadingType readingType, ReadingType possibleBase) {
        if (readingType.isBulkQuantityReadingType(possibleBase)) {
            return DerivationRule.DELTA;
        }
        List<MultiplierUsage> multiplierUsages = getMultipliers(possibleBase);

        if (multiplierUsages
                .stream()
                .filter(on(MultiplierUsage::getMeasured).test(possibleBase::equals))
                .map(MultiplierUsage::getCalculated)
                .flatMap(Functions.asStream())
                .map(use(ReadingType::equals).on(readingType))
                .anyMatch(self())) {
            return DerivationRule.MULTIPLIED;
        }

        if (multiplierUsages
                .stream()
                .map(MultiplierUsage::getCalculated)
                .flatMap(Functions.asStream())
                .map(use(ReadingType::isBulkQuantityReadingType).on(readingType))
                .anyMatch(self())) {
            return DerivationRule.MULTIPLIED_DELTA;
        }

        return DerivationRule.MEASURED;
    }

    private List<MultiplierUsage> getMultipliers(ReadingType readingType) {
        return getMultipliers(clock.instant())
                .stream()
                .filter(on(MultiplierUsage::getMeasured).test(readingType::equals))
                .collect(Collectors.toList());
    }

    private List<? extends MultiplierUsage> getMeterMultipliers(Instant instant) {
        return getChannelsContainer()
                .getMeter()
                .flatMap(use(Meter::getConfiguration).with(instant))
                .map(MeterConfiguration::getReadingTypeConfigs)
                .orElseGet(Collections::emptyList);
    }

    private List<? extends MultiplierUsage> getUsagePointMultipliers(Instant instant) {
        return getChannelsContainer()
                .getUsagePoint()
                .flatMap(use(UsagePoint::getConfiguration).with(instant))
                .map(UsagePointConfiguration::getReadingTypeConfigs)
                .orElseGet(Collections::emptyList);
    }

    private List<? extends MultiplierUsage> getMultipliers(Instant instant) {
        return Stream.of(getMeterMultipliers(instant), getUsagePointMultipliers(instant))
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public ChannelsContainer getChannelsContainer() {
        return channelsContainer.get();
    }

    public TimeSeries getTimeSeries() {
        return timeSeries.get();
    }

    @Override
    public DerivationRule getDerivationRule(IReadingType readingType) {
        int index = getReadingTypes().indexOf(readingType);
        if (index == -1) {
            throw new IllegalArgumentException();
        }
        return getDerivationRules().get(index);
    }

    public List<DerivationRule> getDerivationRules() {
        ImmutableList.Builder<DerivationRule> builder = ImmutableList.builder();
        builder.add(mainDerivationRule);
        if (bulkQuantityReadingType.isPresent()) {
            builder.add(bulkDerivationRule);
        }
        readingTypeInChannels
                .stream()
                .map(ReadingTypeInChannel::getDerivationRule)
                .forEach(builder::add);
        return builder.build();
    }

    public ZoneId getZoneId() {
        return timeSeries.get().getZoneId();
    }

    @Override
    public Instant getLastDateTime() {
        return timeSeries.get().getLastDateTime();
    }

    @Override
    public Instant getFirstDateTime() {
        return timeSeries.get().getFirstDateTime();
    }

    @Override
    public Instant getNextDateTime(Instant instant) {
        return getTimeSeries().getNextDateTime(instant);
    }

    @Override
    public Instant getPreviousDateTime(Instant instant) {
        return getTimeSeries().getPreviousDateTime(instant);
    }

    @Override
    public Optional<TemporalAmount> getIntervalLength() {
        Iterator<IReadingType> it = getReadingTypes().iterator();
        Optional<TemporalAmount> result = it.next().getIntervalLength();
        while (it.hasNext()) {
            IReadingType readingType = it.next();
            Optional<TemporalAmount> intervalLength = readingType.getIntervalLength();
            if (!intervalLength.equals(result)) {
                throw new IllegalArgumentException();
            }
        }
        return result;
    }

    private TimeSeries createTimeSeries(ZoneId zoneId) {
        Vault vault = getVault();
        RecordSpec recordSpec = getRecordSpec();
        TimeZone timeZone = TimeZone.getTimeZone(zoneId);
        return isRegular() ?
                vault.createRegularTimeSeries(recordSpec, timeZone, getIntervalLength().get(), 0) :
                vault.createIrregularTimeSeries(recordSpec, timeZone);
    }

    @Override
    public Object[] toArray(BaseReading reading, ReadingType readingType, ProcessStatus status) {
        int index = getReadingTypes().indexOf(readingType);
        if (index < 0) {
            throw new IllegalArgumentException();
        }
        return getRecordSpecDefinition().toArray(reading, index, status);
    }

    @Override
    public Object[] toArray(BaseReadingRecord readingRecord) {
        if (readingRecord instanceof BaseReadingRecordImpl) {
            return ((BaseReadingRecordImpl) readingRecord).getEntry().getValues();
        }
        RecordSpecs recordSpecDefinition = getRecordSpecDefinition();
        List<IReadingType> readingTypes = getReadingTypes();

        return readingRecord.getReadingTypes()
                .stream()
                .map(readingType -> recordSpecDefinition.toArray(readingRecord.filter(readingType), readingTypes.indexOf(readingType), readingRecord.getProcessStatus()))
                .reduce(null, this::merge);
    }

    private Object[] merge(Object[] first, Object[] second) {
        if (first == null) {
            return second;
        }
        if (second == null) {
            return first;
        }
        Object[] longest = first.length > second.length ? first : second;
        Object[] shortest = first.length > second.length ? second : first;
        for (int i = 0; i < shortest.length; i++) {
            longest[i] = mergeObject(longest[i], shortest[i]);
        }
        return longest;
    }

    private Object mergeObject(Object first, Object second) {
        if (first == null) {
            return second;
        }
        if (second == null) {
            return first;
        }
        if (Objects.equals(first, second)) {
            return first;
        }
        throw new IllegalArgumentException();
    }

    @Override
    public void validateValues(BaseReading reading, Object[] values) {
        getRecordSpecDefinition().validateValues(reading, values);
    }

    @Override
    public Optional<Range<Instant>> getTimePeriod(BaseReading reading, Object[] values) {
        return getRecordSpecDefinition().getTimePeriod(reading, values);
    }

    @Override
    public RecordSpecs getRecordSpecDefinition() {
        if (isRegular()) {
            if (bulkQuantityReadingType.isPresent()) {
                return RecordSpecs.BULKQUANTITYINTERVAL;
            }
            return mainDerivationRule.isMultiplied() ? RecordSpecs.VALUE_MULTIPLIED_INTERVAL : RecordSpecs.SINGLEINTERVAL;
        } else {
            if (hasMacroPeriod()) {
                return RecordSpecs.BILLINGPERIOD;
            }
            return hasMultiplier() ? RecordSpecs.BASEREGISTER_WITH_MULTIPLIED_REGISTER : RecordSpecs.BASEREGISTER;
        }
    }

    private boolean hasMultiplier() {
        List<IReadingType> readingTypes = getReadingTypes();
        if (readingTypes.size() == 1) {
            return false;
        }
        Predicate<MultiplierUsage> matchesReadingTypes = config -> readingTypes.get(0).equals(config.getCalculated().get()) && readingTypes.get(1).equals(config.getMeasured());
        boolean usagePointHasMultiplier = usagePointHasMultiplier(matchesReadingTypes);
        return meterHasMultiplier(matchesReadingTypes) || usagePointHasMultiplier;
    }

    private boolean usagePointHasMultiplier(Predicate<MultiplierUsage> matchesReadingTypes) {
        return getChannelsContainer().getUsagePoint()
                .flatMap(use(UsagePoint::getConfiguration).with(getChannelsContainer().getStart()))
                .map(UsagePointConfiguration::getReadingTypeConfigs)
                .orElseGet(Collections::emptyList)
                .stream()
                .filter(on(UsagePointReadingTypeConfiguration::getCalculated).test(Optional::isPresent))
                .anyMatch(matchesReadingTypes);
    }

    private boolean meterHasMultiplier(Predicate<MultiplierUsage> matchesReadingTypes) {
        return getChannelsContainer().getMeter()
                .flatMap(use(Meter::getConfiguration).with(getChannelsContainer().getStart()))
                .map(MeterConfiguration::getReadingTypeConfigs)
                .orElseGet(Collections::emptyList)
                .stream()
                .filter(on(MeterReadingTypeConfiguration::getCalculated).test(Optional::isPresent))
                .anyMatch(matchesReadingTypes);
    }

    Optional<IReadingType> getDerivedReadingType(IReadingType readingType) {
        List<IReadingType> readingTypes = getReadingTypes();
        return IntStream.range(1, readingTypes.size())
                .filter(i -> readingTypes.get(i).equals(readingType))
                .map(i -> i - 1)
                .mapToObj(readingTypes::get)
                .filter(candidate -> DerivationRule.MEASURED != getDerivationRule(candidate))
                .findFirst();
    }

    private RecordSpec getRecordSpec() {
        return getRecordSpecDefinition().get(idsService);
    }

    private int getVaultId() {
        return getIntervalLength()
                .map(temporalAmount -> temporalAmount instanceof Period ? DAILYVAULTID : INTERVALVAULTID)
                .orElse(IRREGULARVAULTID);
    }

    private Vault getVault() {
        Optional<Vault> result = idsService.getVault(MeteringService.COMPONENTNAME, getVaultId());
        if (result.isPresent()) {
            return result.get();
        }
        throw new DoesNotExistException(String.valueOf(id));
    }

    @Override
    public List<IReadingType> getReadingTypes() {
        return Stream.of(
                mainReadingType.stream(),
                bulkQuantityReadingType.stream(),
                readingTypeInChannels.stream().map(ReadingTypeInChannel::getReadingType)
        )
                .flatMap(Function.identity())
                .collect(ExtraCollectors.toImmutableList());
    }

    @Override
    public List<IntervalReadingRecord> getIntervalReadings(Range<Instant> interval) {
        if (!isRegular()) {
            return Collections.emptyList();
        }
        return getTimeSeries().getEntries(interval)
                .stream()
                .map(use(IntervalReadingRecordImpl::new).on(this))
                .collect(ExtraCollectors.toImmutableList());
    }

    private BaseReadingRecord createReading(TimeSeriesEntry entry, boolean regular) {
        return regular ? new IntervalReadingRecordImpl(this, entry) : new ReadingRecordImpl(this, entry);
    }

    @Override
    public List<BaseReadingRecord> getReadings(Range<Instant> interval) {
        return getTimeSeries().getEntries(interval)
                .stream()
                .map(use(this::createReading).with(isRegular()))
                .collect(ExtraCollectors.toImmutableList());
    }

    public Optional<BaseReadingRecord> getReading(Instant when) {
        return getTimeSeries().getEntry(when).map(entryHolder -> createReading(entryHolder, isRegular()));
    }

    @Override
    public List<IntervalReadingRecord> getIntervalReadings(ReadingType readingType, Range<Instant> interval) {
        if (!isRegular()) {
            return Collections.emptyList();
        }
        return getTimeSeries().getEntries(interval).stream()
                .map(entry -> new IntervalReadingRecordImpl(this, entry))
                .map(reading -> reading.filter(readingType))
                .collect(ExtraCollectors.toImmutableList());
    }

    @Override
    public List<ReadingRecord> getRegisterReadings(ReadingType readingType, Range<Instant> interval) {
        if (isRegular()) {
            return Collections.emptyList();
        }
        return getTimeSeries().getEntries(interval).stream()
                .map(entry -> new ReadingRecordImpl(this, entry))
                .map(reading -> reading.filter(readingType))
                .collect(ExtraCollectors.toImmutableList());
    }

    @Override
    public List<BaseReadingRecord> getReadings(ReadingType readingType, Range<Instant> interval) {
        List<TimeSeriesEntry> entries = getTimeSeries().getEntries(interval);
        return toReadings(readingType, entries);
    }

    @Override
    public List<BaseReadingRecord> getReadingsUpdatedSince(ReadingType readingType, Range<Instant> interval, Instant since) {
        List<TimeSeriesEntry> entries = getTimeSeries().getEntriesUpdatedSince(interval, since);
        return entries.stream()
                .map(entry -> asDifferingReading(entry, readingType, since))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private BaseReadingRecord asDifferingReading(TimeSeriesEntry entry, ReadingType readingType, Instant since) {
        BaseReadingRecord currentReading = toReadingFunction().apply(entry);
        Optional<TimeSeriesEntry> oldEntry = entry.getVersion(since);
        if (!oldEntry.isPresent()) {
            return currentReading;
        }
        BaseReadingRecord oldReading = oldEntry.map(toReadingFunction()).get();
        return is(oldReading.getQuantity(readingType)).equalTo(currentReading.getQuantity(readingType)) ? null : currentReading;
    }

    private List<BaseReadingRecord> toReadings(ReadingType readingType, List<TimeSeriesEntry> entries) {
        Function<TimeSeriesEntry, BaseReadingRecord> readingMapper = toReadingFunction();
        return entries.stream()
                .map(readingMapper)
                .map(reading -> reading.filter(readingType))
                .collect(ExtraCollectors.toImmutableList());
    }

    private Function<TimeSeriesEntry, BaseReadingRecord> toReadingFunction() {
        return isRegular() ?
                entry -> new IntervalReadingRecordImpl(this, entry) :
                entry -> new ReadingRecordImpl(this, entry);
    }

    @Override
    public List<ReadingRecord> getRegisterReadings(Range<Instant> interval) {
        if (isRegular()) {
            return Collections.emptyList();
        }
        return getTimeSeries().getEntries(interval).stream()
                .map(entry -> new ReadingRecordImpl(this, entry))
                .collect(ExtraCollectors.toImmutableList());
    }

    @Override
    public IReadingType getMainReadingType() {
        return mainReadingType.get();
    }

    @Override
    public Optional<IReadingType> getBulkQuantityReadingType() {
        return bulkQuantityReadingType.getOptional();
    }

    @Override
    public ReadingQualityRecord createReadingQuality(ReadingQualityType type, ReadingType readingType, BaseReading baseReading) {
        ReadingQualityRecordImpl readingQualityRecord = ReadingQualityRecordImpl.from(dataModel, type, getCimChannel(readingType).orElseThrow(IllegalArgumentException::new), baseReading);
        readingQualityRecord.doSave();
        return readingQualityRecord;
    }

    @Override
    public ReadingQualityRecord createReadingQuality(ReadingQualityType type, ReadingType readingType, Instant timestamp) {
        ReadingQualityRecordImpl readingQualityRecord = ReadingQualityRecordImpl.from(dataModel, type, getCimChannel(readingType).orElseThrow(IllegalArgumentException::new), timestamp);
        readingQualityRecord.doSave();
        return readingQualityRecord;
    }

    ReadingQualityRecord copyReadingQuality(ReadingQualityRecord source) {
        ReadingQualityRecordImpl readingQualityRecord = ReadingQualityRecordImpl.from(dataModel, source.getType(), getCimChannel(source.getReadingType()).get(), source.getReadingTimestamp());
        readingQualityRecord.copy(source);
        readingQualityRecord.doSave();
        return readingQualityRecord;
    }

    @Override
    public Optional<CimChannel> getCimChannel(ReadingType readingType) {
        Stream<CimChannel> main = mainReadingType.map(rt -> (CimChannel) new CimChannelAdapter(this, rt, dataModel, meteringService)).map(Stream::of).orElse(Stream.empty());
        Stream<CimChannel> bulk = bulkQuantityReadingType.map(rt -> (CimChannel) new CimChannelAdapter(this, rt, dataModel, meteringService)).map(Stream::of).orElse(Stream.empty());
        Stream<ReadingTypeInChannel> readingTypeInChannel = readingTypeInChannels.stream();
        return Stream.of(main, bulk, readingTypeInChannel)
                .flatMap(Function.identity())
                .filter(cimChannel -> cimChannel.getReadingType().equals(readingType))
                .findFirst();
    }

    @Override
    public long getVersion() {
        return version;
    }

    @Override
    public List<ReadingQualityRecord> findReadingQuality(Range<Instant> range) {
        Condition condition = inRange(range).and(ofThisChannel());
        return dataModel.mapper(ReadingQualityRecord.class).select(condition, ORDER_CHRONOLOGICAL);
    }

    @Override
    public List<ReadingQualityRecord> findActualReadingQuality(Range<Instant> range) {
        Condition condition = inRange(range).and(ofThisChannel()).and(isActual());
        return dataModel.mapper(ReadingQualityRecord.class).select(condition, ORDER_CHRONOLOGICAL);
    }

    private Condition isActual() {
        return where("actual").isEqualTo(true);
    }

    private Condition inRange(Range<Instant> range) {
        return where("readingTimestamp").in(range);
    }

    private Condition ofThisChannel() {
        return where("channel").isEqualTo(this);
    }

    private Condition ofType(ReadingQualityType type) {
        return where("typeCode").isEqualTo(type.getCode());
    }

    private Condition ofOneOfTypes(Stream<ReadingQualityType> types) {
        return where("typeCode").in(types.map(ReadingQualityType::getCode).collect(Collectors.toList()));
    }

    private Condition ofIndex(QualityCodeIndex index) {
        return where("typeCode").like(Joiner.on('.').join('*', index.category().ordinal(), index.index()));
    }

    private Condition ofOneOfSystems(Stream<QualityCodeSystem> systems) {
        return where("typeCode").matches("^(" + systems
                .map(system -> Integer.toString(system.ordinal()))
                .collect(Collectors.joining("|")) + ")\\.\\d+\\.\\d+$", "");
    }

    @Override
    public Optional<ReadingQualityRecord> findReadingQuality(ReadingQualityType type, Instant timestamp) {
        Condition condition = ofThisChannel().and(withTimestamp(timestamp)).and(ofType(type));
        return dataModel.mapper(ReadingQualityRecord.class).select(condition).stream().findFirst();
    }

    private Condition withTimestamp(Instant timestamp) {
        return where("readingTimestamp").isEqualTo(timestamp);
    }

    @Override
    public List<ReadingQualityRecord> findReadingQuality(ReadingQualityType type, Range<Instant> range) {
        Condition ofTypeAndInInterval = ofThisChannel().and(inRange(range)).and(ofType(type));
        return dataModel.mapper(ReadingQualityRecord.class).select(ofTypeAndInInterval, ORDER_CHRONOLOGICAL);
    }

    @Override
    public List<ReadingQualityRecord> findActualReadingQuality(ReadingQualityType type, Range<Instant> interval) {
        Condition ofTypeAndInInterval = ofThisChannel().and(inRange(interval)).and(ofType(type)).and(isActual());
        return dataModel.mapper(ReadingQualityRecord.class).select(ofTypeAndInInterval, ORDER_CHRONOLOGICAL);
    }

    @Override
    public List<ReadingQualityRecord> findReadingQualities(Set<QualityCodeSystem> qualityCodeSystems, QualityCodeIndex index,
                                                           Range<Instant> interval, boolean checkIfActual, boolean sort) {
        Condition selectionCondition = ofThisChannel().and(inRange(interval));
        boolean ignoreQualityCodeSystem = qualityCodeSystems == null || qualityCodeSystems.isEmpty();
        if (!ignoreQualityCodeSystem || index != null) {
            selectionCondition = selectionCondition.and(ignoreQualityCodeSystem ?
                    ofIndex(index) :
                    index == null ?
                            ofOneOfSystems(qualityCodeSystems.stream()) :
                            ofOneOfTypes(qualityCodeSystems.stream().map(system -> ReadingQualityType.of(system, index))));
        }
        if (checkIfActual) {
            selectionCondition = selectionCondition.and(isActual());
        }
        DataMapper<ReadingQualityRecord> mapper = dataModel.mapper(ReadingQualityRecord.class);
        return sort ? mapper.select(selectionCondition, ORDER_CHRONOLOGICAL) : mapper.select(selectionCondition);
    }

    @Override
    public List<ReadingQualityRecord> findReadingQualities(Instant timestamp) {
        Condition atTimestamp = ofThisChannel().and(withTimestamp(timestamp));
        return dataModel.mapper(ReadingQualityRecord.class).select(atTimestamp, ORDER_CHRONOLOGICAL);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        return id == ((ChannelImpl) o).id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public boolean isRegular() {
        return getIntervalLength().isPresent();
    }

    @Override
    public List<BaseReadingRecord> getReadingsBefore(Instant when, int readingCount) {
        boolean regular = isRegular();
        List<TimeSeriesEntry> entries = getTimeSeries().getEntriesBefore(when, readingCount);
        ImmutableList.Builder<BaseReadingRecord> builder = ImmutableList.builder();
        for (TimeSeriesEntry entry : entries) {
            builder.add(createReading(entry, regular));
        }
        return builder.build();
    }

    @Override
    public List<BaseReadingRecord> getReadingsOnOrBefore(Instant when, int readingCount) {
        boolean regular = isRegular();
        List<TimeSeriesEntry> entries = getTimeSeries().getEntriesOnOrBefore(when, readingCount);
        ImmutableList.Builder<BaseReadingRecord> builder = ImmutableList.builder();
        entries.forEach(entry -> builder.add(createReading(entry, regular)));
        return builder.build();
    }

    @Override
    public boolean hasMacroPeriod() {
        return !MacroPeriod.NOTAPPLICABLE.equals(mainReadingType.get().getMacroPeriod());
    }

    @Override
    public boolean hasData() {
        return getTimeSeries().getFirstDateTime() != null || getTimeSeries().getLastDateTime() != null;
    }

    @Override
    public void editReadings(List<? extends BaseReading> readings) {
        getCimChannel(getMainReadingType()).get().editReadings(readings);
    }

    @Override
    public void confirmReadings(List<? extends BaseReading> readings) {
        getCimChannel(getMainReadingType()).get().confirmReadings(readings);
        if (getBulkQuantityReadingType().isPresent() && getCimChannel(getBulkQuantityReadingType().get()).isPresent()) {
            getCimChannel(getBulkQuantityReadingType().get()).get().confirmReadings(readings);
        }
    }

    @Override
    public void removeReadings(List<? extends BaseReadingRecord> readings) { // TODO make this for a specific readingType
        if (!readings.isEmpty()) {
            Set<Instant> readingTimes = readings.stream().map(BaseReading::getTimeStamp).collect(Collectors.toSet());
            readingTimes.forEach(instant -> timeSeries.get().removeEntry(instant));
            findReadingQualities(null, null, Range.encloseAll(readingTimes), false, false).stream()
                    .filter(quality -> readingTimes.contains(quality.getReadingTimestamp()))
                    .forEach(ReadingQualityRecord::delete);
            // TODO: refactor with custom QualityCodeSystem(s) in scope of data editing refactoring (CXO-1449)
            ReadingQualityType rejected = ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeIndex.REJECTED);
            readingTimes.forEach(readingTime -> {
                createReadingQuality(rejected, mainReadingType.get(), readingTime);
                getBulkQuantityReadingType().ifPresent(bulkReadingType -> createReadingQuality(rejected, bulkReadingType, readingTime));
            });
            eventService.postEvent(EventType.READINGS_DELETED.topic(), new ReadingsDeletedEventImpl(this, readingTimes));
        }
    }

    void deleteReadings(List<? extends BaseReadingRecord> readings) {
        if (!readings.isEmpty()) {
            Set<Instant> readingTimes = readings.stream()
                    .map(BaseReading::getTimeStamp)
                    .collect(Collectors.toSet());
            readingTimes.forEach(instant -> timeSeries.get().removeEntry(instant));
        }
    }

    @Override
    public List<Instant> toList(Range<Instant> range) {
        return timeSeries.get().toList(range);
    }

    void copyReadings(List<BaseReadingRecord> readings) {
        if (!readings.isEmpty()) {
            TimeSeriesDataStorer storer = idsService.createOverrulingStorer();

            readings.stream()
                    .map(BaseReadingRecordImpl.class::cast)
                    .map(baseReadingRecord -> Pair.of(baseReadingRecord.getTimeStamp(), baseReadingRecord.getEntry().getValues()))
                    .forEach(pair -> storer.add(getTimeSeries(), pair.getFirst(), pair.getLast()));

            storer.execute();
        }
    }

    public static class ReadingsDeletedEventImpl implements Channel.ReadingsDeletedEvent {
        private ChannelImpl channel;
        private Set<Instant> readingTimes;

        public ReadingsDeletedEventImpl(ChannelImpl channel, Set<Instant> readingTimes) {
            this.channel = channel;
            this.readingTimes = readingTimes;
        }

        @Override
        public Channel getChannel() {
            return channel;
        }

        @Override
        public Set<Instant> getReadingTimeStamps() {
            return readingTimes;
        }

        public long getChannelId() {
            return channel.getId();
        }

        public long getStartMillis() {
            return readingTimes.stream().min(Comparator.naturalOrder()).get().toEpochMilli();
        }

        public long getEndMillis() {
            return readingTimes.stream().max(Comparator.naturalOrder()).get().toEpochMilli();
        }
    }
}
