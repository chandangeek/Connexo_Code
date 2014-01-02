package com.elster.jupiter.validation.impl;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.validation.ReadingTypeInValidationRule;
import com.elster.jupiter.validation.ValidationRule;
import com.google.common.base.Optional;

import javax.inject.Inject;

public class ReadingTypeInValidationRuleImpl implements ReadingTypeInValidationRule {

    private long ruleId;
    private String readingTypeMRID;

    private ReadingType readingType;
    private ValidationRule rule;

    @Inject
    private ReadingTypeInValidationRuleImpl() {
    }

    ReadingTypeInValidationRuleImpl(ValidationRule rule, ReadingType readingType) {
        this.rule = rule;
        this.ruleId = rule.getId();
        this.readingType = readingType;
        this.readingTypeMRID = readingType.getMRID();
    }

    @Override
    public ValidationRule getRule() {
        if (rule == null) {
            rule = Bus.getOrmClient().getValidationRuleFactory().getExisting(ruleId);
        }
        return rule;
    }

    public void setRuleId(long ruleId) {
        this.ruleId = ruleId;
    }

    @Override
    public ReadingType getReadingType() {
        if (readingType == null) {
            Optional<ReadingType> optional = Bus.getMeteringService().getReadingType(readingTypeMRID);
            return (optional.isPresent() ? optional.get() : null);
        }
        return readingType;
    }

    @Override
    public String toString() {
        return "ReadingTypeInValidationRule{" +
                "rule=" + rule +
                ", readingType=" + readingType +
                '}';
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ReadingTypeInValidationRuleImpl that = (ReadingTypeInValidationRuleImpl) o;

        if (ruleId != that.ruleId) return false;
        if (!readingTypeMRID.equals(that.readingTypeMRID)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (ruleId ^ (ruleId >>> 32));
        result = 31 * result + readingTypeMRID.hashCode();
        return result;
    }
}
