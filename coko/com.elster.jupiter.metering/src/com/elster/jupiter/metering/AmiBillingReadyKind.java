package com.elster.jupiter.metering;

public enum AmiBillingReadyKind {
	ENABLED ("enabled"),
	OPERABLE ("operable"),
	BILLINGAPPROVED ("billingApproved"),
	NONAMI ("nonAmi"),
	AMIDISABLED ("amiDisabled"),
	AMICAPABLE ("amiCapable"),
	NONMETERED ("nonMetered");
	
	private final String value;
	
	private AmiBillingReadyKind(String value) {
		this.value = value;
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
	
	@Override
	public String toString() {
		return value;
	}

}
