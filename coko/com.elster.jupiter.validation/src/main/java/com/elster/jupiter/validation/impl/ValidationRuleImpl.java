package com.elster.jupiter.validation.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.util.collections.ArrayDiffList;
import com.elster.jupiter.util.collections.DiffList;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.util.time.UtcInstant;
import com.elster.jupiter.validation.ReadingTypeInValidationRule;
import com.elster.jupiter.validation.ValidationAction;
import com.elster.jupiter.validation.ValidationRule;
import com.elster.jupiter.validation.ValidationRuleProperties;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.Validator;
import com.elster.jupiter.validation.ValidatorNotFoundException;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static com.elster.jupiter.validation.MessageSeeds.Constants;

@XmlRootElement
@UniqueName(groups = {Save.Create.class, Save.Update.class}, message = "{" + Constants.DUPLICATE_VALIDATION_RULE + "}")
@HasValidProperties(groups = {Save.Create.class, Save.Update.class})
public final class ValidationRuleImpl implements ValidationRule, IValidationRule {
    private long id;

    @NotNull(groups = {Save.Create.class, Save.Update.class}, message = "{" + Constants.NAME_REQUIRED_KEY + "}")
    @Size(min = 1, max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + Constants.FIELD_SIZE_BETWEEN_1_AND_80 + "}")
    private String name;
    private boolean active;
    private ValidationAction action;
    @NotNull(groups = {Save.Create.class, Save.Update.class}, message = "{" + Constants.NAME_REQUIRED_KEY + "}")
    @Size(min = 1, max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + Constants.FIELD_SIZE_BETWEEN_1_AND_80 + "}")
    @ExistingValidator(groups = {Save.Create.class, Save.Update.class}, message = "{" + Constants.NO_SUCH_VALIDATOR + "}")
    private String implementation; //validator classname
    private UtcInstant obsoleteTime;

    private long version;
    private UtcInstant createTime;
    private UtcInstant modTime;
    private String userName;
    // associations
    @Valid
    private List<ReadingTypeInValidationRule> readingTypesInRule = new ArrayList<>();

    private long ruleSetId;

    @SuppressWarnings("unused")
    private int position;
    private transient ValidationRuleSet ruleSet;
    private transient Validator templateValidator;
    private transient ChannelRuleValidator channelRuleValidator;

    private List<ValidationRuleProperties> properties = new ArrayList<>();
    private final DataModel dataModel;
    private final ValidatorCreator validatorCreator;
    private final Thesaurus thesaurus;
    private final MeteringService meteringService;
    private final EventService eventService;

    @Inject
    ValidationRuleImpl(DataModel dataModel, ValidatorCreator validatorCreator, Thesaurus thesaurus, MeteringService meteringService, EventService eventService) {
        this.dataModel = dataModel;
        this.validatorCreator = validatorCreator;
        this.thesaurus = thesaurus;
        this.meteringService = meteringService;
        this.eventService = eventService;
    }

    ValidationRuleImpl init(ValidationRuleSet ruleSet, ValidationAction action, String implementation, int position, String name) {
        this.ruleSet = ruleSet;
        this.action = action;
        this.implementation = implementation;
        this.position = position;
        this.ruleSetId = ruleSet.getId();
        this.name = name.trim();
        this.active = false;
        return this;
    }

    static ValidationRuleImpl from(DataModel dataModel, ValidationRuleSet ruleSet, ValidationAction action, String implementation, int position, String name) {
        return dataModel.getInstance(ValidationRuleImpl.class).init(ruleSet, action, implementation, position, name);
    }

    @Override
    public void activate() {
        setActive(true);
    }

    @Override
    public ValidationRuleProperties addProperty(String name, Object value) {
        ValidationRulePropertiesImpl newProperty = ValidationRulePropertiesImpl.from(dataModel, this, name, value);
        properties.add(newProperty);
        return newProperty;
    }

    public void setProperties(Map<String, Object> propertyMap) {
        DiffList<ValidationRuleProperties> entryDiff = ArrayDiffList.fromOriginal(getProperties());
        entryDiff.clear();
        List<ValidationRuleProperties> newProperties = new ArrayList<>();
        for (Map.Entry<String, Object> property : propertyMap.entrySet()) {
            ValidationRulePropertiesImpl newProperty = ValidationRulePropertiesImpl.from(dataModel, this, property.getKey(), property.getValue());
            newProperties.add(newProperty);
        }

        entryDiff.addAll(newProperties);
        for (ValidationRuleProperties property : entryDiff.getRemovals()) {
            properties.remove(property);
        }

        for (ValidationRuleProperties property : entryDiff.getRemaining()) {
            property.setValue(propertyMap.get(property.getName()));
            rulePropertiesFactory().update(property);
        }
        for (ValidationRuleProperties property : entryDiff.getAdditions()) {
            properties.add(property);
        }
    }

    @Override
    public ReadingTypeInValidationRule addReadingType(ReadingType readingType) {
        ReadingTypeInValidationRuleImpl readingTypeInValidationRule = ReadingTypeInValidationRuleImpl.from(dataModel, this, readingType);
        readingTypesInRule.add(readingTypeInValidationRule);
        return readingTypeInValidationRule;
    }

    @Override
    public ReadingTypeInValidationRule addReadingType(String mRID) {
        Optional<ReadingType> optional = meteringService.getReadingType(mRID);
        if (!optional.isPresent()) {
            ReadingTypeInValidationRuleImpl empty = ReadingTypeInValidationRuleImpl.from(dataModel, this, mRID);
            if (getId() != 0) {
                Save.UPDATE.validate(dataModel, empty);
            }
            readingTypesInRule.add(empty);
            return empty;
        }
        return addReadingType(optional.get());
    }

    @Override
    public void deactivate() {
        setActive(false);
    }

    public void delete() {
        this.setObsoleteTime(new UtcInstant(new Date())); // mark obsolete
        ruleFactory().update(this);
        eventService.postEvent(EventType.VALIDATIONRULE_DELETED.topic(), this);
    }

    void deleteProperty(ValidationRuleProperties property) {
        properties.remove(property);
    }

    @Override
    public void deleteReadingType(ReadingType readingType) {
        ReadingTypeInValidationRule readingTypeInValidationRule = getReadingTypeInRule(readingType);
        if (readingTypeInValidationRule != null) {
            readingTypesInRule.remove(readingTypeInValidationRule);
        }
    }

    @Override
    public boolean isRequired(final String propertyKey) {
        Optional<PropertySpec> propertySpecOptional = Iterables.tryFind(getValidator().getPropertySpecs(), new Predicate<PropertySpec>() {
            @Override
            public boolean apply(PropertySpec input) {
                return propertyKey.equals(input.getName());
            }
        });
        return propertySpecOptional.isPresent() && propertySpecOptional.get().isRequired();
    }

    private Validator getValidator() {
        if (templateValidator == null) {
            templateValidator = validatorCreator.getTemplateValidator(this.implementation);
        }
        return templateValidator;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        return id == ((ValidationRuleImpl) o).id;
    }

    @Override
    public ValidationAction getAction() {
        return action;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getImplementation() {
        return implementation;
    }

    @Override
    public String getDisplayName() {
        return getValidator().getDisplayName();
    }

    @Override
    public List<ValidationRuleProperties> getProperties() {
        List<ValidationRuleProperties> propertyList = new ArrayList<>();
        for (ValidationRuleProperties validationRuleProperties : properties) {
            propertyList.add(validationRuleProperties);
        }
        return propertyList;
        //return Collections.unmodifiableList(properties);
    }

    @Override
    public Map<String, Object> getProps() {
        ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
        for (ValidationRuleProperties validationRuleProperties : getProperties()) {
            builder.put(validationRuleProperties.getName(), validationRuleProperties.getValue());
        }
        return builder.build();
    }

    private ReadingTypeInValidationRule getReadingTypeInRule(ReadingType readingType) {
        for (ReadingTypeInValidationRule readingTypeInValidationRule : readingTypesInRule) {
            if (readingTypeInValidationRule.getReadingType().equals(readingType)) {
                return readingTypeInValidationRule;
            }
        }
        return null;
    }

    @Override
    public Set<ReadingType> getReadingTypes() {
        Set<ReadingType> result = new HashSet<>();
        for (ReadingTypeInValidationRule readingTypeInRule : readingTypesInRule) {
            result.add(readingTypeInRule.getReadingType());
        }
        return result;
    }

    @Override
    public ValidationRuleSet getRuleSet() {
        if (ruleSet == null) {
            ruleSet = dataModel.mapper(ValidationRuleSet.class).getOptional(ruleSetId).get();
        }
        return ruleSet;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public boolean isActive() {
        return active;
    }
    
    @Override
    public List<PropertySpec> getPropertySpecs() {
        return getValidator().getPropertySpecs();
    }
    
    public PropertySpec<?> getPropertySpec(final String name) {
        return Iterables.find(getValidator().getPropertySpecs(), new Predicate<PropertySpec>() {
            @Override
            public boolean apply(PropertySpec input) {
                return name.equals(input.getName());
            }
        });
    }

    public void save() {
        if (getId() == 0) {
            doPersist();
            return;
        }
        doUpdate();
    }

    public void rename(String name) {
        this.name = name.trim();
    }

    public void setAction(ValidationAction action) {
        this.action = action;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(getImplementation()).append(' ').append(getAction().name()).append(' ').append(isActive());
        for (ValidationRuleProperties property : properties) {
            builder.append(property.toString()).append('\n');
        }
        return builder.toString();
    }

    @Override
    public Date validateChannel(Channel channel, Interval interval) {
        if (!active) {
            return null;
        }
        return getChannelRuleValidator().validateReadings(channel, interval);
    }

    private ChannelRuleValidator getChannelRuleValidator() {
        if (channelRuleValidator == null) {
            channelRuleValidator = new ChannelRuleValidator(this);
        }
        return channelRuleValidator;
    }

    @Override
    public void toggleActivation() {
        if (active) {
            deactivate();
        } else {
            activate();
        }
    }

    @Override
    public void clearReadingTypes() {
        readingTypesInRule.clear();
    }

    @Override
    public Date getObsoleteDate() {
        return getObsoleteTime() != null ? getObsoleteTime().toDate() : null;
    }

    private UtcInstant getObsoleteTime() {
        return this.obsoleteTime;
    }

    private void setObsoleteTime(UtcInstant obsoleteTime) {
        this.obsoleteTime = obsoleteTime;
    }

    @Override
    public long getVersion() {
        return version;
    }

    UtcInstant getCreateTime() {
        return createTime;
    }

    UtcInstant getModTime() {
        return modTime;
    }

    String getUserName() {
        return userName;
    }

    void setRuleSetId(long ruleSetId) {
        this.ruleSetId = ruleSetId;
    }

    Validator createNewValidator() {
        Validator createdValidator = validatorCreator.getValidator(this.implementation, getProps());
        if (createdValidator == null) {
            throw new ValidatorNotFoundException(thesaurus, implementation);
        }
        return createdValidator;
    }

    private void doPersist() {
        ruleFactory().persist(this);
    }

    private void doUpdate() {
        ruleFactory().update(this);
    }

    private DataMapper<IValidationRule> ruleFactory() {
        return dataModel.mapper(IValidationRule.class);
    }

    private DataMapper<ValidationRuleProperties> rulePropertiesFactory() {
        return dataModel.mapper(ValidationRuleProperties.class);
    }

    private void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public String getName() {
        return name;
    }

    public String getDisplayName(String name) {
        return getValidator().getDisplayName(name);
    }
}
