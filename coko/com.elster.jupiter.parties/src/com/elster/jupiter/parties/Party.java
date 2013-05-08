package com.elster.jupiter.parties;

import com.elster.jupiter.cbo.ElectronicAddress;

public interface Party {
	long getId();
	String getMRID();
	String getName();
	String getAliasName();
	ElectronicAddress getElectronicAddress();	
}
