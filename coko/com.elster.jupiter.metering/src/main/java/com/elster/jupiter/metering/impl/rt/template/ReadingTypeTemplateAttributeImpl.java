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
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ReadingTypeTemplateAttributeImpl implements ReadingTypeTemplateAttribute {
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
        if (this.code == null && !this.name.canBeWildcard()) {
            this.code = 0;
        }
        if (possibleValues != null && possibleValues.length > 0) {
            this.values.addAll(Arrays.stream(possibleValues)
                    .filter(Objects::nonNull)
                    .map(value -> dataModel.getInstance(ReadingTypeTemplateAttributeValueImpl.class).init(this, value))
                    .collect(Collectors.toList()));
        }
        return this;
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
            Function<Object, Integer> valueToCodeConverter = (Function<Object, Integer>) getName().getValueToCodeConverter();
            return getName().getPossibleValues()
                    .stream()
                    .map(Object.class::cast)
                    .map(valueToCodeConverter::apply)
                    .collect(Collectors.toList());
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
                ? getCode().isPresent() ? String.valueOf(getCode().get()) : "*"
                : "(" + getPossibleValues().stream().map(String::valueOf).collect(Collectors.joining("|")) + ")";
    }
}
