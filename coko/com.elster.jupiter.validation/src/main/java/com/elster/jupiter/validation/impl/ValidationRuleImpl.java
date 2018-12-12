/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation.impl;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.util.collections.ArrayDiffList;
import com.elster.jupiter.util.collections.DiffList;
import com.elster.jupiter.util.streams.Functions;
import com.elster.jupiter.validation.EventType;
import com.elster.jupiter.validation.ReadingTypeInValidationRule;
import com.elster.jupiter.validation.ValidationAction;
import com.elster.jupiter.validation.ValidationPropertyDefinitionLevel;
import com.elster.jupiter.validation.ValidationRuleProperties;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationRuleSetVersion;
import com.elster.jupiter.validation.Validator;
import com.elster.jupiter.validation.ValidatorNotFoundException;

import com.google.common.collect.ImmutableMap;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.validation.GroupSequence;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.validation.groups.Default;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.streams.Currying.test;

@UniqueName(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.DUPLICATE_VALIDATION_RULE + "}")
@HasValidProperties(groups = {Save.Create.class, Save.Update.class})
@GroupSequence({ValidationRuleImpl.class, ValidationRuleImpl.FirstValidationGroup.class, ValidationRuleImpl.SecondValidationGroup.class})
public final class ValidationRuleImpl implements IValidationRule {
    public interface FirstValidationGroup {
    }

    ;

    public interface SecondValidationGroup {
    }

    ;
    private long id;

    @NotEmpty(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.NAME_REQUIRED_KEY + "}")
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.FIELD_SIZE_BETWEEN_1_AND_80 + "}")
    private String name;
    private boolean active;
    private ValidationAction action;
    @NotNull(message = "{" + MessageSeeds.Constants.NAME_REQUIRED_KEY + "}", groups = Default.class)
    @Size(min = 1, max = Table.NAME_LENGTH, message = "{" + MessageSeeds.Constants.FIELD_SIZE_BETWEEN_1_AND_80 + "}", groups = FirstValidationGroup.class)
    @ExistingValidator(message = "{" + MessageSeeds.Constants.NO_SUCH_VALIDATOR + "}", groups = SecondValidationGroup.class)
    private String implementation; //validator classname
    private Instant obsoleteTime;

    private long version;
    private Instant createTime;
    private Instant modTime;
    private String userName;
    // associations
    @Valid
    @Size(min = 1, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.NAME_REQUIRED_KEY + "}")
    private List<ReadingTypeInValidationRule> readingTypesInRule = new ArrayList<>();

    private Reference<ValidationRuleSetVersion> ruleSetVersion = ValueReference.absent();

    @SuppressWarnings("unused")
    private int position;
    private transient Validator templateValidator;

    private List<ValidationRuleProperties> properties = new ArrayList<>();

    private final DataModel dataModel;
    private final ValidatorCreator validatorCreator;
    private final Thesaurus thesaurus;
    private final MeteringService meteringService;
    private final EventService eventService;
    private final ServerValidationService validationService;
    private final Provider<ReadingTypeInValidationRuleImpl> readingTypeInRuleProvider;
    private final Clock clock;

    @Inject
    ValidationRuleImpl(DataModel dataModel, ValidatorCreator validatorCreator, Thesaurus thesaurus,
                       MeteringService meteringService, EventService eventService, ServerValidationService validationService,
                       Provider<ReadingTypeInValidationRuleImpl> readingTypeInRuleProvider, Clock clock) {
        this.dataModel = dataModel;
        this.validatorCreator = validatorCreator;
        this.thesaurus = thesaurus;
        this.meteringService = meteringService;
        this.eventService = eventService;
        this.validationService = validationService;
        this.readingTypeInRuleProvider = readingTypeInRuleProvider;
        this.clock = clock;
    }

    ValidationRuleImpl init(ValidationRuleSetVersion ruleSetVersion, ValidationAction action, String implementation, String name) {
        this.ruleSetVersion.set(ruleSetVersion);
        this.action = action;
        this.implementation = implementation;
        this.name = name != null ? name.trim() : name;
        this.active = false;
        return this;
    }

    @Override
    public void activate() {
        setActive(true);
    }

    @Override
    public ValidationRuleProperties addProperty(String name, Object value) {
        ValidationRulePropertiesImpl newProperty = new ValidationRulePropertiesImpl().init(this, name, value);
        properties.add(newProperty);
        return newProperty;
    }

    @Override
    public void setProperties(Map<String, Object> propertyMap) {
        DiffList<ValidationRuleProperties> entryDiff = ArrayDiffList.fromOriginal(getProperties());
        entryDiff.clear();
        List<ValidationRuleProperties> newProperties = new ArrayList<>();
        for (Map.Entry<String, Object> property : propertyMap.entrySet()) {
            ValidationRulePropertiesImpl newProperty = new ValidationRulePropertiesImpl().init(this, property.getKey(), property.getValue());
            newProperties.add(newProperty);
        }

        entryDiff.addAll(newProperties);
        for (ValidationRuleProperties property : entryDiff.getRemovals()) {
            properties.remove(property);
        }

        for (ValidationRuleProperties property : entryDiff.getRemaining()) {
            property.setValue(propertyMap.get(property.getName()));
            Optional<ValidationRuleProperties> any = properties.stream().filter(aProperty -> aProperty.getName().equals(property.getName())).findAny();
            any.ifPresent(properties::remove);
            properties.add(property);
        }
        for (ValidationRuleProperties property : entryDiff.getAdditions()) {
            properties.add(property);
        }
    }

    @Override
    public ReadingTypeInValidationRule addReadingType(ReadingType readingType) {
        ReadingTypeInValidationRuleImpl readingTypeInValidationRule = readingTypeInRuleProvider.get().init(this, readingType);
        readingTypesInRule.add(readingTypeInValidationRule);
        return readingTypeInValidationRule;
    }

    @Override
    public ReadingTypeInValidationRule addReadingType(String mRID) {
        return meteringService.getReadingType(mRID)
                .map(this::addReadingType)
                .orElseGet(() -> readingTypeInValidationRuleFor(mRID));
    }

    private ReadingTypeInValidationRule readingTypeInValidationRuleFor(String mRID) {
        ReadingTypeInValidationRuleImpl empty = readingTypeInRuleProvider.get().init(this, mRID);
        if (getId() != 0) {
            Save.UPDATE.validate(dataModel, empty);
        }
        readingTypesInRule.add(empty);
        return empty;
    }

    @Override
    public void deactivate() {
        setActive(false);
    }

    @Override
    public void delete() {
        setObsoleteTime(Instant.now(clock)); // mark obsolete
        Save.UPDATE.save(dataModel, this);
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
        return getValidator().getPropertySpecs().stream()
                .filter(PropertySpec::isRequired)
                .anyMatch(spec -> propertyKey.equals(spec.getName()));
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
        return Collections.unmodifiableList(properties);
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
        return readingTypesInRule
                .stream()
                .map(ReadingTypeInValidationRule::getReadingType)
                .collect(Collectors.toSet());
    }

    @Override
    public boolean appliesTo(ReadingType readingType) {
        return readingTypesInRule
                .stream()
                .map(ReadingTypeInValidationRule::getReadingType)
                .anyMatch(test(ReadingType::equals).with(readingType));
    }

    @Override
    public ValidationRuleSet getRuleSet() {
        return getRuleSetVersion().getRuleSet();
    }

    @Override
    public ValidationRuleSetVersion getRuleSetVersion() {
        return ruleSetVersion.get();
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

    @Override
    public List<PropertySpec> getPropertySpecs(ValidationPropertyDefinitionLevel level) {
        return getValidator().getPropertySpecs(level);
    }

    @Override
    public PropertySpec getPropertySpec(final String name) {
        return getValidator().getPropertySpecs().stream()
                .filter(p -> name.equals(p.getName()))
                .findFirst()
                .orElse(null);
    }

    @Override
    public boolean isObsolete() {
        return getObsoleteDate() != null;
    }

    @Override
    public void save() {
        if (getId() == 0) {
            Save.CREATE.validate(dataModel, this);
            getValidator().validateProperties(getProps());
            doPersist();
        } else {
            Save.UPDATE.validate(dataModel, this);
            getValidator().validateProperties(getProps());
            doUpdate();
        }
    }

    @Override
    public void rename(String name) {
        this.name = name == null ? null : name.trim();
    }

    @Override
    public void setAction(ValidationAction action) {
        this.action = action;
    }

    @Override
    public void setPosition(int position) {
        this.position = position;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(getImplementation()).append(' ').append(getAction().name()).append(' ').append(isActive());
        properties.forEach(p -> builder.append(p.toString()).append('\n'));
        return builder.toString();
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
    public Instant getObsoleteDate() {
        return this.obsoleteTime;
    }

    private void setObsoleteTime(Instant obsoleteTime) {
        this.obsoleteTime = obsoleteTime;
    }

    @Override
    public long getVersion() {
        return version;
    }

    Instant getCreateTime() {
        return createTime;
    }

    Instant getModTime() {
        return modTime;
    }

    String getUserName() {
        return userName;
    }

    @Override
    public Validator createNewValidator(ChannelsContainer channelsContainer, ReadingType readingType) {
        Map<String, Object> properties = getPropsWithOverriddenValues(channelsContainer, readingType);
        Validator createdValidator = validatorCreator.getValidator(this.implementation, properties);
        if (createdValidator == null) {
            throw new ValidatorNotFoundException(thesaurus, implementation);
        }
        return createdValidator;
    }

    private Map<String, Object> getPropsWithOverriddenValues(ChannelsContainer channelsContainer, ReadingType readingType) {
        Map<String, Object> properties = new HashMap<>(getProps());
        Map<String, Object> overriddenProperties = validationService.getValidationPropertyResolvers().stream()
                .map(resolver -> resolver.resolve(channelsContainer))
                .flatMap(Functions.asStream())
                .map(propertyProvider -> propertyProvider.getProperties(ValidationRuleImpl.this, readingType).entrySet())
                .flatMap(Collection::stream)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (v1, v2) -> v1));
        properties.putAll(overriddenProperties);
        return properties;
    }

    private void doPersist() {
        Save.CREATE.save(dataModel, this);
        eventService.postEvent(EventType.VALIDATIONRULE_CREATED.topic(), this);
    }

    private void doUpdate() {
        Save.UPDATE.save(dataModel, this);
        eventService.postEvent(EventType.VALIDATIONRULE_UPDATED.topic(), this);
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

    @Override
    public ReadingQualityType getReadingQualityType() {
        QualityCodeSystem qualityCodeSystem = getRuleSet().getQualityCodeSystem();
        return getValidator().getReadingQualityCodeIndex()
                .map(index -> ReadingQualityType.of(qualityCodeSystem, index))
                .orElse(ReadingQualityType.defaultCodeForRuleId(qualityCodeSystem, getId()));
    }

    @Override
    public boolean appliesTo(Channel channel) {
        return isActive() && getReadingTypes().stream().anyMatch(channel::hasReadingType);
    }
}
