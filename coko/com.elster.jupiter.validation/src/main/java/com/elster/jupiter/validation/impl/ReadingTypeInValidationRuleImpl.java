package com.elster.jupiter.validation.impl;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.validation.ReadingTypeInValidationRule;
import com.elster.jupiter.validation.ValidationRule;

import java.util.Objects;

public class ReadingTypeInValidationRuleImpl implements ReadingTypeInValidationRule {

    private long id;
    private long ruleId;
    private String readingTypeMRID;

    private ReadingType readingType;
    private ValidationRule rule;

    private ReadingTypeInValidationRuleImpl() {
    }

    ReadingTypeInValidationRuleImpl(ValidationRule rule, ReadingType readingType) {
        this.rule = rule;
        this.ruleId = rule.getId();
        this.readingType = readingType;
        this.readingTypeMRID = readingType.getMRID();
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public ValidationRule getRule() {
        if (rule == null) {
            rule = Bus.getOrmClient().getValidationRuleFactory().getExisting(ruleId);
        }
        return rule;
    }

    @Override
    public ReadingType getReadingType() {
        if (readingType == null) {
            readingType = Bus.getOrmClient().getReadingTypeFactory().getExisting(readingTypeMRID);
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
        if (this == o) {
            return true;
        }
        if (!(o instanceof ReadingTypeInValidationRuleImpl)) {
            return false;
        }

        ReadingTypeInValidationRuleImpl that = (ReadingTypeInValidationRuleImpl) o;

        return id == that.id;

    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
