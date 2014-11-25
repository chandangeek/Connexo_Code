package com.elster.jupiter.metering.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingContainer;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.time.Interval;
import com.google.common.collect.ImmutableList;
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
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class MeterActivationImpl implements MeterActivation {
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

    @Inject
	MeterActivationImpl(DataModel dataModel, EventService eventService, Clock clock, Provider<ChannelBuilder> channelBuilder) {
        this.dataModel = dataModel;
        this.eventService = eventService;
        this.clock = clock;
        this.channelBuilder = channelBuilder;
    }
	
	MeterActivationImpl init(Meter meter , UsagePoint usagePoint , Instant start ) {
		this.meter.set(meter);
		this.usagePoint.set(usagePoint);
		this.interval = Interval.startAt(start);
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
        if (!requested.isConnected(getRange())) {
            return Collections.emptyList();
        }
        Range<Instant> active = requested.intersection(getRange());
        Channel channel = getChannel(readingType);
        if (channel == null) {
        	return Collections.emptyList();
        } else {
        	return channel.getReadings(readingType, active);
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
		return interval.isCurrent(clock);
	}
	
    @Override
    public long getVersion() {
        return version;
    }

    @Override
    public void endAt(Instant end) {
        this.interval = interval.withEnd(end);
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
		return interval.getStart();
	}
	
	@Override
	public Instant getEnd() {
		return interval.getEnd();
	}
	
	@Override
	public ZoneId getZoneId() {
		Set<ZoneId> candidates = getChannels().stream()
			.map(ChannelImpl.class::cast)
			.map(ChannelImpl::getZoneId)
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
}
