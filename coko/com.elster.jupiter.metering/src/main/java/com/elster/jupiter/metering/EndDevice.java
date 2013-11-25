package com.elster.jupiter.metering;

import java.util.Date;

import com.elster.jupiter.cbo.ElectronicAddress;
import com.elster.jupiter.cbo.IdentifiedObject;

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
}
