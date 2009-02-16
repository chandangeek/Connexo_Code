package com.energyict.protocolimpl.dlms.eictz3;

public class LogbookEntry {

	int eiCode;
	int ntaCode;
	String description;
	
	public LogbookEntry(int ntaCode, String description, int eiCode) {
		super();
		this.eiCode = eiCode;
		this.ntaCode = ntaCode;
		this.description = description;
	}

	public int getEiCode() {
		return eiCode;
	}

	public int getNtaCode() {
		return ntaCode;
	}

	public String getDescription() {
		return description;
	}
}
