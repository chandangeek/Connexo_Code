package com.elster.jupiter.validation;

import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.metering.readings.ReadingQuality;

import java.util.Collection;
import java.util.EnumSet;

public enum ValidationResult {

    VALID, SUSPECT, NOT_VALIDATED;

    public static ValidationResult getValidationResult(Collection<? extends ReadingQuality> qualities) {
    	EnumSet<ValidationResult> results = qualities.stream()
    		.map(ValidationResult::of)
    		.collect(() -> EnumSet.noneOf(ValidationResult.class), EnumSet::add , EnumSet::addAll);
    	if (results.contains(SUSPECT)) {
    		return SUSPECT;
    	} else if (results.contains(VALID)) {
    		return VALID;
    	} else {
    		return NOT_VALIDATED;
    	}
    }
    
    private static ValidationResult of(ReadingQuality quality) {
    	QualityCodeIndex qualityIndex = quality.getType().qualityIndex().orElse(null);
    	if (EnumSet.of(QualityCodeIndex.SUSPECT, QualityCodeIndex.KNOWNMISSINGREAD, QualityCodeIndex.ERRORCODE).contains(qualityIndex)) {    	
    		return SUSPECT;
    	} else if (QualityCodeIndex.VALIDATED == qualityIndex) {
    		return VALID;
    	} else {
    		return NOT_VALIDATED;
    	}    	
    }
}
 
