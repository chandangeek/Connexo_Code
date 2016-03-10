package com.elster.jupiter.metering.impl.rt.template;

import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;

import javax.inject.Inject;

public class ReadingTypeTemplateAttributeValueImpl {

    public enum Fields {
        CODE("code"),
        ATTRIBUTE("attribute");
        private String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }
    }

    private int code;
    @IsPresent(message = "{" + MessageSeeds.Constants.REQUIRED + "}")
    private Reference<ReadingTypeTemplateAttribute> attribute = Reference.empty();

    @Inject
    public ReadingTypeTemplateAttributeValueImpl() {
    }

    ReadingTypeTemplateAttributeValueImpl init(ReadingTypeTemplateAttribute attribute, int code) {
        this.attribute.set(attribute);
        this.code = code;
        return this;
    }

    public int getCode() {
        return this.code;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ReadingTypeTemplateAttributeValueImpl that = (ReadingTypeTemplateAttributeValueImpl) o;
        if (code != that.code) {
            return false;
        }
        return attribute.equals(that.attribute);
    }

    @Override
    public int hashCode() {
        int result = code;
        result = 31 * result + attribute.hashCode();
        return result;
    }
}
