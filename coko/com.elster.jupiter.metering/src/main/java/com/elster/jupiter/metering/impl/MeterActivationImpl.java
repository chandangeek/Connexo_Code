package com.elster.jupiter.metering.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.EventType;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeterAlreadyLinkedToUsagePoint;
import com.elster.jupiter.metering.ReadingContainer;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.time.Interval;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;

import javax.inject.Inject;
import javax.inject.Provider;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
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

import static com.elster.jupiter.util.Ranges.does;
import static com.elster.jupiter.util.streams.Predicates.not;

public final class MeterActivationImpl implements MeterActivation {
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
    private List<Channel> channels = new ArrayList<>();
    
    private final DataModel dataModel;
    private final EventService eventService;
    private final Clock clock;
    private final Provider<ChannelBuilder> channelBuilder;
    private final Thesaurus thesaurus;

    @Inject
	MeterActivationImpl(DataModel dataModel, EventService eventService, Clock clock, Provider<ChannelBuilder> channelBuilder, Thesaurus thesaurus) {
        this.dataModel = dataModel;
        this.eventService = eventService;
        this.clock = clock;
        this.channelBuilder = channelBuilder;
        this.thesaurus = thesaurus;
    }
	
	MeterActivationImpl init(Meter meter , UsagePoint usagePoint , Instant start ) {
		this.meter.set(meter);
		this.usagePoint.set(usagePoint);
		this.interval = Interval.of(Range.atLeast(start));
        return this;
	}
	
	MeterActivationImpl init(Meter meter , Instant start ) {
        return init(meter,null,start);
	}
	
	MeterActivationImpl init(UsagePoint usagePoint , Instant start ) {
		return init(null,usagePoint,start);
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
	public List<Channel> getChannels() {
        return Collections.unmodifiableList(channels);
    }

	@Override
	public Channel createChannel(ReadingType main, ReadingType... readingTypes) {
		//TODO: check for duplicate channel
		ReadingTypeImpl[] extraTypes = new ReadingTypeImpl[readingTypes.length];
		for (int i = 0 ; i < readingTypes.length ; i++) {
			extraTypes[i] = (ReadingTypeImpl) readingTypes[i];
		}
        Channel channel = channelBuilder.get().meterActivation(this).readingTypes((ReadingTypeImpl) main, extraTypes).build();
        channels.add(channel);
        eventService.postEvent(EventType.CHANNEL_CREATED.topic(), channel);
        return channel;
	}

    @Override
	public List<ReadingType> getReadingTypes() {
        ImmutableList.Builder<ReadingType> builder = ImmutableList.builder();
		for (Channel channel : getChannels()) {
			builder.addAll(channel.getReadingTypes());
		}
		return builder.build();
	}

	@Override
	public List<BaseReadingRecord> getReadings(Range<Instant> requested, ReadingType readingType) {
        if (!requested.isConnected(getInterval().toOpenClosedRange())) {
            return Collections.emptyList();
        }
        Range<Instant> active = requested.intersection(getInterval().toOpenClosedRange());
        Channel channel = getChannel(readingType);
        if (channel == null) {
        	return Collections.emptyList();
        } else {
        	return channel.getReadings(readingType, active);
        }
    }

	@Override
	public List<? extends BaseReadingRecord> getReadingsUpdatedSince(Range<Instant> range, ReadingType readingType, Instant since) {
		if (!range.isConnected(getRange())) {
			return Collections.emptyList();
		}
		Range<Instant> active = range.intersection(getRange());
		if (active.hasUpperBound() && since.isAfter(active.upperEndpoint())) {
			return Collections.emptyList();
		}
		Channel channel = getChannel(readingType);
		if (channel == null) {
			return Collections.emptyList();
		} else {
			return channel.getReadingsUpdatedSince(readingType, active, since);
		}
	}

	@Override
	public List<BaseReadingRecord> getReadingsBefore(Instant when, ReadingType readingType, int count) {
        Channel channel = getChannel(readingType);
        if (channel == null) {
        	return Collections.emptyList();
        } else {
        	return channel.getReadingsBefore(when, count);
        }
    }

	@Override
	public List<BaseReadingRecord> getReadingsOnOrBefore(Instant when, ReadingType readingType, int count) {
        Channel channel = getChannel(readingType);
        if (channel == null) {
        	return Collections.emptyList();
        } else {
        	return channel.getReadingsOnOrBefore(when, count);
        }
    }
	
	@Override
	public Set<ReadingType> getReadingTypes(Range<Instant> requested) {
		if (overlaps(requested)) {
			return new HashSet<>(getReadingTypes());
		} else {
			return Collections.emptySet();
		}
	}

    @Override
    public boolean hasData() {
        return getChannels().stream().anyMatch(Channel::hasData);
    }

    @Override
	public boolean isCurrent() {
		return getRange().contains(clock.instant());
	}
	
    @Override
    public long getVersion() {
        return version;
    }

    @Override
    public void endAt(Instant end) {
        this.interval = Interval.of(Range.closedOpen(getRange().lowerEndpoint(), end));
        save();
    }

    public void save() {
        if (id == 0) {
            dataModel.mapper(MeterActivation.class).persist(this);
        } else {
            dataModel.mapper(MeterActivation.class).update(this);
        }
    }
	
	private Channel getChannel(ReadingType readingType) {
		for (Channel channel : getChannels()) {
			if (channel.getReadingTypes().contains(readingType)) {
				return channel;
			}
		}
		return null;
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
	
	@Override
	public ZoneId getZoneId() {
		Set<ZoneId> candidates = getChannels().stream()
			.map(Channel::getZoneId)
			.collect(Collectors.toSet());
		if (candidates.size() > 1) {
			throw new RuntimeException("More than one zone id for this meter activation");
		}
		return candidates.stream().findFirst().orElse(clock.getZone());
	}

    @Override
    public boolean is(ReadingContainer other) {
        return other instanceof MeterActivation && ((MeterActivation) other).getId() == getId();
    }

    @Override
    public Optional<Meter> getMeter(Instant instant) {
        if (getRange().contains(instant)) {
            return getMeter();
        }
        return Optional.empty();
    }

    @Override
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
        Optional<? extends MeterActivation> overlappingActivation = usagePoint.getMeterActivations().stream()
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
	public List<Instant> toList(ReadingType readingType, Range<Instant> exportInterval) {
		return Optional.ofNullable(getChannel(readingType)).map(channel -> channel.toList(exportInterval)).orElseGet(Collections::emptyList);
	}

    @Override
    public void advanceStartDate(Instant startTime) {
        if (!startTime.isBefore(getRange().lowerEndpoint())) {
            throw new IllegalArgumentException("startDate must be before the current startdate");
        }
        Range<Instant> newRange = Range.singleton(startTime).span(interval.toClosedOpenRange());
        Set<? extends MeterActivation> metersConflicts = getMeter().map(meter -> meter.getMeterActivations().stream()
                        .filter(not(this::equals))
                        .filter(meterActivation -> does(meterActivation.getRange()).overlap(newRange))
                        .collect(Collectors.toSet())
        ).orElseGet(Collections::emptySet);
        Set<? extends MeterActivation> usagePointsConflicts = getUsagePoint().map(meter -> meter.getMeterActivations().stream()
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

        Set<MeterActivation> resolvableConflicts = new HashSet<>(metersConflicts);
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

    private void resolveConflict(MeterActivation toResolve, Instant cutOff) {

        Instant end = toResolve.getEnd();
        toResolve.endAt(cutOff);
        // copy all data since cutoff to this MeterActivation

        Map<Set<ReadingType>, ChannelImpl> sourceChannels = toResolve.getChannels().stream()
                .map(ChannelImpl.class::cast)
                .collect(Collectors.toMap(channel -> ImmutableSet.copyOf(channel.getReadingTypes()), Function.<ChannelImpl>identity()));
        Map<Set<ReadingType>, ChannelImpl> targetChannels = getChannels().stream()
                .map(ChannelImpl.class::cast)
                .collect(Collectors.toMap(channel -> ImmutableSet.copyOf(channel.getReadingTypes()), Function.<ChannelImpl>identity()));

        sourceChannels.entrySet().stream()
                .forEach(entry -> copyData(entry.getValue(), targetChannels.get(entry.getKey()), Range.openClosed(cutOff, end)));

    }

    private void copyData(ChannelImpl sourceChannel, ChannelImpl targetChannel, Range<Instant> range) {
        if (targetChannel == null) {
            throw new IllegalArgumentException("Channel mismatch");
        }
        moveReadings(sourceChannel, targetChannel, range);
        moveReadingQualities(sourceChannel, targetChannel, range);
    }

    private void moveReadingQualities(Channel sourceChannel, ChannelImpl targetChannel, Range<Instant> range) {
        List<ReadingQualityRecord> sourceQualities = sourceChannel.findReadingQuality(range);
        List<ReadingQualityRecord> newReadingQualities = sourceQualities.stream()
                .map(targetChannel::copyReadingQuality)
                .collect(Collectors.toList());
        DataMapper<ReadingQualityRecord> qualityMapper = dataModel.mapper(ReadingQualityRecord.class);
        qualityMapper.persist(newReadingQualities);
        qualityMapper.remove(sourceQualities);
    }

    private void moveReadings(ChannelImpl sourceChannel, ChannelImpl targetChannel, Range<Instant> range) {
        List<BaseReadingRecord> readings = sourceChannel.getReadings(range);
        targetChannel.copyReadings(readings);
        sourceChannel.deleteReadings(readings);
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
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MeterActivationImpl that = (MeterActivationImpl) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

	@Override
	public List<ReadingQualityRecord> getReadingQualities(ReadingQualityType readingQualityType, ReadingType readingType, Range<Instant> interval) {
		return Optional.ofNullable(getChannel(readingType))
				.flatMap(channel -> channel.getCimChannel(readingType))
				.map(cimChannel -> cimChannel.findActualReadingQuality(readingQualityType, interval))
				.orElse(Collections.emptyList());
	}

    @Override
    public List<MeterActivation> getMeterActivations() {
        return Collections.singletonList(this);
    }
}
