package com.elster.jupiter.metering;

import com.elster.jupiter.cbo.ElectronicAddress;
import com.elster.jupiter.cbo.IdentifiedObject;
import com.elster.jupiter.metering.events.EndDeviceEventRecord;
import com.elster.jupiter.metering.events.EndDeviceEventType;
import com.elster.jupiter.util.time.Interval;

import java.util.Date;
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
    Date getCreateTime();
    Date getModTime();
    long getVersion();
    void delete();

    EndDeviceEventRecord addEventRecord(EndDeviceEventType type, Date date);

    List<EndDeviceEventRecord> getDeviceEvents(Interval interval);
}
