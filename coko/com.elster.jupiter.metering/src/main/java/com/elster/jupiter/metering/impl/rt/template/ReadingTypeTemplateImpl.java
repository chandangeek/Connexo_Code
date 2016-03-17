package com.elster.jupiter.metering.impl.rt.template;

import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.ReadingTypeTemplate;
import com.elster.jupiter.metering.ReadingTypeTemplateAttribute;
import com.elster.jupiter.metering.ReadingTypeTemplateAttributeName;
import com.elster.jupiter.metering.impl.config.HasUniqueName;
import com.elster.jupiter.metering.impl.config.UniqueName;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.callback.PersistenceAware;

import javax.inject.Inject;
import javax.validation.Valid;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.conditions.Where.where;

@UniqueName(groups = {Save.Create.class}, message = "{" + MessageSeeds.Constants.OBJECT_MUST_HAVE_UNIQUE_NAME + "}")
public class ReadingTypeTemplateImpl implements ReadingTypeTemplate, PersistenceAware, HasUniqueName {
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
    @Valid
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
        return Collections.unmodifiableSet(this.allAttributes);
    }

    @Override
    public ReadingTypeTemplateAttribute getAttribute(ReadingTypeTemplateAttributeName attributeName) {
        return this.allAttributes
                .stream()
                .filter(attr -> attributeName == attr.getName())
                .findAny()
                .orElseThrow(() -> new IllegalStateException("ReadingTypeTemplate has missed " + getName()));
    }

    @Override
    public String toString() {
        return getName() + " " + this.allAttributes.stream()
                .map(ReadingTypeTemplateAttributeImpl.class::cast)
                .map(ReadingTypeTemplateAttributeImpl::getAttributeAsString)
                .collect(Collectors.joining("."));
    }

    public void save() {
        Save.action(getId()).save(this.dataModel, this);
    }

    @Override
    public boolean matches(ReadingType candidate) {
        return this.allAttributes.stream().allMatch(attr -> attr.matches(candidate));
    }

    @Override
    public long getVersion() {
        return this.version;
    }

    @Override
    public ReadingTypeTemplateUpdater updater() {
        return new ReadingTypeTemplateUpdaterImpl(this);
    }

    @Override
    public boolean validateName() {
        return this.dataModel.query(ReadingTypeTemplate.class).select(where(Fields.NAME.fieldName()).isEqualTo(getName())).isEmpty();
    }

    @Override
    public boolean hasWildcards() {
        return this.allAttributes.stream().anyMatch(attr -> !attr.getCode().isPresent() && attr.getPossibleValues().isEmpty());
    }

    private static class ReadingTypeTemplateUpdaterImpl implements ReadingTypeTemplateUpdater {

        private final ReadingTypeTemplateImpl template;
        private List<ReadingTypeTemplateAttributeImpl> attributes;

        private ReadingTypeTemplateUpdaterImpl(ReadingTypeTemplateImpl template) {
            this.template = template;
            this.attributes = new ArrayList<>();
        }

        @Override
        public ReadingTypeTemplateUpdater setAttribute(ReadingTypeTemplateAttributeName name, Integer code, Integer... possibleValues) {
            attributes.add(template.dataModel.getInstance(ReadingTypeTemplateAttributeImpl.class)
                    .init(template, name, code, possibleValues));
            return this;
        }

        @Override
        public void done() {
            if (!this.attributes.isEmpty()) {
                this.template.allAttributes.removeAll(this.attributes);
                this.template.allAttributes.addAll(this.attributes);
                this.template.persistedAttributes.removeAll(this.attributes);
                ListIterator<ReadingTypeTemplateAttributeImpl> attrItr = this.attributes.listIterator();
                while (attrItr.hasNext()) {
                    ReadingTypeTemplateAttributeImpl attribute = attrItr.next();
                    Save.CREATE.validate(this.template.dataModel, attribute);
                    // do not persist default attributes
                    if (attribute.getPossibleValues().isEmpty() && (!attribute.getCode().isPresent()
                            || attribute.getCode().get() == 0 && !attribute.getName().getDefinition().canBeWildcard())) {
                        attrItr.remove();
                    }
                }
                this.template.persistedAttributes.addAll(this.attributes);
                this.template.dataModel.touch(this.template);
            }
        }
    }
}
