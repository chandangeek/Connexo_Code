package com.elster.jupiter.metering;

import com.elster.jupiter.nls.TranslationKey;

public enum ServiceKind implements TranslationKey {
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

	@Override
	public String getKey() {
		return getTranslationKey(this);
	}

	public static String getTranslationKey(ServiceKind kind){
		return "service.category." + kind.name().toLowerCase();
	}

	@Override
	public String getDefaultFormat() {
		return displayName;
	}
}
