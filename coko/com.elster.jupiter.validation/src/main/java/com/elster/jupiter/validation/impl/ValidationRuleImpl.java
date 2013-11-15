package com.elster.jupiter.validation.impl;

import com.elster.jupiter.validation.*;

public final class ValidationRuleImpl implements ValidationRule {

    private long id;
    private boolean active;
    private ValidationAction action;
    private String implementation; //validator name

    private long ruleSetId;

    private int position;
    private transient ValidationRuleSet ruleSet;
    private transient Validator validator;

    private ValidationRuleImpl() {}     //for persistence

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
        return builder.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        return id == ((ValidationRuleImpl) o).id;

    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

    private void setActive(boolean active) {
        this.active = active;
    }
}
