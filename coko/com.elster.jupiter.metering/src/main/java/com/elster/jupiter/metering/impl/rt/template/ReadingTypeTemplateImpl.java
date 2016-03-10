package com.elster.jupiter.metering.impl.rt.template;

import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.callback.PersistenceAware;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class ReadingTypeTemplateImpl implements ReadingTypeTemplate, PersistenceAware {
    public enum Fields {
        ID("id"),
        NAME("name"),
        ATTRIBUTES("persistedAttributes");
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
    @NotEmpty(message = "{" + MessageSeeds.Constants.REQUIRED + "}")
    private String name;
    private List<ReadingTypeTemplateAttribute> persistedAttributes = new ArrayList<>(ReadingTypeTemplateAttributeName.values().length);

    private Set<ReadingTypeTemplateAttribute> allAttributes;

    @Inject
    public ReadingTypeTemplateImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public void postLoad() {
        this.allAttributes = new TreeSet<>(Comparator.comparing(ReadingTypeTemplateAttribute::getName));
        Set<ReadingTypeTemplateAttributeName> attributeNames = EnumSet.allOf(ReadingTypeTemplateAttributeName.class);
        this.allAttributes.addAll(this.persistedAttributes);
        this.persistedAttributes.stream().forEach(attr -> attributeNames.remove(attr.getName()));
        for (ReadingTypeTemplateAttributeName attributeName : attributeNames) {
            this.allAttributes.add(dataModel.getInstance(ReadingTypeTemplateAttributeImpl.class)
                    .init(this, attributeName, 0, true));
        }
    }

    @Override
    public long getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Set<ReadingTypeTemplateAttribute> getAttributes() {
        return this.allAttributes;
    }

    @Override
    public ReadingTypeTemplate setAttribute(ReadingTypeTemplateAttributeName name, int code, boolean canBeAny, Integer... possibleValues) {
        ReadingTypeTemplateAttributeImpl attribute = dataModel.getInstance(ReadingTypeTemplateAttributeImpl.class)
                .init(this, name, code, canBeAny, possibleValues);
        this.persistedAttributes.remove(attribute);
        if (code != 0 || !canBeAny) { // do not persist default attributes
            this.persistedAttributes.add(attribute);
        }
        this.allAttributes.add(attribute);
        return this;
    }

    @Override
    public String toString() {
        return this.allAttributes.stream()
                .map(ReadingTypeTemplateAttributeImpl.class::cast)
                .map(ReadingTypeTemplateAttributeImpl::getAttributeAsString)
                .collect(Collectors.joining("."));
    }
}
