package com.elster.jupiter.validation.impl;

import com.elster.jupiter.orm.cache.TypeCache;
import com.elster.jupiter.util.collections.ArrayDiffList;
import com.elster.jupiter.util.collections.DiffList;
import com.elster.jupiter.util.time.UtcInstant;
import com.elster.jupiter.validation.ValidationAction;
import com.elster.jupiter.validation.ValidationRule;
import com.elster.jupiter.validation.ValidationRuleSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class ValidationRuleSetImpl implements IValidationRuleSet {

    private long id;
    private String mRID;
    private String name;
    private String aliasName;
    private String description;

    private long version;
    private UtcInstant createTime;
    private UtcInstant modTime;
    private String userName;

    private List<IValidationRule> rules;

    private ValidationRuleSetImpl() {
        // for persistence
    }

    public ValidationRuleSetImpl(String name) {
        this.name = name;
    }

    public ValidationRuleSetImpl(String name, String description) {
        this.name = name;
        this.description = description;
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

        DiffList<IValidationRule> entryDiff = ArrayDiffList.fromOriginal(loadRules());
        entryDiff.clear();
        if (rules != null) {
            entryDiff.addAll(rules);
        }
        for (IValidationRule rule : entryDiff.getRemovals()) {
            rule.delete();
        }

        for (IValidationRule rule : entryDiff.getRemaining()) {
            rule.save();
        }

        for (IValidationRule rule : entryDiff.getAdditions()) {
            rule.save();
        }
        Bus.getEventService().postEvent(EventType.VALIDATIONRULESET_UPDATED.topic(), this);
    }

    private void doPersist() {
        validationRuleSetFactory().persist(this);
        for (IValidationRule rule : doGetRules()) {
            ((ValidationRuleImpl) rule).setRuleSetId(getId());
            ((ValidationRuleImpl) rule).save();
        }
        Bus.getEventService().postEvent(EventType.VALIDATIONRULESET_CREATED.topic(), this);
    }

    private TypeCache<IValidationRule> ruleFactory() {
        return Bus.getOrmClient().getValidationRuleFactory();
    }


    @Override
    public void delete() {
        validationRuleSetFactory().remove(this);
        Bus.getEventService().postEvent(EventType.VALIDATIONRULESET_DELETED.topic(), this);
    }

    @Override
    public List<IValidationRule> getRules() {
        return Collections.unmodifiableList(doGetRules());
    }

    private List<IValidationRule> doGetRules() {
        if (rules == null) {
            rules = loadRules();
        }
        return  rules;
    }

    private List<IValidationRule> loadRules() {
        ArrayList<IValidationRule> validationRules = new ArrayList<>();
        for (IValidationRule validationRule : ruleFactory().find()) {
            if (this.equals(validationRule.getRuleSet())) {
                validationRules.add(validationRule);
            }
        }
        return validationRules;
    }

    @Override
    public IValidationRule addRule(ValidationAction action, String implementation) {
        ValidationRuleImpl newRule = new ValidationRuleImpl(this, action, implementation, doGetRules().size() + 1);
        rules.add(newRule);
        return newRule;
    }

    @Override
    public void deleteRule(ValidationRule rule) {
        doGetRules();
        rules.remove(rule);
        int position = 1;
        for (IValidationRule validationRule : rules) {
            validationRule.setPosition(position++);
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(getName());
        builder.append('\n');
        for (IValidationRule validationRule : doGetRules()) {
            builder.append(validationRule.toString()).append('\n');
        }
        return builder.toString();
    }

    private TypeCache<IValidationRuleSet> validationRuleSetFactory() {
        return Bus.getOrmClient().getValidationRuleSetFactory();
    }


}
