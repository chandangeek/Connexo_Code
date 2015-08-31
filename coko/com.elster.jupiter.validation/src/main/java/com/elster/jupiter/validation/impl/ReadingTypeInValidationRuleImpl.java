package com.elster.jupiter.validation.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.validation.ReadingTypeInValidationRule;
import com.elster.jupiter.validation.ValidationRule;

import javax.inject.Inject;
import java.util.Objects;

public class ReadingTypeInValidationRuleImpl implements ReadingTypeInValidationRule {

    @ValidReadingType(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.NO_SUCH_READINGTYPE + "}")
    private String readingTypeMRID;

    private ReadingType readingType;
    private Reference<ValidationRule> rule = ValueReference.absent();
    private final MeteringService meteringService;

    @Inject
    ReadingTypeInValidationRuleImpl(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    ReadingTypeInValidationRuleImpl init(ValidationRule rule, ReadingType readingType) {
        this.rule.set(rule);
        this.readingType = readingType;
        this.readingTypeMRID = readingType.getMRID();
        return this;
    }

    ReadingTypeInValidationRuleImpl init(ValidationRule rule, String readingTypeMRID) {
        this.rule.set(rule);
        this.readingTypeMRID = readingTypeMRID;
        return this;
    }

    static ReadingTypeInValidationRuleImpl from(DataModel dataModel, ValidationRule rule, ReadingType readingType) {
        return dataModel.getInstance(ReadingTypeInValidationRuleImpl.class).init(rule, readingType);
    }

    static ReadingTypeInValidationRuleImpl from(DataModel dataModel, ValidationRule rule, String readingTypeMRID) {
        return dataModel.getInstance(ReadingTypeInValidationRuleImpl.class).init(rule, readingTypeMRID);
    }

    @Override
    public ValidationRule getRule() {
        return rule.get();
    }

    @Override
    public ReadingType getReadingType() {
        if (readingType == null) {
            readingType = meteringService.getReadingType(readingTypeMRID).get();
        }
        return readingType;
    }

    @Override
    public String toString() {
        return "ReadingTypeInValidationRule{" +
                "rule=" + rule.get() +
                ", readingType=" + readingTypeMRID +
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
        return Objects.hash(rule.get().getId(), readingTypeMRID);
    }
}
