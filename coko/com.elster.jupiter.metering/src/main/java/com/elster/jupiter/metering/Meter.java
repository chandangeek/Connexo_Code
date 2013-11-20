package com.elster.jupiter.metering;

import java.util.List;

import com.elster.jupiter.cbo.ElectronicAddress;
import com.elster.jupiter.cbo.IdentifiedObject;
import com.elster.jupiter.metering.readings.MeterReading;
import com.elster.jupiter.util.time.UtcInstant;

public interface Meter extends IdentifiedObject {
	long getId();
	String getSerialNumber();
	String getUtcNumber();
	ElectronicAddress getElectronicAddress();
	AmrSystem getAmrSystem();
	
	void save();

    UtcInstant getCreateTime();

    UtcInstant getModTime();

    long getVersion();

    void delete();
    
    void store(MeterReading reading);
	List<MeterActivation> getMeterActivations();
}
