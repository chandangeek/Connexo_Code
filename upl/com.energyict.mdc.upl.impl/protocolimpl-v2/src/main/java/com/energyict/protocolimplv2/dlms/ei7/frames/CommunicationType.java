package com.energyict.protocolimplv2.dlms.ei7.frames;

public enum CommunicationType
{
	GPRS("GPRS"),
	NB_IoT("NB-IoT");

	private final String name;

	private CommunicationType(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}
}
