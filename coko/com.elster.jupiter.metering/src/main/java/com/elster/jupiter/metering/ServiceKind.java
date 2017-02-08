/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;

public enum ServiceKind implements TranslationKey {
	ELECTRICITY ("electricity", "Electricity"),
	GAS ("gas", "Gas"),
	WATER ("water", "Water"),
	TIME ("time", "Time"),
	HEAT ("heat", "Heat"),
	REFUSE ("refuse", "Refuse"),
	SEWERAGE ("sewerage", "Sewerage"),
	RATES ("rates", "Rates"),
	TVLICENSE ("tvLicense", "TV license"),
	INTERNET ("internet", "Internet"),
	OTHER ("other", "Other");

	private final String displayName;
	private final String defaultFormat;

	ServiceKind(String displayName, String defaultFormat) {
		this.displayName = displayName;
		this.defaultFormat = defaultFormat;
	}

	public String getDisplayName() {
		return this.displayName;
	}

	public String getDisplayName(Thesaurus thesaurus) {
		return thesaurus.getFormat(this).format();
	}

	@Override
	public String toString() {
		return defaultFormat;
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
		return this.defaultFormat;
	}
}