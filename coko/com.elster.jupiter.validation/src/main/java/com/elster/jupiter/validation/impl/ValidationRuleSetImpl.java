package com.elster.jupiter.validation.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.collections.ArrayDiffList;
import com.elster.jupiter.util.collections.DiffList;
import com.elster.jupiter.util.conditions.Operator;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.time.UtcInstant;
import com.elster.jupiter.validation.ValidationAction;
import com.elster.jupiter.validation.ValidationRule;
import com.elster.jupiter.validation.ValidationRuleSet;

import javax.inject.Inject;
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

    private final EventService eventService;
    private final DataModel dataModel;

    @Inject
    ValidationRuleSetImpl(DataModel dataModel, EventService eventService) {
        // for persistence
        this.dataModel = dataModel;
        this.eventService = eventService;
    }

    ValidationRuleSetImpl init(String name, String description) {
        this.name = name;
        this.description = description;
        return this;
    }

    static ValidationRuleSetImpl from(DataModel dataModel, String name) {
        return from(dataModel, name, null);
    }

    static ValidationRuleSetImpl from(DataModel dataModel, String name, String description) {
        return dataModel.getInstance(ValidationRuleSetImpl.class).init(name, description);
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
        eventService.postEvent(EventType.VALIDATIONRULESET_UPDATED.topic(), this);
    }

    private void doPersist() {
        validationRuleSetFactory().persist(this);
        for (IValidationRule rule : doGetRules()) {
            ((ValidationRuleImpl) rule).setRuleSetId(getId());
            ((ValidationRuleImpl) rule).save();
        }
        eventService.postEvent(EventType.VALIDATIONRULESET_CREATED.topic(), this);
    }

    private DataMapper<IValidationRule> ruleFactory() {
        return dataModel.mapper(IValidationRule.class);
    }


    @Override
    public void delete() {
        validationRuleSetFactory().remove(this);
        eventService.postEvent(EventType.VALIDATIONRULESET_DELETED.topic(), this);
    }

    @Override
    public List<IValidationRule> getRules() {
        return Collections.unmodifiableList(doGetRules());
    }

    public List<IValidationRule> getRules(int start, int limit) {
        return Collections.unmodifiableList(
                dataModel.query(IValidationRule.class).select(
                        Operator.EQUAL.compare("ruleSetId", this.id), new Order[] {}, false, new String[]{}, start + 1, start + limit));
    }

    private List<IValidationRule> doGetRules() {
        if (rules == null) {
            rules = loadRules();
        }
        return  rules;
    }

    private List<IValidationRule> loadRules() {
        return ruleFactory().find("ruleSet", this);
    }

    @Override
    public IValidationRule addRule(ValidationAction action, String implementation, String name) {
        ValidationRuleImpl newRule = ValidationRuleImpl.from(dataModel, this, action, implementation, doGetRules().size() + 1, name);
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

    private DataMapper<IValidationRuleSet> validationRuleSetFactory() {
        return dataModel.mapper(IValidationRuleSet.class);
    }


}
