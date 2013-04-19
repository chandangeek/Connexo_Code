package com.elster.jupiter.metering;

public enum ServiceKind {
	ELECTRICITY ("electricity"),
	GAS ("gas"),
	WATER ("water"),
	TIME ("time"),
	HEAT ("heat"),
	REFUSE ("refuse"),
	SEWERAGE ("sewerage"),
	RATES ("rates"),
	TVLICENSE ("tvLicense"),
	INTERNET ("internet"),
	OTHER ("other");
	
	private final String displayName;
	
	ServiceKind(String displayName) {
		this.displayName = displayName;
	}
	
	public String getDisplayName() {
		return displayName;
	}
	
	@Override
	public String toString() {
		return displayName;
	}
}
