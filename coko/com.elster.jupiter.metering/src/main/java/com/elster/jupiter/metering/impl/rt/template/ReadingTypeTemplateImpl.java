package com.elster.jupiter.metering.impl.rt.template;

import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.metering.ReadingTypeTemplate;
import com.elster.jupiter.metering.ReadingTypeTemplateAttribute;
import com.elster.jupiter.metering.ReadingTypeTemplateAttributeName;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.callback.PersistenceAware;

import javax.inject.Inject;
import java.time.Instant;
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

    private long version;
    private Instant createTime;
    private Instant modTime;
    private String userName;

    private Set<ReadingTypeTemplateAttribute> allAttributes = new TreeSet<>(Comparator.comparing(ReadingTypeTemplateAttribute::getName));

    @Inject
    public ReadingTypeTemplateImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    public ReadingTypeTemplateImpl init(String name) {
        this.name = name;
        postLoad();
        return this;
    }

    @Override
    public void postLoad() {
        Set<ReadingTypeTemplateAttributeName> attributeNames = EnumSet.allOf(ReadingTypeTemplateAttributeName.class);
        this.allAttributes.addAll(this.persistedAttributes);
        this.persistedAttributes.stream().forEach(attr -> attributeNames.remove(attr.getName()));
        for (ReadingTypeTemplateAttributeName attributeName : attributeNames) {
            this.allAttributes.add(dataModel.getInstance(ReadingTypeTemplateAttributeImpl.class).init(this, attributeName, null));
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
    public ReadingTypeTemplate setAttribute(ReadingTypeTemplateAttributeName name, Integer code, Integer... possibleValues) {
        ReadingTypeTemplateAttributeImpl attribute = dataModel.getInstance(ReadingTypeTemplateAttributeImpl.class)
                .init(this, name, code, possibleValues);
        this.persistedAttributes.remove(attribute);
        this.allAttributes.remove(attribute);
        if (possibleValues != null && possibleValues.length > 0 || code != null && (code != 0 || name.canBeWildcard())) { // do not persist default attributes
            this.persistedAttributes.add(attribute);
        }
        this.allAttributes.add(attribute);
        this.dataModel.touch(this);
        return this;
    }

    @Override
    public String toString() {
        return this.allAttributes.stream()
                .map(ReadingTypeTemplateAttributeImpl.class::cast)
                .map(ReadingTypeTemplateAttributeImpl::getAttributeAsString)
                .collect(Collectors.joining("."));
    }

    public void save() {
        this.dataModel.persist(this);
    }

    @Override
    public long getVersion() {
        return this.version;
    }
}
