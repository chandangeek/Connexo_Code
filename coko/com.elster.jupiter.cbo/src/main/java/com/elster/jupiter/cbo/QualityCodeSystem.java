package com.elster.jupiter.cbo;

import java.util.Optional;

public enum QualityCodeSystem {
	NOTAPPLICABLE,
	ENDDEVICE,
	MDC,
	MDM,
	OTHER,
	EXTERNAL;
	
	public static Optional<QualityCodeSystem> get(int ordinal) {
		return Optional.ofNullable(ordinal < values().length ? values()[ordinal] : null);
	}
}
