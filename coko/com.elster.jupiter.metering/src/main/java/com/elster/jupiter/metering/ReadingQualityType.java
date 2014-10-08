package com.elster.jupiter.metering;

import java.util.Objects;
import java.util.Optional;

import com.elster.jupiter.cbo.QualityCodeCategory;
import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.google.common.base.Joiner;

public final class ReadingQualityType {

    public static final String MDM_VALIDATED_OK_CODE = "3.0.1";

    private final String code;

    public ReadingQualityType(String code) {
        this.code = Objects.requireNonNull(code);
    }

    public static ReadingQualityType of(QualityCodeSystem system , QualityCodeIndex codeIndex) {
    	return new ReadingQualityType(Joiner.on(".").join(system.ordinal(), codeIndex.category().ordinal(), codeIndex.index()));
    }
    
    public static ReadingQualityType of(QualityCodeSystem system , QualityCodeCategory category, int index) {
    	return new ReadingQualityType(Joiner.on(".").join(system.ordinal(), category.ordinal(), index));
    }
    
    public String getCode() {
        return code;
    }
    
    private Optional<Integer> getCode(int index) {
    	String[] parts = code.split("\\.");
    	if (parts.length < index) {
    		return Optional.empty();
    	}
    	try {
    		return Optional.of(Integer.parseInt(parts[index-1]));
    	} catch (NumberFormatException ex) {
    		return Optional.empty();
    	}
    }
    
    Optional<QualityCodeSystem> system() {
    	return getCode(1).flatMap(QualityCodeSystem::get);
    }
    
    Optional<QualityCodeCategory> category() {
    	return getCode(2).flatMap(QualityCodeCategory::get);
    }
    
    Optional<QualityCodeIndex> qualityIndex()  {
    	return category().flatMap(category -> getCode(3).flatMap(category::qualityCodeIndex));
    }

    public static ReadingQualityType defaultCodeForRuleId(long id) {
        return ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeCategory.VALIDATION, (int) id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        return code.equals(((ReadingQualityType) o).code);

    }

    @Override
    public int hashCode() {
        return code.hashCode();
    }
    
    @Override
    public String toString() {
    	return "Quality Reading Type " + code;
    }
}
