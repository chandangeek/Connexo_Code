package com.elster.jupiter.validation.impl;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.validation.ReadingTypeInValidationRule;
import com.elster.jupiter.validation.ValidationRule;
import com.google.common.base.Optional;

import javax.inject.Inject;

public class ReadingTypeInValidationRuleImpl implements ReadingTypeInValidationRule {

    private long ruleId;
    private String readingTypeMRID;

    private ReadingType readingType;
    private ValidationRule rule;
    private final DataModel dataModel;
    private final MeteringService meteringService;

    @Inject
    ReadingTypeInValidationRuleImpl(DataModel dataModel, MeteringService meteringService) {
        this.dataModel = dataModel;
        this.meteringService = meteringService;
    }

    ReadingTypeInValidationRuleImpl init(ValidationRule rule, ReadingType readingType) {
        this.rule = rule;
        this.ruleId = rule.getId();
        this.readingType = readingType;
        this.readingTypeMRID = readingType.getMRID();
        return this;
    }

    static ReadingTypeInValidationRuleImpl from(DataModel dataModel, ValidationRule rule, ReadingType readingType) {
        return dataModel.getInstance(ReadingTypeInValidationRuleImpl.class).init(rule, readingType);
    }

    @Override
    public ValidationRule getRule() {
        if (rule == null) {
            rule = dataModel.mapper(ValidationRule.class).getExisting(ruleId);
        }
        return rule;
    }

    public void setRuleId(long ruleId) {
        this.ruleId = ruleId;
    }

    @Override
    public ReadingType getReadingType() {
        if (readingType == null) {
            Optional<ReadingType> optional = meteringService.getReadingType(readingTypeMRID);
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
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ReadingTypeInValidationRuleImpl that = (ReadingTypeInValidationRuleImpl) o;

        return ruleId == that.ruleId && readingTypeMRID.equals(that.readingTypeMRID);

    }

    @Override
    public int hashCode() {
        int result = (int) (ruleId ^ (ruleId >>> 32));
        result = 31 * result + readingTypeMRID.hashCode();
        return result;
    }
}
