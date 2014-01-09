package com.elster.jupiter.metering.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.time.Clock;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.util.time.UtcInstant;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class MeterActivationImpl implements MeterActivation {
	//persistent fields
	private long id;
	private long usagePointId;
	private long meterId;
	private Interval interval;
	private long version;
	@SuppressWarnings("unused")
	private UtcInstant createTime;
	@SuppressWarnings("unused")
	private UtcInstant modTime;
	@SuppressWarnings("unused")
	private String userName;
	
	// associations
	private UsagePoint usagePoint;
    private Meter meter;
    private final List<Channel> channels = new ArrayList<>();
    private final DataModel dataModel;
    private final EventService eventService;
    private final Clock clock;
    private final ChannelBuilder channelBuilder;

    @SuppressWarnings("unused")
    @Inject
	MeterActivationImpl(DataModel dataModel, EventService eventService, Clock clock, ChannelBuilder channelBuilder) {
        this.dataModel = dataModel;
        this.eventService = eventService;
        this.clock = clock;
        this.channelBuilder = channelBuilder;
    }
	
	MeterActivationImpl init(Meter meter , UsagePoint usagePoint , Date start ) {
		this.meterId = meter == null ? 0 : meter.getId();
		this.meter = meter;
		this.usagePointId = usagePoint == null ? 0 : usagePoint.getId();
		this.usagePoint = usagePoint;
		this.interval = Interval.startAt(start);
        return this;
	}

    static MeterActivationImpl from(DataModel dataModel, Meter meter , UsagePoint usagePoint , Date start) {
        return dataModel.getInstance(MeterActivationImpl.class).init(meter, usagePoint, start);
    }

	static MeterActivationImpl from(DataModel dataModel, UsagePoint usagePoint, Date at) {
		return from(dataModel, null, usagePoint, at);
	}

	static MeterActivationImpl from(DataModel dataModel, Meter meter, Date at) {
		return from(dataModel, meter, null, at);
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
		if (usagePointId == 0) {
			return Optional.absent();
        }
		if (usagePoint == null) {
			usagePoint = dataModel.mapper(UsagePoint.class).getExisting(usagePointId);
		}			
		return Optional.of(usagePoint);
	}

	@Override
	public Optional<Meter> getMeter() {
		if (meterId == 0) {
			return Optional.absent();
        }
		if (meter == null) {
			meter = (Meter) dataModel.mapper(EndDevice.class).getExisting(meterId);
		}
		return Optional.of(meter);
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
        Channel channel = channelBuilder.meterActivation(this).readingTypes(main, readingTypes).build();
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
        if (!requested.overlaps(requested)) {
            return Collections.emptyList();
        }
        Interval active = requested.intersection(requested);
        for (Channel channel : getChannels()) {
            if (channel.getReadingTypes().contains(readingType)) {
                return channel.getReadings(readingType, active);
            }
        }
        return Collections.emptyList();
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
}
