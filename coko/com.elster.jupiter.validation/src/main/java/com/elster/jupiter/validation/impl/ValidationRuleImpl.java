package com.elster.jupiter.validation.impl;

import com.elster.jupiter.validation.ValidationAction;
import com.elster.jupiter.validation.ValidationRule;
import com.elster.jupiter.validation.ValidationRuleSet;

public class ValidationRuleImpl implements ValidationRule {

    private long id;
    private boolean active;
    private ValidationAction action;
    private String implementation; //validator name

    private long ruleSetId;

    private int position;
    private transient ValidationRuleSet ruleSet;

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

    private void setActive(boolean active) {
        this.active = active;
    }
}
