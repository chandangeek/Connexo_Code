package com.elster.jupiter.metering.impl.rt.template;

import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.metering.ReadingTypeTemplate;
import com.elster.jupiter.metering.ReadingTypeTemplateAttribute;
import com.elster.jupiter.metering.ReadingTypeTemplateAttributeName;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

import javax.inject.Inject;
import javax.validation.ConstraintValidatorContext;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
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
    @IsPresent(message = "{" + MessageSeeds.Constants.REQUIRED + "}")
    private Reference<ReadingTypeTemplate> template = ValueReference.absent();
    @NotNull(message = "{" + MessageSeeds.Constants.REQUIRED + "}")
    private ReadingTypeTemplateAttributeName name;
    private Integer code;
    private List<ReadingTypeTemplateAttributeValueImpl> values = new ArrayList<>();

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
        boolean isValid = true;
        ReadingTypeTemplateAttributeName.ReadingTypeAttribute<?> definition = getName().getDefinition();
        List<Integer> systemAllowedCodes = getSystemAllowedCodes(definition);
        isValid = validateCodeIsNotEmpty(context, definition);
        isValid &= validateCodeIsInAllowedCodes(context, systemAllowedCodes);
        if (!this.values.isEmpty()) {
            for (int i = 0; i < this.values.size() && !systemAllowedCodes.isEmpty(); i++) {
                isValid &= validatePossibleValueIsInAllowedCodes(context, systemAllowedCodes, i);
            }
            isValid &= validateCodeIsInAllowedCodes(context, getPossibleValues());
        }
        return isValid;
    }

    private <T> List<Integer> getSystemAllowedCodes(ReadingTypeTemplateAttributeName.ReadingTypeAttribute<T> definition) {
        Function<T, Integer> converter = definition.getValueToCodeConverter();
        return definition.getPossibleValues().stream()
                .filter(Objects::nonNull)
                .map(converter)
                .collect(Collectors.toList());
    }

    private boolean validateCodeIsNotEmpty(ConstraintValidatorContext context, ReadingTypeTemplateAttributeName.ReadingTypeAttribute<?> definition) {
        if (this.code == null && !canHaveNullCode()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("{" + MessageSeeds.Constants.REQUIRED + "}")
                    .addPropertyNode(Fields.CODE.fieldName())
                    .addConstraintViolation();
            return false;
        }
        return true;
    }

    private boolean validateCodeIsInAllowedCodes(ConstraintValidatorContext context, List<Integer> allowedCodes) {
        if (this.code != null && !allowedCodes.isEmpty() && !allowedCodes.contains(this.code)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("{" + MessageSeeds.Constants.READING_TYPE_ATTRIBUTE_CODE_IS_NOT_WITHIN_LIMITS + "}")
                    .addPropertyNode(Fields.CODE.fieldName())
                    .addConstraintViolation();
            return false;
        }
        return true;
    }

    private boolean validatePossibleValueIsInAllowedCodes(ConstraintValidatorContext context, List<Integer> allowedCodes, int currentIdx) {
        if (!allowedCodes.contains(this.values.get(currentIdx).getCode())) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("{" + MessageSeeds.Constants.READING_TYPE_ATTRIBUTE_CODE_IS_NOT_WITHIN_LIMITS + "}")
                    .addPropertyNode(Fields.POSSIBLE_VALUES.fieldName() + "[" + currentIdx + "]")
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
        return getName() + ": " + getAttributeAsString();
    }

    String getAttributeAsString() {
        return this.values.isEmpty()
                ? (this.code != null ? String.valueOf(getCode().get()) : "*")
                : "(" + getPossibleValues().stream().map(String::valueOf).collect(Collectors.joining("|")) + ")";
    }
}
