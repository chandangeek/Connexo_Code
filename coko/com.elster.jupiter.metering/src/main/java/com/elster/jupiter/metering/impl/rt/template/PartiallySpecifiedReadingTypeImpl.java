package com.elster.jupiter.metering.impl.rt.template;

import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.ReadingTypeTemplate;
import com.elster.jupiter.metering.ReadingTypeTemplateAttributeName;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PartiallySpecifiedReadingTypeImpl extends ReadingTypeRequirementImpl implements PartiallySpecifiedReadingType {

    private final DataModel dataModel;

    @IsPresent(message = "{" + MessageSeeds.Constants.REQUIRED + "}")
    private Reference<ReadingTypeTemplate> template = ValueReference.absent();
    private List<PartiallySpecifiedReadingTypeAttributeValueImpl> overriddenAttributes = new ArrayList<>(ReadingTypeTemplateAttributeName.values().length);

    @Inject
    public PartiallySpecifiedReadingTypeImpl(DataModel dataModel) {
        super();
        this.dataModel = dataModel;
    }

    PartiallySpecifiedReadingTypeImpl init(ReadingTypeTemplate template, String name) {
        this.template.set(template);
        setName(name);
        return this;
    }

    @Override
    public ReadingTypeTemplate getTemplate() {
        return this.template.get();
    }

    @Override
    public boolean matches(ReadingType candidate) {
        if (this.overriddenAttributes.isEmpty()) {
            return this.template.get().matches(candidate);
        }
        Map<ReadingTypeTemplateAttributeName, Function<ReadingType, Boolean>> attributeMatchers = getTemplate().getAttributes()
                .stream()
                .collect(Collectors.toMap(attr -> attr.getName(), attr -> attr::matches));
        this.overriddenAttributes.stream().forEach(attr -> attributeMatchers.put(attr.getName(),
                rt -> ReadingTypeTemplateAttributeImpl.getReadingTypeAttributeCode(attr.getName().getDefinition(), rt) == attr.getCode()));
        return attributeMatchers.values().stream().allMatch(matcher -> matcher.apply(candidate));
    }

    void touch() {
        this.dataModel.touch(this);
    }

    @Override
    public PartiallySpecifiedReadingType overrideAttribute(ReadingTypeTemplateAttributeName name, int code) {
        PartiallySpecifiedReadingTypeAttributeValueImpl value = this.dataModel.getInstance(PartiallySpecifiedReadingTypeAttributeValueImpl.class);
        value.init(this, name, code);
        this.overriddenAttributes.remove(value);
        this.overriddenAttributes.add(value);
        touch();
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
                    return this;
                }
            }
        }
        return this;
    }
}
