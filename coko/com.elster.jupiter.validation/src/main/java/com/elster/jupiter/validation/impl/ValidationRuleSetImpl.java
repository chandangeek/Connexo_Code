package com.elster.jupiter.validation.impl;

import static com.elster.jupiter.util.conditions.Where.where;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.QueryExecutor;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.util.Checks;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.conditions.Where;
import com.elster.jupiter.validation.MessageSeeds.Constants;
import com.elster.jupiter.validation.ValidationAction;
import com.elster.jupiter.validation.ValidationRule;
import com.elster.jupiter.validation.ValidationRuleProperties;
import com.elster.jupiter.validation.ValidationRuleSet;

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

    @Valid
    private List<IValidationRule> rules = new ArrayList<>();

    private List<IValidationRule> rulesToSave = new ArrayList<>();

    private final EventService eventService;
    private final DataModel dataModel;
    private final Provider<ValidationRuleImpl> validationRuleProvider;

    @Inject
    ValidationRuleSetImpl(DataModel dataModel, EventService eventService, Provider<ValidationRuleImpl> validationRuleProvider) {
        // for persistence
        this.dataModel = dataModel;
        this.eventService = eventService;
        this.validationRuleProvider = validationRuleProvider;
    }
    
    ValidationRuleSetImpl init(String name) {
    	return init(name,null);
    }

    ValidationRuleSetImpl init(String name, String description) {
        this.name = Checks.is(name).emptyOrOnlyWhiteSpace() ? null : name.trim();
        this.description = description;
        return this;
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
        this.name = name != null ? name.trim() : name;
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
        addNewRules();
        if (getId() == 0) {
            doPersist();
        } else {
            doUpdate();
        }
    }

    private void doUpdate() {
        Save.UPDATE.save(dataModel, this);
        doGetRules().forEach( rule -> Save.UPDATE.save(dataModel, rule));
        eventService.postEvent(EventType.VALIDATIONRULESET_UPDATED.topic(), this);
    }

    private void doPersist() {
        Save.CREATE.save(dataModel, this);
        eventService.postEvent(EventType.VALIDATIONRULESET_CREATED.topic(), this);
    }

    @Override
    public void delete() {
        this.setObsoleteTime(Instant.now()); // mark obsolete
        doGetRules().forEach(rule -> rule. delete());   
        validationRuleSetFactory().update(this);
        eventService.postEvent(EventType.VALIDATIONRULESET_DELETED.topic(), this);
    }

    @Override
    public List<IValidationRule> getRules() {
        return doGetRules()
        	.sorted(Comparator.comparing(rule -> rule.getName().toUpperCase()))
        	.collect(Collectors.toList());
    }

    private void addNewRules() {
        rulesToSave.forEach( newRule -> {
            Save.CREATE.validate(dataModel, newRule);
            rules.add(newRule);
        });
        rulesToSave.clear();
    }
    
    private Stream<IValidationRule> doGetRules() {
    	return rules.stream().filter(rule -> !rule.isObsolete());
    }

    public List<IValidationRule> getRules(int start, int limit) {
        return Collections.unmodifiableList(
                getRuleQuery().select(
                        Where.where("ruleSet").isEqualTo(this), 
                        new Order[]{Order.ascending("name").toUpperCase()}, 
                        false, 
                        new String[]{}, 
                        start + 1, 
                        start + limit));
    }

    private QueryExecutor<IValidationRule> getRuleQuery() {
        QueryExecutor<IValidationRule> ruleQuery = dataModel.query(IValidationRule.class, ValidationRuleProperties.class);
        ruleQuery.setRestriction(where("obsoleteTime").isNull());
        return ruleQuery;
    }

    @Override
    public IValidationRule addRule(ValidationAction action, String implementation, String name) {
        ValidationRuleImpl newRule = validationRuleProvider.get().init(this, action, implementation, name);
        rulesToSave.add(newRule);
        return newRule;
    }

    @Override
    public IValidationRule updateRule(long id, String name, boolean activeStatus, ValidationAction action, List<String> mRIDs, Map<String, Object> properties) {
        IValidationRule rule = getExistingRule(id);
        return doUpdateRule(rule, name, activeStatus, action,  mRIDs, properties);
    }

    private IValidationRule doUpdateRule(IValidationRule rule, String name,  boolean activeStatus, ValidationAction action, List<String> mRIDs, Map<String, Object> properties) {
        rule.rename(name);
        rule.setAction(action);

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
    	return doGetRules()
    		.filter(rule -> rule.getId() == id)
    		.findFirst()
    		.orElseThrow(() -> new IllegalArgumentException("The ruleset " + this.getId() + " doesn't contain provided ruleId: " + id));
    }

    @Override
    public void deleteRule(ValidationRule rule) {
        IValidationRule iRule = (IValidationRule) rule;
        if (doGetRules().anyMatch( candidate -> candidate.equals(iRule))) {
            iRule.delete();
        } else {
            throw new IllegalArgumentException("The rulset " + this.getId() + " doesn't contain provided ruleId: " + rule.getId());
        }      
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(getName());
        builder.append('\n');
        doGetRules().forEach(rule -> builder.append(rule.toString()).append('\n'));
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
