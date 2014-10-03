package com.elster.jupiter.cbo;

import java.util.Optional;

public enum QualityCodeCategory {
	VALID,
	DIAGNOSTICS,
	POWERQUALITY,
	TAMPER,
	DATACOLLECTION,
	REASONABILITY,
	VALIDATION,
	EDITED,
	ESTIMATED,
	OBSOLETE_OSCILLATORY,
	QUESTIONABLE,
	DERIVED,
	PROJECTED;
	
	public Optional<QualityCodeIndex> qualityCodeIndex(int index) {
		return QualityCodeIndex.get(this, index);
	}
	
	public static Optional<QualityCodeCategory> get(int ordinal) {
		return Optional.ofNullable(ordinal < values().length ? values()[ordinal] : null);
	}
}
