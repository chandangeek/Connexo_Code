package com.elster.jupiter.metering;

import com.elster.jupiter.cbo.ElectronicAddress;
import com.elster.jupiter.cbo.IdentifiedObject;
import com.elster.jupiter.metering.events.EndDeviceEventRecord;
import com.elster.jupiter.metering.events.EndDeviceEventType;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.List;

public interface EndDevice extends IdentifiedObject {
	String TYPE_IDENTIFIER = "E";
	long getId();
	String getSerialNumber();
	String getUtcNumber();
	ElectronicAddress getElectronicAddress();
	AmrSystem getAmrSystem();	
	String getAmrId();
	void save();
    Instant getCreateTime();
    Instant getModTime();
    long getVersion();
    void delete();

    EndDeviceEventRecord addEventRecord(EndDeviceEventType type, Instant instant);

    List<EndDeviceEventRecord> getDeviceEvents(Range<Instant> range);
    List<EndDeviceEventRecord> getDeviceEvents(Range<Instant> range, List<EndDeviceEventType> eventTypes);
    List<EndDeviceEventRecord> getDeviceEventsByFilter(EndDeviceEventRecordFilterSpecification filter);

    void setSerialNumber(String serialNumber);

    void setName(String name);
}
