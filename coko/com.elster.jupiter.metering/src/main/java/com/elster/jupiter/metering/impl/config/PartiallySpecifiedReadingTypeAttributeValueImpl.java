package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.metering.ReadingTypeTemplate;
import com.elster.jupiter.metering.ReadingTypeTemplateAttribute;
import com.elster.jupiter.metering.ReadingTypeTemplateAttributeName;
import com.elster.jupiter.metering.config.PartiallySpecifiedReadingType;
import com.elster.jupiter.metering.impl.rt.template.SelfObjectValidator;
import com.elster.jupiter.metering.impl.rt.template.SelfValid;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;

import javax.inject.Inject;
import javax.validation.ConstraintValidatorContext;
import java.util.List;

@SelfValid
public class PartiallySpecifiedReadingTypeAttributeValueImpl implements SelfObjectValidator {

    public enum Fields {
        READING_TYPE_REQUIREMENT("readingTypeRequirement"),
        ATTRIBUTE_NAME("attributeName"),
        CODE("code"),;
        private String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }
    }

    @IsPresent(message = "{" + MessageSeeds.Constants.REQUIRED + "}")
    private Reference<PartiallySpecifiedReadingType> readingTypeRequirement = Reference.empty();
    private ReadingTypeTemplateAttributeName attributeName;
    private int code;

    @Inject
    public PartiallySpecifiedReadingTypeAttributeValueImpl() {
    }

    PartiallySpecifiedReadingTypeAttributeValueImpl init(PartiallySpecifiedReadingType partiallySpecifiedReadingType, ReadingTypeTemplateAttributeName attributeName, int code) {
        this.readingTypeRequirement.set(partiallySpecifiedReadingType);
        this.attributeName = attributeName;
        this.code = code;
        return this;
    }

    public int getCode() {
        return this.code;
    }

    public ReadingTypeTemplateAttributeName getName() {
        return this.attributeName;
    }

    @Override
    public boolean validate(ConstraintValidatorContext context) {
        ReadingTypeTemplate readingTypeTemplate = this.readingTypeRequirement.get().getReadingTypeTemplate();
        ReadingTypeTemplateAttribute templateAttribute = readingTypeTemplate.getAttribute(getName());
        List<Integer> attributePossibleValues = templateAttribute.getPossibleValues();
        if (!isCodeInAttributePossibleValues(attributePossibleValues)
                || !isCodeInSystemPossibleValues(templateAttribute, attributePossibleValues)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("{" + MessageSeeds.Constants.READING_TYPE_ATTRIBUTE_CODE_IS_NOT_WITHIN_LIMITS + "}")
                    .addPropertyNode(Fields.CODE.fieldName())
                    .addConstraintViolation();
            return false;
        }
        return true;
    }

    private boolean isCodeInAttributePossibleValues(List<Integer> attributePossibleValues) {
        return attributePossibleValues.isEmpty() || attributePossibleValues.contains(this.code);
    }

    private boolean isCodeInSystemPossibleValues(ReadingTypeTemplateAttribute templateAttribute, List<Integer> attributePossibleValues) {
        return !attributePossibleValues.isEmpty()
                || templateAttribute.getName().getDefinition().getPossibleValues().isEmpty()
                || templateAttribute.getName().getDefinition().getPossibleValues()
                .stream()
                .map(possibleValue -> ReadingTypeTemplateAttributeName.getCodeFromAttributeValue(templateAttribute.getName().getDefinition(), possibleValue))
                .anyMatch(possibleCode -> possibleCode == this.code);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PartiallySpecifiedReadingTypeAttributeValueImpl that = (PartiallySpecifiedReadingTypeAttributeValueImpl) o;
        return readingTypeRequirement.equals(that.readingTypeRequirement) && attributeName == that.attributeName;
    }

    @Override
    public int hashCode() {
        int result = readingTypeRequirement != null ? readingTypeRequirement.hashCode() : 0;
        result = 31 * result + (attributeName != null ? attributeName.hashCode() : 0);
        return result;
    }
}
