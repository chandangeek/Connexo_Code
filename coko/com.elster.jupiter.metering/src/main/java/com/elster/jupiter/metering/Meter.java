package com.elster.jupiter.metering;

import com.elster.jupiter.cbo.ElectronicAddress;

public interface Meter {
	long getId();
	String getMRID();
	String getName();
	String getAliasName();
	String getDescription();
	String getSerialNumber();
	String getUtcNumber();
	ElectronicAddress getElectronicAddress();
	AmrSystem getAmrSystem();
	
	void save();
}
