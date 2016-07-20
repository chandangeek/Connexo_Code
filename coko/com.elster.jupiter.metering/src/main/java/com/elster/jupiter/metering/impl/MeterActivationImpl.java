package com.elster.jupiter.metering.impl;

import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.EventType;
import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeterAlreadyLinkedToUsagePoint;
import com.elster.jupiter.metering.MeterConfiguration;
import com.elster.jupiter.metering.MultiplierType;
import com.elster.jupiter.metering.MultiplierUsage;
import com.elster.jupiter.metering.ReadingContainer;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointConfiguration;
import com.elster.jupiter.metering.ami.EndDeviceCapabilities;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.streams.Functions;
import com.elster.jupiter.util.time.Interval;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.elster.jupiter.util.Ranges.does;
import static com.elster.jupiter.util.streams.Currying.test;
import static com.elster.jupiter.util.streams.Predicates.not;

public final class MeterActivationImpl implements IMeterActivation {
    //persistent fields
    private long id;
    private Interval interval;
    private long version;
    @SuppressWarnings("unused")
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;
    @SuppressWarnings("unused")
    private String userName;

    // associations
    private Reference<UsagePoint> usagePoint = ValueReference.absent();
    private Reference<Meter> meter = ValueReference.absent();
    private Reference<MeterRole> meterRole = ValueReference.absent();
    private Reference<MeterActivationChannelsContainerImpl> channelsContainer = ValueReference.absent();
    private List<MultiplierValue> multipliers = new ArrayList<>();

    private final DataModel dataModel;
    private final EventService eventService;
    private final Clock clock;
    private final Thesaurus thesaurus;

    @Inject
    MeterActivationImpl(DataModel dataModel, EventService eventService, Clock clock, Thesaurus thesaurus) {
        this.dataModel = dataModel;
        this.eventService = eventService;
        this.clock = clock;
        this.thesaurus = thesaurus;
    }

    MeterActivationImpl init(Meter meter, Instant from) {
        return init(meter, null, null, from);
    }

    MeterActivationImpl init(Meter meter, Range<Instant> range) {
        return init(meter, null, null, range);
    }

    MeterActivationImpl init(UsagePoint usagePoint, Instant from) {
        return init(null, null, usagePoint, from);
    }

    public MeterActivationImpl init(Meter meter, MeterRole role, UsagePoint usagePoint, Instant from) {
        return init(meter, role, usagePoint, Range.atLeast(from));
    }

    public MeterActivationImpl init(Meter meter, MeterRole meterRole, UsagePoint usagePoint, Range<Instant> range) {
        if (usagePoint != null && meter != meterRole && (meter == null || meterRole == null)) {
            throw new IllegalArgumentException("You are trying to activate meter on usage point, but you didn't specified a meter role" +
                    " or you specified a meter role, but forgot about meter.");
        }
        this.meter.set(meter);
        this.meterRole.set(meterRole);
        this.usagePoint.set(usagePoint);
        this.interval = Interval.of(range);
        this.channelsContainer.set(this.dataModel.getInstance(MeterActivationChannelsContainerImpl.class).init(this));
        if (meter != null) {
            meter.getHeadEndInterface()
                    .map(headEndInterface -> headEndInterface.getCapabilities(meter))
                    .map(EndDeviceCapabilities::getConfiguredReadingTypes)
                    .ifPresent(readingTypeList -> createChannels(readingTypeList));
        }
        return this;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public Interval getInterval() {
        return interval;
    }

    @Override
    public Optional<UsagePoint> getUsagePoint() {
        return usagePoint.getOptional();
    }

    @Override
    public Optional<Meter> getMeter() {
        return meter.getOptional();
    }

    @Override
    public Optional<MeterRole> getMeterRole() {
        return this.meterRole.getOptional();
    }

    private void createChannels(List<ReadingType> readingTypes) {
        Stream<MultiplierUsage> meterMultipliers = getMeter()
                .flatMap(meter -> meter.getConfiguration(getStart()))
                .map(MeterConfiguration::getReadingTypeConfigs)
                .orElse(Collections.emptyList())
                .stream()
                .map(MultiplierUsage.class::cast);
        Stream<MultiplierUsage> usagePointMultipliers = getUsagePoint()
                .flatMap(usagePoint -> usagePoint.getConfiguration(getStart()))
                .map(UsagePointConfiguration::getReadingTypeConfigs)
                .orElse(Collections.emptyList())
                .stream()
                .map(MultiplierUsage.class::cast);
        Set<ReadingType> calculatedReadingTypes = Stream.concat(meterMultipliers, usagePointMultipliers).map(MultiplierUsage::getCalculated).flatMap(Functions.asStream()).collect(Collectors.toSet());

        List<ReadingType> collect = readingTypes.stream()
                .filter(not(calculatedReadingTypes::contains))
                .filter(not(readingType -> isDeltaDeltaOfOther(readingType, readingTypes)))
                .filter(not(readingType -> isDeltaDeltaOfOther(readingType, calculatedReadingTypes))) // if the calculated reading type is a bulk, automatically the deltadelta will be added
                .distinct().collect(Collectors.toList());
        collect.forEach(this.getChannelsContainer()::createChannel);
    }

    private boolean isDeltaDeltaOfOther(ReadingType readingType, Collection<ReadingType> readingTypes) {
        return readingTypes.stream().anyMatch(readingType::isBulkQuantityReadingType);
    }

    @Override
    public List<ReadingType> getReadingTypes() {
        ImmutableList.Builder<ReadingType> builder = ImmutableList.builder();
        for (Channel channel : getChannelsContainer().getChannels()) {
            builder.addAll(channel.getReadingTypes());
        }
        return builder.build();
    }

    @Override
    public boolean isCurrent() {
        return getRange().contains(this.clock.instant());
    }

    @Override
    public long getVersion() {
        return version;
    }

    @Override
    public void endAt(Instant end) {
        if (isInvalidEndDate(end)) {
            throw new ChannelDataPresentException();
        }
        doEndAt(end);
    }

    private boolean isInvalidEndDate(Instant end) {
        return getChannelsContainer().getChannels().stream()
                .map(Channel::getLastDateTime)
                .filter(Objects::nonNull)
                .anyMatch(test(Instant::isAfter).with(end));
    }

    @Override
    public void doEndAt(Instant end) {
        this.interval = Interval.of(Range.closedOpen(getRange().lowerEndpoint(), end));
        save();
    }

    public void save() {
        if (id == 0) {
            this.dataModel.persist(this);
        } else {
            this.dataModel.update(this);
        }
    }

    private DataMapper<MeterActivation> getDataMapper() {
        return dataModel.mapper(MeterActivation.class);
    }

    @Override
    public Instant getStart() {
        return getRange().lowerEndpoint();
    }

    @Override
    public Instant getEnd() {
        Range<Instant> range = getRange();
        return range.hasUpperBound() ? range.upperEndpoint() : null;
    }

    public Optional<Meter> getMeter(Instant instant) {
        if (getRange().contains(instant)) {
            return getMeter();
        }
        return Optional.empty();
    }

    public Optional<UsagePoint> getUsagePoint(Instant instant) {
        if (getRange().contains(instant)) {
            return getUsagePoint();
        }
        return Optional.empty();
    }

    void setUsagePoint(UsagePoint usagePoint) {
        if (this.usagePoint.isPresent()) {
            throw new MeterAlreadyLinkedToUsagePoint(thesaurus, this);
        }
        Optional<MeterActivation> overlappingActivation = usagePoint.getMeterActivations().stream()
                .filter(overlaps())
                .findAny();
        overlappingActivation.ifPresent(activation -> {
            throw new RuntimeException("UsagePoint is already active at " + activation.getRange());
        });
        this.usagePoint.set(usagePoint);
        this.save();
    }

    private Predicate<MeterActivation> overlaps() {
        return exists -> exists.getRange().isConnected(getRange()) && !exists.getRange().intersection(getRange()).isEmpty();
    }

    void setMeter(Meter meter) {
        if (this.meter.isPresent()) {
            throw new RuntimeException("MeterActivation is already linked with usagepoint");
        }
        Optional<? extends MeterActivation> overlappingActivation = meter.getMeterActivations().stream()
                .filter(overlaps())
                .findAny();
        overlappingActivation.ifPresent(activation -> {
            throw new RuntimeException("UsagePoint is already active at " + activation.getRange());
        });
        this.meter.set(meter);
        this.save();
    }

    @Override
    public void advanceStartDate(Instant startTime) {
        if (!startTime.isBefore(getRange().lowerEndpoint())) {
            throw new IllegalArgumentException("startDate must be before the current startdate");
        }
        Range<Instant> newRange = Range.singleton(startTime).span(interval.toClosedOpenRange());
        Set<IMeterActivation> metersConflicts = getMeter().map(meter -> meter.getMeterActivations().stream()
                .map(IMeterActivation.class::cast)
                .filter(not(this::equals))
                .filter(meterActivation -> does(meterActivation.getRange()).overlap(newRange))
                .collect(Collectors.toSet())
        ).orElseGet(Collections::emptySet);
        Set<IMeterActivation> usagePointsConflicts = getUsagePoint().map(meter -> meter.getMeterActivations().stream()
                .map(IMeterActivation.class::cast)
                .filter(not(this::equals))
                .filter(meterActivation -> does(meterActivation.getRange()).overlap(newRange))
                .collect(Collectors.toSet())
        ).orElseGet(Collections::emptySet);

        boolean unresolvableMeterConflict = usagePointsConflicts.stream()
                .anyMatch(this::hasDifferentMeter);
        if (unresolvableMeterConflict) {
            throw new IllegalArgumentException("resulting MeterActivation would overlap with the previous MeterActivation of its UsagePoint.");
        }
        boolean unresolvableUsagePointConflict = metersConflicts.stream()
                .anyMatch(this::hasDifferentUsagePoint);
        if (unresolvableUsagePointConflict) {
            throw new IllegalArgumentException("resulting MeterActivation would overlap with the previous MeterActivation of its Meter.");
        }

        Set<IMeterActivation> resolvableConflicts = new HashSet<>(metersConflicts);
        resolvableConflicts.addAll(usagePointsConflicts);

        if (resolvableConflicts.size() > 1) {
            throw new IllegalArgumentException("resulting MeterActivation would overlap with more than one preceding MeterActivations");
        }

        this.interval = Interval.of(newRange);
        resolvableConflicts.stream()
                .findFirst()
                .ifPresent(conflict -> this.resolveConflict(conflict, startTime));

        this.save();
        eventService.postEvent(EventType.METER_ACTIVATION_ADVANCED.topic(), new EventType.MeterActivationAdvancedEvent(this, resolvableConflicts.stream().findFirst().orElse(null)));
    }

    private void resolveConflict(IMeterActivation toResolve, Instant cutOff) {
        Instant end = toResolve.getEnd();
        toResolve.doEndAt(cutOff);
        // copy all data since cutoff to this MeterActivation
        moveAllChannelsData(toResolve, end != null ? Range.openClosed(cutOff, end) : Range.greaterThan(cutOff));
    }

    void moveAllChannelsData(MeterActivation source, Range<Instant> range) {
        Map<Set<ReadingType>, ChannelImpl> sourceChannels = source.getChannelsContainer().getChannels().stream()
                .map(ChannelImpl.class::cast)
                .collect(Collectors.toMap(channel -> ImmutableSet.copyOf(channel.getReadingTypes()), Function.<ChannelImpl>identity()));
        Map<Set<ReadingType>, ChannelImpl> targetChannels = getChannelsContainer().getChannels().stream()
                .map(ChannelImpl.class::cast)
                .collect(Collectors.toMap(channel -> ImmutableSet.copyOf(channel.getReadingTypes()), Function.<ChannelImpl>identity()));

        sourceChannels.entrySet().stream()
                .forEach(entry -> moveSingleChannelData(entry.getValue(), targetChannels.get(entry.getKey()), range));
    }

    private void moveSingleChannelData(ChannelImpl sourceChannel, ChannelImpl targetChannel, Range<Instant> range) {
        if (targetChannel == null) {
            throw new IllegalArgumentException("Channel mismatch");
        }
        moveReadings(sourceChannel, targetChannel, range);
        moveReadingQualities(sourceChannel, targetChannel, range);
    }

    private void moveReadingQualities(Channel sourceChannel, ChannelImpl targetChannel, Range<Instant> range) {
        List<ReadingQualityRecord> sourceQualities = sourceChannel.findReadingQualities().inTimeInterval(range).collect();
        sourceQualities.forEach(targetChannel::copyReadingQuality);
        dataModel.mapper(ReadingQualityRecord.class).remove(sourceQualities);
    }

    private void moveReadings(ChannelImpl sourceChannel, ChannelImpl targetChannel, Range<Instant> range) {
        List<BaseReadingRecord> readings = sourceChannel.getReadings(range);
        targetChannel.copyReadings(readings);
        sourceChannel.deleteReadings(readings);
    }

    void detachUsagePoint() {
        this.usagePoint.setNull();
        this.meterRole.setNull();
        save();
    }
    private boolean hasDifferentMeter(MeterActivation other) {
        return getMeter()
                .map(meter -> other.getMeter()
                        .map(otherMeter -> otherMeter.getId() != meter.getId())
                        .orElse(false))
                .orElse(false);
    }

    private boolean hasDifferentUsagePoint(MeterActivation other) {
        return getUsagePoint()
                .map(usagePoint -> other.getUsagePoint()
                        .map(otherUsagePoint -> otherUsagePoint.getId() != usagePoint.getId())
                        .orElse(false))
                .orElse(false);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MeterActivationImpl that = (MeterActivationImpl) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public ChannelsContainer getChannelsContainer() {
        return this.channelsContainer.get();
    }

    @Override
    public void setMultiplier(MultiplierType type, BigDecimal value) {
        getDataMapper().lockObjectIfVersion(version, getId());
        multipliers.stream()
                .filter(multiplierValue -> multiplierValue.getType().equals(type))
                .findFirst()
                .map(multiplierValue -> {
                    multiplierValue.setValue(value);
                    dataModel.touch(this);
                    return multiplierValue;
                })
                .orElseGet(() -> {
                    MultiplierValueImpl newMultiplier = MultiplierValueImpl.from(dataModel, this, type, value);
                    multipliers.add(newMultiplier);
                    dataModel.touch(this);
                    return newMultiplier;
                });
    }

    @Override
    public void removeMultiplier(MultiplierType type) {
        multipliers.stream()
                .filter(multiplierValue -> multiplierValue.getType().equals(type))
                .findFirst()
                .ifPresent(this::doRemoveMultiplier);
    }

    private void doRemoveMultiplier(MultiplierValue multiplierValue) {
        getDataMapper().lockObjectIfVersion(version, getId());
        multipliers.remove(multiplierValue);
        dataModel.touch(this);
    }

    @Override
    public Map<MultiplierType, BigDecimal> getMultipliers() {
        return multipliers.stream()
                .collect(Collectors.toMap(MultiplierValue::getType, MultiplierValue::getValue));
    }

    @Override
    public Optional<BigDecimal> getMultiplier(MultiplierType type) {
        return multipliers.stream()
                .filter(multiplierValue -> multiplierValue.getType().equals(type))
                .map(MultiplierValue::getValue)
                .findFirst();
    }

    class ChannelDataPresentException extends LocalizedException {
        public ChannelDataPresentException() {
            super(thesaurus, MessageSeeds.CHANNEL_DATA_PRESENT);
        }
    }

    @Override
    public Set<ReadingType> getReadingTypes(Range<Instant> range) {
        return getChannelsContainer().getReadingTypes(range);
    }

    @Override
    public List<? extends BaseReadingRecord> getReadings(Range<Instant> range, ReadingType readingType) {
        return getChannelsContainer().getReadings(range, readingType);
    }

    @Override
    public List<? extends BaseReadingRecord> getReadingsUpdatedSince(Range<Instant> range, ReadingType readingType, Instant since) {
        return getChannelsContainer().getReadingsUpdatedSince(range, readingType, since);
    }

    @Override
    public List<? extends BaseReadingRecord> getReadingsBefore(Instant when, ReadingType readingType, int count) {
        return getChannelsContainer().getReadingsBefore(when, readingType, count);
    }

    @Override
    public List<? extends BaseReadingRecord> getReadingsOnOrBefore(Instant when, ReadingType readingType, int count) {
        return getChannelsContainer().getReadingsOnOrBefore(when, readingType, count);
    }

    @Override
    public boolean hasData() {
        return getChannelsContainer().hasData();
    }

    @Override
    public boolean is(ReadingContainer other) {
        return getChannelsContainer().is(other);
    }

    @Override
    public ZoneId getZoneId() {
        return getChannelsContainer().getZoneId();
    }

    @Override
    public List<Instant> toList(ReadingType readingType, Range<Instant> exportInterval) {
        return getChannelsContainer().toList(readingType, exportInterval);
    }

    @Override
    public List<ReadingQualityRecord> getReadingQualities(Set<QualityCodeSystem> qualityCodeSystems, QualityCodeIndex qualityCodeIndex, ReadingType readingType, Range<Instant> interval) {
        return getChannelsContainer().getReadingQualities(qualityCodeSystems, qualityCodeIndex, readingType, interval);
    }

    @Override
    public List<ChannelsContainer> getChannelsContainers() {
        return Collections.singletonList(getChannelsContainer());
    }
}
