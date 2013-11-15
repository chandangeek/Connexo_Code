package com.elster.jupiter.validation.impl;

import com.elster.jupiter.orm.cache.TypeCache;
import com.elster.jupiter.util.collections.ArrayDiffList;
import com.elster.jupiter.util.collections.DiffList;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.util.time.UtcInstant;
import com.elster.jupiter.validation.ValidationAction;
import com.elster.jupiter.validation.ValidationRule;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.google.common.collect.FluentIterable;

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
            doPersist();
        } else {
            doUpdate();
        }
    }

    private void doUpdate() {
        validationRuleSetFactory().update(this);

        DiffList<ValidationRule> entryDiff = ArrayDiffList.fromOriginal(loadRules());
        entryDiff.clear();
        if (rules != null) {
            entryDiff.addAll(rules);
        }
        for (ValidationRule rule : entryDiff.getRemovals()) {
            ruleFactory().remove(rule);
        }

        for (ValidationRule rule : entryDiff.getRemaining()) {
            ruleFactory().update(rule);
        }

        for (ValidationRule rule : entryDiff.getAdditions()) {
            ruleFactory().persist(rule);
        }
        Bus.getEventService().postEvent(EventType.VALIDATIONRULESET_UPDATED.topic(), this);
    }

    private void doPersist() {
        validationRuleSetFactory().persist(this);
        Bus.getEventService().postEvent(EventType.VALIDATIONRULESET_CREATED.topic(), this);
        for (ValidationRule rule : doGetRules()) {
            ((ValidationRuleImpl) rule).setRuleSetId(getId());
            ruleFactory().persist(rule);
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
            rules = loadRules();
        }
        return  rules;
    }

    private ArrayList<ValidationRule> loadRules() {
        ArrayList<ValidationRule> validationRules = new ArrayList<>();
        for (ValidationRule validationRule : ruleFactory().find()) {
            if (this.equals(validationRule.getRuleSet())) {
                validationRules.add(validationRule);
            }
        }
        return validationRules;
    }

    @Override
    public ValidationRule addRule(ValidationAction action, String implementation) {
        ValidationRuleImpl newRule = new ValidationRuleImpl(this, action, implementation, doGetRules().size() + 1);
        rules.add(newRule);
        return newRule;
    }

    public void deleteRule(ValidationRule rule) {
        doGetRules();
        rules.remove(rule);
        int position = 1;
        for (ValidationRule validationRule : rules) {
            ((ValidationRuleImpl) validationRule).setPosition(position++);
        }
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
