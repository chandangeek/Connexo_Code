package com.elster.jupiter.validation.impl;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.validation.ReadingTypeInValidationRule;
import com.elster.jupiter.validation.ValidationRule;
import com.google.common.base.Optional;

import javax.inject.Inject;

public class ReadingTypeInValidationRuleImpl implements ReadingTypeInValidationRule {

    private String readingTypeMRID;

    private ReadingType readingType;
    private Reference<ValidationRule> rule = ValueReference.absent();
    private final DataModel dataModel;
    private final MeteringService meteringService;

    @Inject
    ReadingTypeInValidationRuleImpl(DataModel dataModel, MeteringService meteringService) {
        this.dataModel = dataModel;
        this.meteringService = meteringService;
    }

    ReadingTypeInValidationRuleImpl init(ValidationRule rule, ReadingType readingType) {
        this.rule.set(rule);
        this.readingType = readingType;
        this.readingTypeMRID = readingType.getMRID();
        return this;
    }

    static ReadingTypeInValidationRuleImpl from(DataModel dataModel, ValidationRule rule, ReadingType readingType) {
        return dataModel.getInstance(ReadingTypeInValidationRuleImpl.class).init(rule, readingType);
    }

    @Override
    public ValidationRule getRule() {
        return rule.get();
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
                "rule=" + rule.get() +
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

        return rule.get().getId() == that.rule.get().getId() && readingTypeMRID.equals(that.readingTypeMRID);

    }

    @Override
    public int hashCode() {
        long ruleId = rule.get().getId();
        int result = (int) (ruleId ^ (ruleId >>> 32));
        result = 31 * result + readingTypeMRID.hashCode();
        return result;
    }
}
