package com.elster.jupiter.metering.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.time.Clock;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.util.time.UtcInstant;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MeterActivationImpl implements MeterActivation {
	//persistent fields
	private long id;
	private Interval interval;
	private long version;
	@SuppressWarnings("unused")
	private UtcInstant createTime;
	@SuppressWarnings("unused")
	private UtcInstant modTime;
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
	
	MeterActivationImpl init(Meter meter , UsagePoint usagePoint , Date start ) {
		this.meter.set(meter);
		this.usagePoint.set(usagePoint);
		this.interval = Interval.startAt(start);
        return this;
	}
	
	MeterActivationImpl init(Meter meter , Date start ) {
        return init(meter,null,start);
	}
	
	MeterActivationImpl init(UsagePoint usagePoint , Date start ) {
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
	public Date getStart() {
		return interval.getStart();
	}

	@Override
	public Date getEnd() {
		return interval.getEnd();
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
	public List<BaseReadingRecord> getReadings(Interval requested, ReadingType readingType) {
        if (!requested.overlaps(interval)) {
            return Collections.emptyList();
        }
        Interval active = requested.intersection(interval);
        Channel channel = getChannel(readingType);
        if (channel == null) {
        	return Collections.emptyList();
        } else {
        	return channel.getReadings(readingType, active);
        }
    }

	@Override
	public List<BaseReadingRecord> getReadingsBefore(Date when, ReadingType readingType, int count) {
        Channel channel = getChannel(readingType);
        if (channel == null) {
        	return Collections.emptyList();
        } else {
        	return channel.getReadingsBefore(when, count);
        }
    }

	@Override
	public List<BaseReadingRecord> getReadingsOnOrBefore(Date when, ReadingType readingType, int count) {
        Channel channel = getChannel(readingType);
        if (channel == null) {
        	return Collections.emptyList();
        } else {
        	return channel.getReadingsOnOrBefore(when, count);
        }
    }
	
	@Override
	public Set<ReadingType> getReadingTypes(Interval requested) {
		if (requested.overlaps(interval)) {
			return new HashSet<>(getReadingTypes());
		} else {
			return Collections.emptySet();
		}
	}

    @Override
    public boolean hasData() {
        for (Channel channel : getChannels()) {
            if (channel.getTimeSeries().getFirstDateTime() != null || channel.getTimeSeries().getLastDateTime() != null) {
                return true;
            }
        }
        return false;
    }

    @Override
	public boolean isCurrent() {
		return interval.isCurrent(clock);
	}
	
	public boolean isEffective(Date when) {
		return interval.isEffective(when);
	}

    @Override
    public long getVersion() {
        return version;
    }

    @Override
    public void endAt(Date end) {
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

	@Override
	public boolean overlaps(Interval interval) {
		return this.getInterval().overlaps(interval);
	}
	
	private Channel getChannel(ReadingType readingType) {
		for (Channel channel : getChannels()) {
			if (channel.getReadingTypes().contains(readingType)) {
				return channel;
			}
		}
		return null;
	}
	
}
