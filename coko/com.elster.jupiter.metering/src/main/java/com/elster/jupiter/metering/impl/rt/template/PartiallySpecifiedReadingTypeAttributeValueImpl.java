package com.elster.jupiter.metering.impl.rt.template;

import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.metering.ReadingTypeTemplateAttributeName;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;

import javax.inject.Inject;

public class PartiallySpecifiedReadingTypeAttributeValueImpl {

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
