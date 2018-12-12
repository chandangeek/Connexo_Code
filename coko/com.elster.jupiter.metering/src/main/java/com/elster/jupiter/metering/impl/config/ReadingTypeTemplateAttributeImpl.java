/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.config.ReadingTypeTemplate;
import com.elster.jupiter.metering.config.ReadingTypeTemplateAttribute;
import com.elster.jupiter.metering.config.ReadingTypeTemplateAttributeName;
import com.elster.jupiter.metering.impl.PrivateMessageSeeds;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

import javax.inject.Inject;
import javax.validation.ConstraintValidatorContext;
import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@SelfValid
public class ReadingTypeTemplateAttributeImpl implements ReadingTypeTemplateAttribute, SelfObjectValidator {

    public enum Fields {
        ID("id"),
        TEMPLATE("template"),
        NAME("name"),
        CODE("code"),
        POSSIBLE_VALUES("values");
        private String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }
    }

    private final DataModel dataModel;

    private long id;
    @IsPresent(message = "{" + PrivateMessageSeeds.Constants.REQUIRED + "}")
    private Reference<ReadingTypeTemplate> template = ValueReference.absent();
    @NotNull(message = "{" + PrivateMessageSeeds.Constants.REQUIRED + "}")
    private ReadingTypeTemplateAttributeName name;
    private Integer code;
    private List<ReadingTypeTemplateAttributeValueImpl> values = new ArrayList<>();

    @SuppressWarnings("unused")
    private long version;
    @SuppressWarnings("unused")
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;
    @SuppressWarnings("unused")
    private String userName;

    @Inject
    public ReadingTypeTemplateAttributeImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    public ReadingTypeTemplateAttributeImpl init(ReadingTypeTemplate template, ReadingTypeTemplateAttributeName name, Integer code, Integer... possibleValues) {
        this.template.set(template);
        this.name = name;
        this.code = code;
        if (possibleValues != null && possibleValues.length > 0) {
            this.values.addAll(Arrays.stream(possibleValues)
                    .filter(Objects::nonNull)
                    .sorted(Integer::compare)
                    .map(value -> dataModel.getInstance(ReadingTypeTemplateAttributeValueImpl.class).init(this, value))
                    .collect(Collectors.toList()));
        }
        if (this.code == null && !canHaveNullCode()) {
            this.code = 0;
        }
        return this;
    }

    private boolean canHaveNullCode() {
        return !this.values.isEmpty() || this.name.getDefinition().canBeWildcard();
    }

    @Override
    public boolean validate(ConstraintValidatorContext context) {
        ReadingTypeTemplateAttributeName.ReadingTypeAttribute<?> definition = getName().getDefinition();
        boolean isValid = validateCodeIsNotEmpty(context, definition);
        if (!this.values.isEmpty()) {
            isValid &= validateCodeIsInAllowedCodes(context, getPossibleValues());
            if (ReadingTypeTemplateAttributeName.UNIT_OF_MEASURE == getName()) {
                isValid &= validateAllPossibleValuesHasTheSameDimension(context, getPossibleValues());
            }
        }
        return isValid;
    }

    private boolean validateCodeIsNotEmpty(ConstraintValidatorContext context, ReadingTypeTemplateAttributeName.ReadingTypeAttribute<?> definition) {
        if (this.code == null && !canHaveNullCode()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("{" + PrivateMessageSeeds.Constants.REQUIRED + "}")
                    .addPropertyNode(Fields.CODE.fieldName())
                    .addConstraintViolation();
            return false;
        }
        return true;
    }

    private boolean validateCodeIsInAllowedCodes(ConstraintValidatorContext context, List<Integer> allowedCodes) {
        if (this.code != null && !allowedCodes.isEmpty() && !allowedCodes.contains(this.code)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("{" + PrivateMessageSeeds.Constants.READING_TYPE_ATTRIBUTE_CODE_IS_NOT_WITHIN_LIMITS + "}")
                    .addPropertyNode(Fields.CODE.fieldName())
                    .addConstraintViolation();
            return false;
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    private boolean validateAllPossibleValuesHasTheSameDimension(ConstraintValidatorContext context, List<Integer> possibleValues) {
        ReadingTypeTemplateAttributeName.ReadingTypeAttribute<ReadingTypeUnit> definition =
                (ReadingTypeTemplateAttributeName.ReadingTypeAttribute<ReadingTypeUnit>) ReadingTypeTemplateAttributeName.UNIT_OF_MEASURE.getDefinition();
        if (possibleValues.stream()
                .map(definition.getCodeToValueConverter())
                .map(rtUnit -> rtUnit.getUnit().getDimension())
                .distinct().count() != 1) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("{" + PrivateMessageSeeds.Constants.READING_TYPE_TEMPLATE_UNITS_SHOULD_HAVE_THE_SAME_DIMENSION + "}")
                    .addPropertyNode(Fields.POSSIBLE_VALUES.fieldName())
                    .addConstraintViolation();
            return false;
        }
        return true;
    }

    @Override
    public ReadingTypeTemplateAttributeName getName() {
        return this.name;
    }

    @Override
    public Optional<Integer> getCode() {
        return Optional.ofNullable(this.code);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Integer> getPossibleValues() {
        if (this.values.isEmpty()) {
            return Collections.emptyList();
        }
        return this.values.stream()
                .map(ReadingTypeTemplateAttributeValueImpl::getCode)
                .collect(Collectors.toList());
    }

    @Override
    public boolean matches(ReadingType candidate) {
        ReadingTypeTemplateAttributeName.ReadingTypeAttribute<?> definition = getName().getDefinition();
        if (getCode().isPresent()) {
            return ReadingTypeTemplateAttributeName.getReadingTypeAttributeCode(definition, candidate) == getCode().get();
        } else if (!this.values.isEmpty()) {
            this.getPossibleValues().contains(ReadingTypeTemplateAttributeName.getReadingTypeAttributeCode(definition, candidate));
        }
        return true; // true for wildcard
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ReadingTypeTemplateAttributeImpl that = (ReadingTypeTemplateAttributeImpl) o;
        return name == that.name && template.equals(that.template);
    }

    @Override
    public int hashCode() {
        int result = template != null ? template.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return getName() + ": " + (this.code != null ? "x:=" + String.valueOf(this.code) + ", " : "") +
                (this.values.isEmpty() ? "x∈{ℕ}" : getPossibleValues().stream().map(String::valueOf).collect(Collectors.joining(",", "x∈{", "}")));
    }

    String getAttributeAsString() {
        return this.code != null
                ? String.valueOf(getCode().get())
                : this.values.isEmpty() ? "*" : "(" + getPossibleValues().stream().map(String::valueOf).collect(Collectors.joining(",")) + ")";
    }

    void prepareDelete() {
        values.clear();
    }
}
