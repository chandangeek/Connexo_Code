package com.elster.jupiter.validation.impl;

import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.cache.TypeCache;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.util.units.Quantity;
import com.elster.jupiter.validation.ReadingTypeInValidationRule;
import com.elster.jupiter.validation.ValidationAction;
import com.elster.jupiter.validation.ValidationRule;
import com.elster.jupiter.validation.ValidationRuleProperties;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationStats;
import com.elster.jupiter.validation.Validator;
import com.elster.jupiter.validation.ValidatorNotFoundException;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Ordering;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

final class ValidationRuleImpl implements ValidationRule, IValidationRule {
    private long id;
    private boolean active;
    private ValidationAction action;
    private String implementation; //validator name

    // associations
    private List<ReadingTypeInValidationRule> readingTypesInRule;

    private long ruleSetId;

    @SuppressWarnings("unused")
    private int position;
    private transient ValidationRuleSet ruleSet;
    private transient Validator validator;

    private List<ValidationRuleProperties> properties;

    public ValidationRuleImpl(ValidationRuleSet ruleSet, ValidationAction action, String implementation, int position) {
        this.ruleSet = ruleSet;
        this.action = action;
        this.implementation = implementation;
        this.position = position;
        this.ruleSetId = ruleSet.getId();
    }

    @Override
    public void activate() {
        setActive(true);
    }

    @Override
    public ValidationRuleProperties addProperty(String name, Quantity value) {
        ValidationRulePropertiesImpl newProperty = new ValidationRulePropertiesImpl(this, name, value);
        doGetProperties().add(newProperty);
        return newProperty;
    }

    @Override
    public ReadingTypeInValidationRule addReadingType(ReadingType readingType) {
        ReadingTypeInValidationRuleImpl readingTypeInValidationRule =
                new ReadingTypeInValidationRuleImpl(this, readingType);
        doGetReadingTypesInValidationRule().add(readingTypeInValidationRule);
        return readingTypeInValidationRule;
    }

    @Override
    public void deactivate() {
        setActive(false);
    }

    public void delete() {
        ruleFactory().remove(this);
    }

    @Override
    public void deleteProperty(ValidationRuleProperties property) {
        doGetProperties().remove(property);
    }

    @Override
    public void deleteReadingType(ReadingType readingType) {
        ReadingTypeInValidationRule readingTypeInValidationRule = getReadingTypeInRule(readingType);
        if (readingTypeInValidationRule != null) {
            doGetReadingTypesInValidationRule().remove(readingTypeInValidationRule);
        }
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
    public List<ValidationRuleProperties> getProperties() {
        return Collections.unmodifiableList(doGetProperties());
    }

    @Override
    public Map<String, Quantity> getProps() {
        ImmutableMap.Builder<String, Quantity> builder = ImmutableMap.builder();
        for (ValidationRuleProperties validationRuleProperties : getProperties()) {
            builder.put(validationRuleProperties.getName(), validationRuleProperties.getValue());
        }
        return builder.build();
    }

    @Override
    public Set<ReadingType> getReadingTypes() {
        Set<ReadingType> result = new HashSet<>();
        for (ReadingTypeInValidationRule readingTypeInRule : getReadingTypesInRule()) {
            result.add(readingTypeInRule.getReadingType());
        }
        return result;
    }

    public List<ReadingTypeInValidationRule> getReadingTypesInRule() {
        return ImmutableList.copyOf(doGetReadingTypesInValidationRule());
    }

    @Override
    public ValidationRuleSet getRuleSet() {
        if (ruleSet == null) {
            ruleSet = Bus.getOrmClient().getValidationRuleSetFactory().get(ruleSetId).get();
        }
        return ruleSet;
    }

    @Override
    public Validator getValidator() {
        if (validator == null) {
            validator = Bus.getValidator(this.implementation, getProps());
            if (validator == null) {
                throw new ValidatorNotFoundException(implementation);
            }
        }
        return validator;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public boolean isActive() {
        return active;
    }

    public void save() {
        if (getId() == 0) {
            doPersist();
        } else {
            doUpdate();
            // remove all properties
            for (ValidationRuleProperties property : loadProperties()) {
                rulePropertiesFactory().remove(property);
            }
            //remove all reading types
            for (ReadingTypeInValidationRule readingTypeInValidationRule : loadReadingTypesInValidationRule()) {
                readingTypesInRuleFactory().remove(readingTypeInValidationRule);
            }
        }
        //create new properties
        for (ValidationRuleProperties property : doGetProperties()) {
            ((ValidationRulePropertiesImpl) property).setRuleId(id);
            rulePropertiesFactory().persist(property);
        }
        //create new readingTypes
        for (ReadingTypeInValidationRule readingTypeInValidationRule : this.doGetReadingTypesInValidationRule()) {
            ((ReadingTypeInValidationRuleImpl) readingTypeInValidationRule).setRuleId(id);
            readingTypesInRuleFactory().persist(readingTypeInValidationRule);
        }
    }

    @Override
    public void setAction(ValidationAction action) {
        this.action = action;
    }

    @Override
    public void setImplementation(String implementation) {
        this.implementation = implementation;
    }

    @Override
    public void setPosition(int position) {
        this.position = position;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(getImplementation()).append(' ').append(getAction().name()).append(' ').append(isActive());
        for (ValidationRuleProperties property : doGetProperties()) {
            builder.append(property.toString()).append('\n');
        }
        return builder.toString();
    }

    @Override
    public Date validateChannel(Channel channel, Interval interval) {
        Date earliestLastChecked = null;
        for (ReadingType channelReadingType : channel.getReadingTypes()) {
            if (getReadingTypes().contains(channelReadingType)) {
                ValidationStats stats = getValidator().validate(channel, channelReadingType, interval);
                earliestLastChecked = earliestLastChecked == null ? stats.getLastChecked() : Ordering.natural().min(earliestLastChecked, stats.getLastChecked());
            }
        }
        return earliestLastChecked;
    }

    void setRuleSetId(long ruleSetId) {
        this.ruleSetId = ruleSetId;
    }

    private ValidationRuleImpl() {
        //for persistence
    }

    private List<ValidationRuleProperties> doGetProperties() {
        if (properties == null) {
            properties = loadProperties();
        }
        return properties;
    }

    private List<ReadingTypeInValidationRule> doGetReadingTypesInValidationRule() {
        if (readingTypesInRule == null) {
            readingTypesInRule = loadReadingTypesInValidationRule();
        }
        return readingTypesInRule;
    }

    private void doPersist() {
        ruleFactory().persist(this);
    }

    private void doUpdate() {
        ruleFactory().update(this);
    }

    public ReadingTypeInValidationRule getReadingTypeInRule(ReadingType readingType){
        for (ReadingTypeInValidationRule readingTypeInValidationRule : doGetReadingTypesInValidationRule()) {
            if (readingTypeInValidationRule.getReadingType().equals(readingType)) {
                return readingTypeInValidationRule;
            }
        }
        return null;
    }

    private List<ValidationRuleProperties> loadProperties() {
        ArrayList<ValidationRuleProperties> validationRulesProperties = new ArrayList<>();
        for (ValidationRuleProperties property : rulePropertiesFactory().find()) {
            if (this.equals(property.getRule())) {
                validationRulesProperties.add(property);
            }
        }
        return validationRulesProperties;
    }

    private List<ReadingTypeInValidationRule> loadReadingTypesInValidationRule() {
        return readingTypesInRuleFactory().find("ruleId",this.getId());
    }

    private DataMapper<ReadingTypeInValidationRule> readingTypesInRuleFactory() {
        return Bus.getOrmClient().getReadingTypesInValidationRuleFactory();
    }

    private TypeCache<IValidationRule> ruleFactory() {
        return Bus.getOrmClient().getValidationRuleFactory();
    }

    private TypeCache<ValidationRuleProperties> rulePropertiesFactory() {
        return Bus.getOrmClient().getValidationRulePropertiesFactory();
    }

    private void setActive(boolean active) {
        this.active = active;
    }
}
