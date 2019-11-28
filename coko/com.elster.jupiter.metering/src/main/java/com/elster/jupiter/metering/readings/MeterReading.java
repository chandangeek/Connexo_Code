/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.readings;

import com.elster.jupiter.metering.readings.beans.EndDeviceEventImpl;
import com.elster.jupiter.metering.readings.beans.IntervalBlockImpl;
import com.elster.jupiter.metering.readings.beans.ReadingImpl;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
@JsonTypeInfo(
		use = JsonTypeInfo.Id.CLASS,
		include = JsonTypeInfo.As.PROPERTY,
		property = "type")
public interface MeterReading {
	@XmlElement(type = ReadingImpl.class)
	List<Reading> getReadings();
	@XmlElement(type = IntervalBlockImpl.class)
	List<IntervalBlock> getIntervalBlocks();
	@XmlElement(type = EndDeviceEventImpl.class)
	List<EndDeviceEvent> getEvents();
}
