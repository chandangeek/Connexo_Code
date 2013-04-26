package com.elster.jupiter.metering.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.elster.jupiter.metering.BaseReading;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.plumbing.Bus;
import com.elster.jupiter.time.Interval;
import com.elster.jupiter.time.UtcInstant;

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
	
	MeterActivationImpl(UsagePointImpl usagePoint , Date at , Meter meter) {
		this.usagePointId = usagePoint == null ? 0 : usagePoint.getId();
		this.usagePoint = usagePoint;
		this.meterId = meter == null ? 0 : meter.getId();
		this.meter = meter;
		this.interval = new Interval(at);
	}
	
	public MeterActivationImpl(UsagePointImpl usagePoint, Date at) {
		this(usagePoint,at,null);
	}

	@Override
	public long getId() {	
		return id;
	}

	@Override
	public UsagePoint getUsagePoint() {
		if (usagePointId == 0)
			return null;
		if (usagePoint == null) {
			usagePoint = Bus.getOrmClient().getUsagePointFactory().get(usagePointId);
		}			
		return usagePoint;
	}

	@Override
	public Meter getMeter() {
		if (meterId == 0) 
			return null;
		if (meter == null) {
			meter = Bus.getOrmClient().getMeterFactory().get(meterId);
		}
		return meter;
	}
	
	@Override
	public List<Channel> getChannels() {
		return getChannels(true);
	}

	private List<Channel> getChannels(boolean protect) {
		if (channels == null) {
			channels = Bus.getOrmClient().getChannelFactory().find("meterActivation",this);
		}
		return protect ? Collections.unmodifiableList(channels) : channels;
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
	public Channel createChannel(ReadingType ...readingTypes) {
		ChannelImpl channel = new ChannelImpl(this);
		channel.init(readingTypes);
		return channel;
	}

	@Override
	public List<ReadingType> getReadingTypes() {
		List<ReadingType> result = new ArrayList<>();
		for (Channel channel : getChannels(false)) {
			result.addAll(channel.getReadingTypes());
		}
		return result;
	}

	@Override
	public List<BaseReading> getReadings(Date from, Date to,ReadingType readingType) {
		//TODO
		return null;
	}

	@Override
	public boolean isCurrent() {
		return interval.isCurrent();
	}

}
