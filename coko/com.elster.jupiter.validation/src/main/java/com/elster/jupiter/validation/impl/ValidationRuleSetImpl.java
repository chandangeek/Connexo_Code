package com.elster.jupiter.validation.impl;

import com.elster.jupiter.orm.cache.TypeCache;
import com.elster.jupiter.util.time.UtcInstant;
import com.elster.jupiter.validation.ValidationAction;
import com.elster.jupiter.validation.ValidationRule;
import com.elster.jupiter.validation.ValidationRuleSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class ValidationRuleSetImpl implements ValidationRuleSet {

    private long id;
    private String mRID;
    private String name;
    private String aliasName;
    private String description;

    private long version;
    private UtcInstant createTime;
    private UtcInstant modTime;
    private String userName;

    private List<ValidationRule> rules;

    private ValidationRuleSetImpl() {
        // for persistence
    }

    public ValidationRuleSetImpl(String name) {
        this.name = name;
    }


    @Override
    public String getAliasName() {
        return aliasName;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getMRID() {
        return mRID;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public void setMRID(String mRID) {
        this.mRID = mRID;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void setAliasName(String aliasName) {
        this.aliasName = aliasName;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ValidationRuleSet)) {
            return false;
        }

        ValidationRuleSet validationRuleSet = (ValidationRuleSet) o;

        return id == validationRuleSet.getId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public void save() {
        if (getId() == 0) {
            validationRuleSetFactory().persist(this);
            Bus.getEventService().postEvent(EventType.VALIDATIONRULESET_CREATED.topic(), this);
            for (ValidationRule rule : doGetRules()) {
                ((ValidationRuleImpl) rule).setRuleSetId(getId());
                ruleFactory().persist(rule);
            }
        } else {
            validationRuleSetFactory().update(this);
            Bus.getEventService().postEvent(EventType.VALIDATIONRULESET_UPDATED.topic(), this);

            //TODO update rules
        }
    }

    private TypeCache<ValidationRule> ruleFactory() {
        return Bus.getOrmClient().getValidationRuleFactory();
    }


    @Override
    public void delete() {
        validationRuleSetFactory().remove(this);
        Bus.getEventService().postEvent(EventType.VALIDATIONRULESET_DELETED.topic(), this);
    }

    @Override
    public List<ValidationRule> getRules() {
        return Collections.unmodifiableList(doGetRules());
    }

    private List<ValidationRule> doGetRules() {
        if (rules == null) {
            rules = new ArrayList<>();
            for (ValidationRule validationRule : ruleFactory().find()) {
                if (this.equals(validationRule.getRuleSet())) {
                    rules.add(validationRule);
                }
            }
        }
        return  rules;
    }

    @Override
    public ValidationRule addRule(ValidationAction action, String implementation) {
        ValidationRuleImpl newRule = new ValidationRuleImpl(this, action, implementation, doGetRules().size() + 1);
        rules.add(newRule);
        return newRule;
    }

    public void deleteRule(ValidationRule rule) {
        doGetRules();

    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(getName());
        builder.append('\n');
        for (ValidationRule validationRule : doGetRules()) {
            builder.append(validationRule.toString()).append('\n');
        }
        return builder.toString();
    }

    private TypeCache<ValidationRuleSet> validationRuleSetFactory() {
        return Bus.getOrmClient().getValidationRuleSetFactory();
    }


}
