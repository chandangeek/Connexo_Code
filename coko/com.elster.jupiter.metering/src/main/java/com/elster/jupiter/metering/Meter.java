package com.elster.jupiter.metering;

import java.util.Date;
import java.util.List;

import com.elster.jupiter.metering.readings.MeterReading;

public interface Meter extends EndDevice {
	String TYPE_IDENTIFIER = "M";
	void store(MeterReading reading);
	List<MeterActivation> getMeterActivations();
	MeterActivation activate(Date date);
}
