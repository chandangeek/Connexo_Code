/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;

public enum AmiBillingReadyKind implements TranslationKey {
	UNKNOWN("unknown", "Unknown"),
	ENABLED("enabled", "Enabled"),
	OPERABLE("operable", "Operable"),
	BILLINGAPPROVED("billingApproved", "Billing approved"),
	NONAMI("nonAmi", "Non AMI"),
	AMIDISABLED("amiDisabled", "AMI disabled"),
	AMICAPABLE("amiCapable", "AMI capable"),
	NONMETERED("nonMetered", "Non metered");

	private final String value;
	private final String defaultFormat;

	AmiBillingReadyKind(String value, String defaultFormat) {
		this.value = value;
		this.defaultFormat = defaultFormat;
	}

	public static AmiBillingReadyKind get(int id) {
		return values()[id-1];
	}

	public int getId() {
		return ordinal() + 1;
	}

	public String getValue() {
		return value;
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
		return this.value;
	}

	@Override
	public String getDefaultFormat() {
		return this.defaultFormat;
	}
}
