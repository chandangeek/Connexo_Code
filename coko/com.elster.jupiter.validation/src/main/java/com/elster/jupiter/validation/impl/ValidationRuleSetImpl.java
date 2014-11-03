package com.elster.jupiter.validation.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.QueryExecutor;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.util.collections.ArrayDiffList;
import com.elster.jupiter.util.collections.DiffList;
import com.elster.jupiter.util.conditions.Operator;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.conditions.Where;
import com.elster.jupiter.validation.ValidationAction;
import com.elster.jupiter.validation.ValidationRule;
import com.elster.jupiter.validation.ValidationRuleProperties;
import com.elster.jupiter.validation.ValidationRuleSet;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static com.elster.jupiter.util.conditions.Where.where;
import static com.elster.jupiter.validation.MessageSeeds.Constants;

@XmlRootElement
@UniqueName(groups = {Save.Create.class, Save.Update.class}, message = "{" + Constants.DUPLICATE_VALIDATION_RULE_SET + "}")
public final class ValidationRuleSetImpl implements IValidationRuleSet {

    static final String OBSOLETE_TIME_FIELD = "obsoleteTime";

    private long id;
    private String mRID;

    @NotNull(groups = {Save.Create.class, Save.Update.class}, message = "{" + Constants.NAME_REQUIRED_KEY + "}")
    @Size(min = 1, max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + Constants.FIELD_SIZE_BETWEEN_1_AND_80 + "}")
    private String name;
    @Size(min = 0, max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + Constants.FIELD_SIZE_BETWEEN_1_AND_80 + "}")
    private String aliasName;
    @Size(min = 0, max = Table.DESCRIPTION_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + Constants.FIELD_SIZE_BETWEEN_1_AND_4000 + "}")
    private String description;
    private Instant obsoleteTime;

    private long version;
    private Instant createTime;
    private Instant modTime;
    private String userName;

    private List<IValidationRule> rules = new ArrayList<>();

    private final EventService eventService;
    private final DataModel dataModel;

    @Inject
    ValidationRuleSetImpl(DataModel dataModel, EventService eventService) {
        // for persistence
        this.dataModel = dataModel;
        this.eventService = eventService;
    }

    ValidationRuleSetImpl init(String name, String description) {
        this.name = name.trim();
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
        this.name = name.trim();
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
        Save.UPDATE.save(dataModel, this);

        for (IValidationRule rule : rules) {
            Save.UPDATE.save(dataModel, rule);
        }
        
        eventService.postEvent(EventType.VALIDATIONRULESET_UPDATED.topic(), this);
    }

    private void doPersist() {
        Save.CREATE.save(dataModel, this);
        eventService.postEvent(EventType.VALIDATIONRULESET_CREATED.topic(), this);
    }

    @Override
    public void delete() {
        this.setObsoleteTime(Instant.now()); // mark obsolete
        rules.clear();        
        validationRuleSetFactory().update(this);
        eventService.postEvent(EventType.VALIDATIONRULESET_DELETED.topic(), this);
    }

    @Override
    public List<IValidationRule> getRules() {
        return Collections.unmodifiableList(rules);
    }

    public List<IValidationRule> getRules(int start, int limit) {
        return Collections.unmodifiableList(
                getRuleQuery().select(
                        Operator.EQUAL.compare("ruleSetId", this.id), new Order[]{Order.ascending("upper(name)")}, false, new String[]{}, start + 1, start + limit));
    }


    private IValidationRule doGetRule(long id) {
        for (IValidationRule singleRule : rules) {
            if (singleRule.getId() == id) {
                return singleRule;
            }
        }
        return null;
    }

    private List<IValidationRule> loadRules() {
        return getRuleQuery().select(Where.where("ruleSet").isEqualTo(this), Order.ascending("name").toUpperCase());
    }

    private QueryExecutor<IValidationRule> getRuleQuery() {
        QueryExecutor<IValidationRule> ruleQuery = dataModel.query(IValidationRule.class, ValidationRuleProperties.class);
        ruleQuery.setRestriction(where("obsoleteTime").isNull());
        return ruleQuery;
    }

    @Override
    public IValidationRule addRule(ValidationAction action, String implementation, String name) {
        ValidationRuleImpl newRule = ValidationRuleImpl.from(dataModel, this, action, implementation, rules.size() + 1, name);
        rules.add(newRule);
        return newRule;
    }

    @Override
    public IValidationRule updateRule(long id, String name, boolean activeStatus, List<String> mRIDs, Map<String, Object> properties) {
        IValidationRule rule = getExistingRule(id);
        return doUpdateRule(rule, name, activeStatus, mRIDs, properties);
    }

    private IValidationRule doUpdateRule(IValidationRule rule, String name,  boolean activeStatus, List<String> mRIDs, Map<String, Object> properties) {
        rule.rename(name);

        if (activeStatus != rule.isActive()) {
            rule.toggleActivation();
        }
        updateReadingTypes(rule, mRIDs);
        rule.setProperties(properties);

        return rule;
    }

    private void updateReadingTypes(IValidationRule rule, List<String> mRIDs) {
        rule.clearReadingTypes();
        for (String readingTypeMRID : mRIDs) {
            rule.addReadingType(readingTypeMRID);
        }
    }

    private IValidationRule getExistingRule(long id) {
        IValidationRule rule = doGetRule(id);
        if (rule == null) {
            throw new IllegalArgumentException("The ruleset " + this.getId() + " doesn't contain provided ruleId: " + id);
        }
        return rule;
    }

    @Override
    public void deleteRule(ValidationRule rule) {
        IValidationRule iRule = (IValidationRule) rule;
        if (rules.contains(iRule)) {
            iRule.delete();
        } else {
            throw new IllegalArgumentException("The rulset " + this.getId() + " doesn't contain provided ruleId: " + rule.getId());
        }      
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(getName());
        builder.append('\n');
        for (IValidationRule validationRule : rules) {
            builder.append(validationRule.toString()).append('\n');
        }
        return builder.toString();
    }

    private DataMapper<IValidationRuleSet> validationRuleSetFactory() {
        return dataModel.mapper(IValidationRuleSet.class);
    }

    public List<ValidationRule> getRules(Iterable<? extends ReadingType> readingTypes) {
        List<ValidationRule> result = new ArrayList<>();
        List<IValidationRule> rules = getRules();
        for (ValidationRule rule : rules) {
            Set<ReadingType> readingTypesForRule = rule.getReadingTypes();
            for (ReadingType readingtype : readingTypes) {
                if (readingTypesForRule.contains(readingtype)) {
                    result.add(rule);
                }
            }
        }
        return result;
    }

    @Override
    public Instant getObsoleteDate() {
        return this.obsoleteTime;
    }

    private void setObsoleteTime(Instant obsoleteTime) {
        this.obsoleteTime = obsoleteTime;
    }
}
