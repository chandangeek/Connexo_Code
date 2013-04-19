package com.elster.jupiter.metering;

import java.util.Date;
import java.util.List;

public interface MeterActivation {
	long getId();
	UsagePoint getUsagePoint();
	Meter getMeter();
	Date getFrom();
	Date getTo();
	Channel createChannel(ReadingType... readingTypes);
	List<Channel> getChannels();
	List<ReadingType> getReadingTypes();
	List<BaseReading> getReadings(Date from , Date to, ReadingType readingType);
}
