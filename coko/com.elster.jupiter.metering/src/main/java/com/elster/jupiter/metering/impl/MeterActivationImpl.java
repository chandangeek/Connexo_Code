package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.BaseReading;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.util.time.UtcInstant;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import java.util.Collections;
import java.util.Date;
import java.util.List;

public class MeterActivationImpl implements MeterActivation {
	//persistent fields
	private long id;
	private long usagePointId;
	private long meterId;
	private Interval interval;
	@SuppressWarnings("unused")
	private long version;
	@SuppressWarnings("unused")
	private UtcInstant createTime;
	@SuppressWarnings("unused")
	private UtcInstant modTime;
	@SuppressWarnings("unused")
	private String userName;
	
	// associations
	UsagePoint usagePoint;
	Meter meter;
	List<Channel> channels;
	
	@SuppressWarnings("unused")
	private MeterActivationImpl() {	
	}
	
	MeterActivationImpl(UsagePoint usagePoint , Date start , Meter meter) {
		this.usagePointId = usagePoint == null ? 0 : usagePoint.getId();
		this.usagePoint = usagePoint;
		this.meterId = meter == null ? 0 : meter.getId();
		this.meter = meter;
		this.interval = Interval.startAt(start);
	}
	
	public MeterActivationImpl(UsagePoint usagePoint, Date at) {
		this(usagePoint,at,null);
	}

	@Override
	public long getId() {	
		return id;
	}

	@Override
	public Optional<UsagePoint> getUsagePoint() {
		if (usagePointId == 0) {
			return Optional.absent();
        }
		if (usagePoint == null) {
			usagePoint = Bus.getOrmClient().getUsagePointFactory().getExisting(usagePointId);
		}			
		return Optional.of(usagePoint);
	}

	@Override
	public Optional<Meter> getMeter() {
		if (meterId == 0) {
			return Optional.absent();
        }
		if (meter == null) {
			meter = Bus.getOrmClient().getMeterFactory().getExisting(meterId);
		}
		return Optional.of(meter);
	}
	
	@Override
	public List<Channel> getChannels() {
		return ImmutableList.copyOf(doGetChannels());
	}

	private List<Channel> doGetChannels() {
		if (channels == null) {
			channels = Bus.getOrmClient().getChannelFactory().find("meterActivation",this);
		}
		return channels;
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
        return Bus.getChannelBuilder().meterActivation(this)
                .readingTypes(main, readingTypes)
        .build();
	}

	@Override
	public List<ReadingType> getReadingTypes() {
        ImmutableList.Builder<ReadingType> builder = ImmutableList.builder();
		for (Channel channel : doGetChannels()) {
			builder.addAll(channel.getReadingTypes());
		}
		return builder.build();
	}

	@Override
	public List<? extends BaseReading> getReadings(Date from, Date to,ReadingType readingType) {
		Interval requested = new Interval(from, to);
        if (!requested.overlaps(interval)) {
            return Collections.emptyList();
        }
        Interval active = requested.intersection(interval);
        for (Channel channel : getChannels()) {
            if (channel.getReadingTypes().contains(readingType)) {
                return channel.getIntervalReadings(readingType, active.getStart(), active.getEnd());
            }
        }
        return Collections.emptyList();
    }

	@Override
	public boolean isCurrent() {
		return interval.isCurrent(Bus.getClock());
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
            Bus.getOrmClient().getMeterActivationFactory().persist(this);
        } else {
            Bus.getOrmClient().getMeterActivationFactory().update(this);
        }
    }
}
