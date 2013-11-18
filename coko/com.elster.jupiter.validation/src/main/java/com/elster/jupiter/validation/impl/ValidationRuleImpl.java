package com.elster.jupiter.validation.impl;

import com.elster.jupiter.orm.cache.TypeCache;
import com.elster.jupiter.validation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class ValidationRuleImpl implements ValidationRule {

    private long id;
    private boolean active;
    private ValidationAction action;
    private String implementation; //validator name

    private long ruleSetId;

    @SuppressWarnings("unused")
    private int position;
    private transient ValidationRuleSet ruleSet;
    private transient Validator validator;

    private List<ValidationRuleProperties> properties;

    private ValidationRuleImpl() {
        //for persistence
    }

    public ValidationRuleImpl(ValidationRuleSet ruleSet, ValidationAction action, String implementation, int position) {
        this.ruleSet = ruleSet;
        this.action = action;
        this.implementation = implementation;
        this.position = position;
        this.ruleSetId = ruleSet.getId();
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
            validator = Bus.getValidator(this.implementation);
            if (validator == null) {
                throw new ValidatorNotFoundException(implementation);
            }
        }
        return validator;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public ValidationAction getAction() {
        return action;
    }

    @Override
    public String getImplementation() {
        return implementation;
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
    public void activate() {
        setActive(true);
    }

    @Override
    public void deactivate() {
        setActive(false);
    }

    void setRuleSetId(long ruleSetId) {
        this.ruleSetId = ruleSetId;
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
    public int hashCode() {
        return Objects.hash(id);
    }

    private void setActive(boolean active) {
        this.active = active;
    }

    private TypeCache<ValidationRule> ruleFactory() {
        return Bus.getOrmClient().getValidationRuleFactory();
    }

    public void delete() {
        ruleFactory().remove(this);
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
        }
        //create new properties
        for (ValidationRuleProperties property : doGetProperties()) {
            ((ValidationRulePropertiesImpl) property).setRuleId(id);
            rulePropertiesFactory().persist(property);
        }
    }

    private void doUpdate() {
        ruleFactory().update(this);
    }

    private void doPersist() {
        ruleFactory().persist(this);
    }

    @Override
    public List<ValidationRuleProperties> getProperties() {
        return Collections.unmodifiableList(doGetProperties());
    }

    private List<ValidationRuleProperties> doGetProperties() {
        if (properties == null) {
            properties = loadProperties();
        }
        return properties;
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

    @Override
    public ValidationRuleProperties addProperty(String name, long value) {
        ValidationRulePropertiesImpl newProperty = new ValidationRulePropertiesImpl(this, name, value);
        doGetProperties().add(newProperty);
        return newProperty;
    }

    @Override
    public void deleteProperty(ValidationRuleProperties property) {
        doGetProperties().remove(property);
    }

    private TypeCache<ValidationRuleProperties> rulePropertiesFactory() {
        return Bus.getOrmClient().getValidationRulePropertiesFactory();
    }


}
