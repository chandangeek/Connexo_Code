package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.ReadingTypeTemplate;
import com.elster.jupiter.metering.ReadingTypeTemplateAttribute;
import com.elster.jupiter.metering.ReadingTypeTemplateAttributeName;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.PartiallySpecifiedReadingType;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PartiallySpecifiedReadingTypeImpl extends ReadingTypeRequirementImpl implements PartiallySpecifiedReadingType {

    private final DataModel dataModel;

    @IsPresent(message = "{" + MessageSeeds.Constants.REQUIRED + "}")
    private Reference<ReadingTypeTemplate> readingTypeTemplate = ValueReference.absent();
    private List<PartiallySpecifiedReadingTypeAttributeValueImpl> overriddenAttributes = new ArrayList<>(ReadingTypeTemplateAttributeName.values().length);

    private Collection<Function<ReadingType, Boolean>> attributeMatchers;

    @Inject
    public PartiallySpecifiedReadingTypeImpl(DataModel dataModel) {
        super();
        this.dataModel = dataModel;
    }

    public PartiallySpecifiedReadingTypeImpl init(MetrologyConfiguration metrologyConfiguration, String name, ReadingTypeTemplate template) {
        super.init(metrologyConfiguration, name);
        this.readingTypeTemplate.set(template);
        return this;
    }

    @Override
    public ReadingTypeTemplate getReadingTypeTemplate() {
        return this.readingTypeTemplate.get();
    }

    @Override
    public boolean matches(ReadingType candidate) {
        if (this.attributeMatchers == null) {
            Map<ReadingTypeTemplateAttributeName, Function<ReadingType, Boolean>> attributeMatchersMap = getReadingTypeTemplate().getAttributes()
                    .stream()
                    .collect(Collectors.toMap(ReadingTypeTemplateAttribute::getName, this::getMatcherWithSystemPossibleValues));
            this.overriddenAttributes.stream().forEach(attr -> attributeMatchersMap.put(attr.getName(),
                    rt -> ReadingTypeTemplateAttributeName.getReadingTypeAttributeCode(attr.getName().getDefinition(), rt) == attr.getCode()));
            this.attributeMatchers = attributeMatchersMap.values();
        }
        return this.attributeMatchers.stream().allMatch(matcher -> matcher.apply(candidate));
    }

    private Function<ReadingType, Boolean> getMatcherWithSystemPossibleValues(ReadingTypeTemplateAttribute attribute) {
        if (attribute.getCode().isPresent() || !attribute.getPossibleValues().isEmpty()) {
            return attribute::matches;
        }
        ReadingTypeTemplateAttributeName.ReadingTypeAttribute<?> definition = attribute.getName().getDefinition();
        Set<?> systemPossibleValues = definition.getPossibleValues()
                .stream()
                .map(possibleValue -> ReadingTypeTemplateAttributeName.getCodeFromAttributeValue(definition, possibleValue))
                .collect(Collectors.toSet());
        return !systemPossibleValues.isEmpty()
                ? rt -> systemPossibleValues.contains(ReadingTypeTemplateAttributeName.getReadingTypeAttributeCode(definition, rt))
                : rt -> true;
    }

    void touch() {
        this.dataModel.touch(this);
    }

    @Override
    public PartiallySpecifiedReadingType overrideAttribute(ReadingTypeTemplateAttributeName name, int code) {
        PartiallySpecifiedReadingTypeAttributeValueImpl value = this.dataModel.getInstance(PartiallySpecifiedReadingTypeAttributeValueImpl.class);
        value.init(this, name, code);
        Save.CREATE.validate(this.dataModel, value);
        this.overriddenAttributes.remove(value);
        this.overriddenAttributes.add(value);
        touch();
        this.attributeMatchers = null;
        return this;
    }

    @Override
    public PartiallySpecifiedReadingType removeOverriddenAttribute(ReadingTypeTemplateAttributeName name) {
        if (!this.overriddenAttributes.isEmpty()) {
            ListIterator<PartiallySpecifiedReadingTypeAttributeValueImpl> itr = this.overriddenAttributes.listIterator();
            while (itr.hasNext()) {
                if (itr.next().getName() == name) {
                    itr.remove();
                    touch();
                    this.attributeMatchers = null;
                    return this;
                }
            }
        }
        return this;
    }
}
