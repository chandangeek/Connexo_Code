package com.elster.jupiter.metering;

import com.elster.jupiter.cbo.ElectronicAddress;
import com.elster.jupiter.util.HasName;
import com.elster.jupiter.util.time.UtcInstant;

public interface Meter extends HasName {
	long getId();
	String getMRID();
	String getAliasName();
	String getDescription();
	String getSerialNumber();
	String getUtcNumber();
	ElectronicAddress getElectronicAddress();
	AmrSystem getAmrSystem();
	
	void save();

    UtcInstant getCreateTime();

    UtcInstant getModTime();

    long getVersion();
}
