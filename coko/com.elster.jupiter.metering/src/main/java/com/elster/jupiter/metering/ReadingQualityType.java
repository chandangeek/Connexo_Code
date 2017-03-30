/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering;

import com.elster.jupiter.cbo.QualityCodeCategory;
import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;

import com.google.common.base.Joiner;

import java.util.Objects;
import java.util.Optional;

public final class ReadingQualityType {

    private final String code;

    public ReadingQualityType(String code) {
        this.code = Objects.requireNonNull(code);
    }

    public static ReadingQualityType of(QualityCodeSystem system , QualityCodeIndex codeIndex) {
        return of(system, codeIndex.category(), codeIndex.index());
    }
    
    public static ReadingQualityType of(QualityCodeSystem system , QualityCodeCategory category, int index) {
        return new ReadingQualityType(Joiner.on('.').join(system.ordinal(), category.ordinal(), index));
    }

    public String getCode() {
        return code;
    }
    
    private Optional<Integer> getCode(int index) {
        String[] parts = code.split("\\.");
        if (parts.length >= index) {
            try {
                return Optional.of(Integer.parseInt(parts[index - 1]));
            } catch (NumberFormatException ex) {
                //return Optional.empty();
            }
        }
        return Optional.empty();
    }

    public int getSystemCode() {
        return getCode(1).get();
    }
    
    public Optional<QualityCodeSystem> system() {
        return QualityCodeSystem.get(getSystemCode());
    }

    public int getCategoryCode() {
        return getCode(2).get();
    }
    
    public Optional<QualityCodeCategory> category() {
        return QualityCodeCategory.get(getCategoryCode());
    }

    public int getIndexCode() {
        return getCode(3).get();
    }
    
    public Optional<QualityCodeIndex> qualityIndex() {
        return category().flatMap(category -> category.qualityCodeIndex(getIndexCode()));
    }

    public static ReadingQualityType defaultCodeForRuleId(QualityCodeSystem system, long id) {
        return ReadingQualityType.of(system, QualityCodeCategory.VALIDATION, (int) (1000 + id));
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

    public boolean hasEstimatedCategory() {
        return hasQualityCodeCategory(QualityCodeCategory.ESTIMATED);
    }

    public boolean isConfirmed() {
        return hasQualityIndex(QualityCodeIndex.ACCEPTED);
    }

    public boolean hasEditCategory() {
        return hasQualityCodeCategory(QualityCodeCategory.EDITED);
    }

    public boolean hasReasonabilityCategory() {
        return hasQualityCodeCategory(QualityCodeCategory.REASONABILITY);
    }

    public boolean hasValidationCategory() {
        return hasQualityCodeCategory(QualityCodeCategory.VALIDATION);
    }

    public boolean isSuspect() {
        return hasQualityIndex(QualityCodeIndex.SUSPECT);
    }

    public boolean isMissing() {
        return hasQualityIndex(QualityCodeIndex.KNOWNMISSINGREAD);
    }

    public boolean isError() {
        return hasQualityIndex(QualityCodeIndex.ERRORCODE);
    }

    private boolean hasQualityCodeCategory(QualityCodeCategory cat) {
        return category().filter(category -> category.equals(cat)).isPresent();
    }

    private boolean hasQualityIndex(QualityCodeIndex index) {
        return qualityIndex().filter(qualityIndex -> qualityIndex.equals(index)).isPresent();
    }

}
